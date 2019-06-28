package com.ycz.lanhome.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.le.ScanCallback
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ycz.lanhome.R
import com.ycz.lanhome.SocketWrapper
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.defaultPreferences
import com.ycz.lanhome.model.Device
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class DeviceService : Service() {
    private val binder = DeviceBinder()
    private var scanJob: Job? = null
    private var socketWrappers = HashMap<String,SocketWrapper>()
    override fun onCreate() {
        createNotification()
        val notification = NotificationCompat.Builder(this,AppConfig.KEY_CONTROL_NOTIFICATION_CHANNEL)
            .setContentTitle("前台服务")
            .setContentText("保持设备连接")
            .build()
        if (defaultPreferences.getBoolean(getString(R.string.key_setting_using_foreground_service_keep_alive),true)) {
            startForeground(AppConfig.KET_CONTROL_NOTIFICATION_CHANNEL_INT,notification)
        }
        defaultPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
            val keyString = getString(R.string.key_setting_using_foreground_service_keep_alive)
            if (s == keyString && sharedPreferences.getBoolean(keyString,true)) {
                startForeground(AppConfig.KET_CONTROL_NOTIFICATION_CHANNEL_INT,notification)
            }else if (s == keyString) {
                stopForeground(true)
            }
        }
        binder.scanDevice()
    }
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        scanJob?.cancel()
        super.onDestroy()
    }

    fun createNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            AppConfig.KEY_CONTROL_NOTIFICATION_CHANNEL,
            getString(R.string.foreground_service),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "用来保持设备连接"
        manager.createNotificationChannel(channel)
    }
    inner class DeviceBinder: Binder() {
        lateinit var socket: DatagramSocket
        val devices = HashMap<String,Device>()
        val listeners = HashMap<String,DeviceScanCallback>()
        val socketCallbacks = HashMap<String,HashMap<String,SocketWrapper.Callback>>()
        init {
            GlobalScope.launch {
                socket = DatagramSocket(null)
                socket.reuseAddress = true
                socket.bind(InetSocketAddress(19998))
            }
        }
        fun scanDevice() {
            if (true) {
                Log.e("scanStart","true")
                if (scanJob != null) {
                    scanJob?.cancel()
                }
                listeners.values.forEach {
                    it.onNewDeviceScanStart()
                }
                scanJob = GlobalScope.launch {
                    devices.clear()
                    socketCallbacks.clear()
                    socketWrappers.values.forEach {
                        it.close()
                    }
                    socketWrappers.clear()
                    val result = withTimeoutOrNull(60000) {

                        val buf = ByteArray(1024)
                        val pack = DatagramPacket(buf, buf.size)
                        val systemLong = System.currentTimeMillis()
                        while (true) {
                            if (System.currentTimeMillis() - systemLong >= 15000)
                                break
                            socket.receive(pack)
                            val string = String(pack.data, 0, pack.length)
                            if (string.startsWith("ycz ")) {
                                val name = string.split(" ")[1]
                                val hostAddress = pack.address.hostAddress
                                val device = Device(hostAddress,name)
                                if (devices.containsKey(hostAddress).not()) {

                                    devices[hostAddress] = device
                                    withContext(Dispatchers.Main) {
                                        Log.e("ete","x")
                                        listeners.values.forEach {
                                            it.onNewDeviceAdded(device)
                                        }
                                    }
                                }
                                Log.e("device", device.toString())
                            }
                            Log.e("socket", String(pack.data, 0, pack.length))
                        }
                    }.also {
                        withContext(Dispatchers.Main) {
                            listeners.values.forEach {
                                it.onNewDeviceScanFinish()
                            }
                        }
                    }
                }
            }else {
                Log.e("stared","")
                listeners.values.forEach {
                    it.onNewDeviceScanFinish()
                }
            }
        }
        fun connectDevice(ip: String,tag: String,callback:SocketWrapper.Callback):SocketWrapper? {
            if (socketWrappers.containsKey(ip).not()) {
                socketCallbacks[ip] = HashMap()
                socketWrappers[ip] = SocketWrapper(ip,19999,DeviceSocketCallback(ip))
            }
            socketCallbacks[ip]?.put(tag,callback)
            return socketWrappers[ip]
        }

        fun disconnectDevice(ip: String,tag: String) {
            socketCallbacks[ip]?.remove(tag)
        }

        fun addDeviceScanCallback(tag: String, listener:DeviceScanCallback) {
            listeners[tag] = listener
        }

        fun removeDeviceScanCallback(tag: String) {
            listeners.remove(tag)
        }
        inner class DeviceSocketCallback(val ip: String):SocketWrapper.Callback {
            override fun onOpen() {
                socketCallbacks[ip]?.values?.forEach {
                    it.onOpen()
                }
            }

            override fun onClose() {
                socketCallbacks[ip]?.values?.forEach {
                    it.onClose()
                }
            }

            override fun onException(t: Throwable?) {
                socketCallbacks[ip]?.values?.forEach {
                    it.onException(t)
                }
            }

            override fun onReceiveMessage(string: String) {
                socketCallbacks[ip]?.values?.forEach {
                    it.onReceiveMessage(string)
                }
            }

            override fun onReceiveMessage(bytes: ByteArray, length: Int) {
                socketCallbacks[ip]?.values?.forEach {
                    it.onReceiveMessage(bytes, length)
                }
            }
        }
    }


    interface DeviceScanCallback {
        fun onNewDeviceAdded(device: Device)
        fun onNewDeviceScanStart()
        fun onNewDeviceScanFinish()
    }
}
