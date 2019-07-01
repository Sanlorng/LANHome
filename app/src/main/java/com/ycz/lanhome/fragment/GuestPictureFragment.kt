package com.ycz.lanhome.fragment

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ycz.lanhome.BaseViewHolder

import com.ycz.lanhome.R
import com.ycz.lanhome.network.SocketWrapper
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.service.DeviceService
import com.ycz.lanhome.toast
import com.ycz.lanhome.viewmodel.GuestPictureViewModel
import com.ywl5320.wlmedia.WlMedia
import com.ywl5320.wlmedia.enums.WlCodecType
import com.ywl5320.wlmedia.enums.WlMute
import com.ywl5320.wlmedia.enums.WlPlayModel
import kotlinx.android.synthetic.main.guest_picture_fragment.*
import kotlinx.android.synthetic.main.image_item.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class GuestPictureFragment : Fragment() {

    companion object {
        fun newInstance() = GuestPictureFragment()
    }

    private lateinit var viewModel: GuestPictureViewModel
    private var binder: DeviceService.DeviceBinder? = null
    private var socketWrapper: SocketWrapper? = null
    private lateinit var surfaceHolder: SurfaceHolder
    private var ip = ""
    private var receiveMode = false
    private var fileName = ""
    private var listFile = ArrayList<String>()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            binder = p1 as DeviceService.DeviceBinder
            socketWrapper = binder?.connectDevice(ip, javaClass.name, callback)
            socketWrapper?.sendMessage("getPic")
            Log.e("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",ip)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }
    private val callback = object : SocketWrapper.Callback {
        private var fileOutputStream: FileOutputStream? = null

        override fun onClose() {
            toast("连接已关闭")
            activity?.finish()
        }

        override fun onReceiveMessage(bytes: ByteArray, length: Int) {
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
                fileOutputStream?.write(bytes,16,len)
            }else {
                val string = String(bytes, 16, 16 + len)
                when {
                    string == "newPic" -> {
                        receiveMode = true
                        socketWrapper?.sendMessage("getPic")
                        fileName = "${context?.cacheDir?.path}${File.separator}${System.currentTimeMillis()}.pic"
                        val file = File(fileName)
                        file.deleteOnExit()
                        file.createNewFile()
                        listFile.add(fileName)
                        fileOutputStream = file.outputStream()
                    }
                    string.contains( "oneFinished") -> {
                        fileOutputStream?.close()
                        listImage.adapter?.notifyItemInserted(listFile.lastIndex)
                        Glide.with(this@GuestPictureFragment)
                            .load(fileName)
                            .into(imagePreviewPreview)
                        fileName = "${context?.cacheDir?.path}${File.separator}${System.currentTimeMillis()}.pic"
                        listFile.add(fileName)
                        val file = File(fileName)
                        file.deleteOnExit()
                        file.createNewFile()
                        fileOutputStream = file.outputStream()
                        socketWrapper?.sendMessage("picOK")
                    }
                    string == "picFinished" -> receiveMode = false
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
        return inflater.inflate(R.layout.guest_picture_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GuestPictureViewModel::class.java)
        ip = arguments?.getString(AppConfig.KEY_DEVICE_ADDRESS) ?: ""
        Log.e("ip",ip)
        listImage.adapter = Adapter(listFile)
        listImage.layoutManager = LinearLayoutManager(context!!)
        context?.bindService(
            Intent(context, DeviceService::class.java),
            connection,
            Service.BIND_AUTO_CREATE
        )
        Log.e("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",ip)
        // TODO: Use the ViewModel

    }

    override fun onDestroy() {
        binder?.disconnectDevice(ip, javaClass.name)
        context?.unbindService(connection)
        super.onDestroy()
    }

    inner class Adapter(val list: ArrayList<String>): RecyclerView.Adapter<BaseViewHolder>() {
        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            Glide.with(this@GuestPictureFragment)
                .load(fileName)
                .into(holder.itemView.imagePreview)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
//            val image = ImageView(parent.context)
//            image.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT,ViewGroup.MarginLayoutParams.MATCH_PARENT).apply {
//                topMargin = 120
//            }
            return BaseViewHolder(R.layout.image_item,parent)
        }
    }


}
