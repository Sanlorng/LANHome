package com.ycz.lanhome.fragment

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide

import com.ycz.lanhome.R
import com.ycz.lanhome.SocketWrapper
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.service.DeviceService
import kotlinx.android.synthetic.main.video_fragment.*
import java.io.File
import java.io.FileOutputStream

class GuestPictureFragment : Fragment() {

    companion object {
        fun newInstance() = GuestPictureFragment()
    }

    private lateinit var viewModel: VideoViewModel
    private var binder : DeviceService.DeviceBinder? = null
    private var socketWrapper : SocketWrapper? = null
    private lateinit var surfaceHolder: SurfaceHolder
    private var ip = ""
    private var receiveMode = false
    private var fileName = ""
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            binder = p1 as DeviceService.DeviceBinder
            socketWrapper = binder?.connectDevice(ip,javaClass.name,callback)
            socketWrapper?.sendMessage("getPic")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }
    private val callback = object : SocketWrapper.Callback {
        private var fileOutputStream: FileOutputStream? = null
        override fun onReceiveMessage(bytes: ByteArray,length: Int) {
            val str = String(bytes,0,16)

            val len = str.substring(11,15).toInt()
            if (str.startsWith("pic")) {
                if (fileOutputStream == null ) {
                    fileName = "${context?.cacheDir?.path}${File.separator}${System.currentTimeMillis()}.pic"
                    val file = File(fileName)
                    file.deleteOnExit()
                    file.createNewFile()
                    fileOutputStream = file.outputStream()
                }
                Log.e("write",(fileOutputStream!= null).toString())
                fileOutputStream?.write(bytes,16,len)
            }else {
                val string = String(bytes, 16, 16 + len)
                Log.e("string",string)
                if (string == "newPic") {
                    receiveMode = true
                    socketWrapper?.sendMessage("getPic")
                    fileName = "${context?.cacheDir?.path}${File.separator}${System.currentTimeMillis()}.pic"
                    val file = File(fileName)
                    file.deleteOnExit()
                    file.createNewFile()
                    fileOutputStream = file.outputStream()
                } else if (string.contains( "oneFinished")) {
                    fileOutputStream?.close()
                    Log.e("fileName",fileName)
                    Glide.with(this@GuestPictureFragment)
                        .load(fileName)
                        .into(videoPreview)
//                    socketWrapper?.sendMessage("getPic")
                    fileName = "${context?.cacheDir?.path}${File.separator}${System.currentTimeMillis()}.pic"
                    val file = File(fileName)
                    file.deleteOnExit()
                    file.createNewFile()
                    fileOutputStream = file.outputStream()
                    socketWrapper?.sendMessage("picOK")
                } else if (string == "picFinished") {
                    receiveMode = false
                }
            }
//            val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
//            videoPreview.setImageBitmap(bitmap)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VideoViewModel::class.java)
        ip  = arguments?.getString(AppConfig.KEY_DEVICE_ADDRESS)?:""
        context?.bindService(Intent(context,DeviceService::class.java),connection,Service.BIND_AUTO_CREATE)
        // TODO: Use the ViewModel
    }

    override fun onDestroy() {
//        socketWrapper?.sendMessage("stopVideo")
        binder?.disconnectDevice(ip,javaClass.name)
        context?.unbindService(connection)
        super.onDestroy()
    }



}
