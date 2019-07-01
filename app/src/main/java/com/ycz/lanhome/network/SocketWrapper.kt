package com.ycz.lanhome.network

import android.util.Log
import kotlinx.coroutines.*
import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.net.Socket

class SocketWrapper(
    host: String, port: Int,
    var bufSize: Int = 1024,
    var needBind: Boolean = true,
    var callback: Callback? = null
) {
    private var socket: Socket? = null
    private var sendJob: Job? = null
    private var receiveJob: Job? = null

    var isClient = false

    init {
        GlobalScope.launch {
            socket = withTimeoutOrNull(1000) {
                try {
                    Socket(host, port)
                } catch (t: Throwable) {
                    withContext(Dispatchers.Main) {
                        callback?.onConnectException(t)
                    }
                    null
                }

            }
            withContext(Dispatchers.Main) {
                callback?.onOpen()
                isClient = true
            }
            receiveJob = GlobalScope.launch {
                try {

                    val io = socket?.getInputStream()
                    val buf = ByteArray(bufSize)
                    var len = io?.read(buf, 0, bufSize) ?: 0
                    while (len != -1) {
                        while (len != bufSize && needBind) {
                            val a = io?.read(buf, len, bufSize - len) ?: 0
                            if (a == -1)
                                break
                            else
                                len += a
                        }
                        val str = String(buf, 0, bufSize)
                        withContext(Dispatchers.Main) {
                            //                            Log.e("socketWrapper",buf.toString())
                            Log.e("data len", len.toString())
                            Log.e("socketWrapper", str)
                            callback?.onReceiveMessage(buf, len)
                            callback?.onReceiveMessage(str)
                        }
                        len = io?.read(buf) ?: 0
                    }
                    withContext(Dispatchers.Main) {
                        callback?.onClose()
                        Log.e("socketClose", "socketClose")
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    withContext(Dispatchers.Main) {
                        if (t is IndexOutOfBoundsException || t is NumberFormatException) {
                            receiveJob?.start()
                        }
                        callback?.onException(t)
                    }
                }

            }
        }
    }

    fun sendMessage(string: String) {
        GlobalScope.launch {
            try {
                while (socket == null);
                socket?.getOutputStream()?.write(string.toByteArray())
            } catch (t: Throwable) {
                callback?.onException(t)
            }
        }
    }

    fun close() {
        receiveJob?.cancel()
        socket?.close()
    }

    interface Callback {
        fun onOpen() {}
        fun onClose() {}
        fun onReceiveMessage(string: String) {}
        fun onReceiveMessage(bytes: ByteArray, length: Int) {}
        fun onException(t: Throwable?) {}
        fun onConnectException(t: Throwable?) {}
    }
}