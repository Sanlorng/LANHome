package com.ycz.lanhome.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ycz.lanhome.R
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.network.SocketWrapper
import com.ycz.lanhome.viewmodel.VideoStreamViewModel
import com.ywl5320.wlmedia.WlMedia
import com.ywl5320.wlmedia.enums.WlCodecType
import com.ywl5320.wlmedia.enums.WlMute
import com.ywl5320.wlmedia.enums.WlPlayModel
import kotlinx.android.synthetic.main.video_stream_fragment.*

class VideoStreamFragment : Fragment() {
    private lateinit var socket: SocketWrapper

    companion object {
        fun newInstance() = VideoStreamFragment()
    }

    private lateinit var viewModel: VideoStreamViewModel
    private lateinit var wlMedia: WlMedia
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_stream_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VideoStreamViewModel::class.java)
        val ip = arguments?.getString(AppConfig.KEY_DEVICE_ADDRESS) ?: ""
        wlMedia = WlMedia()
        wlMedia.setPlayModel(WlPlayModel.PLAYMODEL_ONLY_VIDEO)
        wlMedia.setCodecType(WlCodecType.CODEC_MEDIACODEC)
        wlMedia.mute = WlMute.MUTE_CENTER
        wlMedia.playPitch = 1.0f
        wlMedia.playSpeed = 1.0f
        wlMedia.setBufferSource(true)
        videoSee.setWlMedia(wlMedia)
        wlMedia.setOnPreparedListener {
            wlMedia.start()
        }
        socket = SocketWrapper(ip, 20000,
            409600,
            false,
            object : SocketWrapper.Callback {
                var bufferQueueSize = 0
                override fun onReceiveMessage(bytes: ByteArray, length: Int) {
                    if (wlMedia.isPlaying) {
                        if (bufferQueueSize < 100) {
                            bufferQueueSize = wlMedia.putBufferSource(bytes, length)
                            while (bufferQueueSize < 0) {
                                bufferQueueSize = wlMedia.putBufferSource(bytes, length)
                            }
                        } else {
                            bufferQueueSize = wlMedia.putBufferSource(bytes, 0)
                        }
                    }
                }
            })
//        socket.sendMessage("getVideo")
        wlMedia.prepared()
    }

    override fun onDestroy() {
        socket.sendMessage("stopVideo")
        socket.close()
        super.onDestroy()
    }

}
