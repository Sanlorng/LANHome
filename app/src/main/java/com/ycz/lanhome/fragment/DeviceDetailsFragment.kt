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
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.tabs.TabLayoutMediator

import com.ycz.lanhome.R
import com.ycz.lanhome.ShellActivity
import com.ycz.lanhome.SocketWrapper
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.service.DeviceService
import kotlinx.android.synthetic.main.device_details_fragment.*

class DeviceDetailsFragment : Fragment() {
    private var binder: DeviceService.DeviceBinder? = null
    private var ip = ""
    private val listChart = ArrayList<LineChart>()
    private val listAnalyzeData = List(3) {
        ArrayList<Entry>()
    }
    companion object {
        fun newInstance() = DeviceDetailsFragment()
    }

    private lateinit var viewModel: DeviceDetailsViewModel
    private var socketWrapper: SocketWrapper? = null
    private val callback = object : SocketWrapper.Callback {
        override fun onReceiveMessage(string: String) {
            val sub = string.substring(11,15)
//            Log.e("sub",sub)
            val length =sub .toInt()

            Log.e("length",length.toString())
            if (string.startsWith("pic").not() && string.startsWith("vid").not()) {
                val command = string.substring(16, 16 + length)
                Log.e("xxxxx", command)
                if (string.substring(0, 3) == "com")
                    when (command) {
                        "odok" -> controlDoor.isChecked = true
                        "cdok" -> controlDoor.isChecked = false
                        else -> {
                            if (command.startsWith("m")) {
                                val value = command.removePrefix("m").toInt()
                                listAnalyzeData[2].add(Entry(3.0f, value.toFloat()))

//                                listChart[2].data.notifyDataChanged()
                                concenDetails.text = "指数：${value}"
                            } else if (command.startsWith("h")) {
                                val value = command.removePrefix("h").toInt()
                                listAnalyzeData[1].add(Entry(4.0f, value.toFloat()))
//                                listChart[1].data.notifyDataChanged()
                                humiDetails.text = "湿度：${value}"
                            } else if (command.startsWith("t")) {
                                val value = command.removePrefix("t").toInt()
                                listAnalyzeData[0].add(Entry(5.0f, value.toFloat()))
//                                listChart[0].data.notifyDataChanged()
                                tempDetails.text = "温度：${value}"
                            } else if (command.startsWith("ow") && command.endsWith("ok")) {
                                controlWindowLevel.currentLevel = command.removePrefix("ow").removeSuffix("ok").toInt()
                            } else if (command.startsWith("of") && command.endsWith("ok"))
                                controlWindowLevel.currentLevel = command.removePrefix("of").removePrefix("ok").toInt()

                        }
                    }
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            binder = p1 as DeviceService.DeviceBinder
            socketWrapper = binder?.connectDevice(ip,javaClass.name,callback)
            socketWrapper?.sendMessage("getInfo")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.device_details_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DeviceDetailsViewModel::class.java)
        ip = arguments?.getString("deviceAddress","")?:""
        if (ip.isNotEmpty()) {
            titleTextDeviceDetails.text = "设备：$ip"
        }
        controlDoor.setOnCheckedChangeListener { compoundButton, b ->
            if (b)
                socketWrapper?.sendMessage("od")
            else
                socketWrapper?.sendMessage("cd")
        }
        controlFanLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var isNotDrag = true
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isNotDrag = false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                isNotDrag = true
                val p1 = p0?.progress?:0
                if (isNotDrag) {
                    if (p1 == 0)
                        controlFanLevel.titleText = "风扇：关"
                    else
                        controlFanLevel.titleText = "风扇：$p1 级"
                }
                socketWrapper?.sendMessage("of$p1")
            }
        })
        controlWindowLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var isNotDrag = true
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isNotDrag = false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                isNotDrag = true
                val p1 = p0?.progress?:0
                if (isNotDrag) {
                    if (p1 == 0)
                        controlWindowLevel.titleText = "窗户：关"
                    else
                        controlWindowLevel.titleText = "窗户：$p1 级"
                }
                socketWrapper?.sendMessage("ow$p1")
            }
        })

        previewVideoDeviceDetails.setOnClickListener {
            startActivity(Intent(context!!,ShellActivity::class.java).apply {
                putExtra(AppConfig.KEY_NAVIGATE_ID,R.id.videoFragment)
                putExtra(AppConfig.KEY_DEVICE_ADDRESS,ip)
            })
        }
        viewPagerAnalyze.offscreenPageLimit = 3
        viewPagerAnalyze.adapter = object : RecyclerView.Adapter<BaseViewHolder>() {
            override fun getItemCount(): Int {
                return 0
            }

            override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                (holder.itemView as LineChart).data = LineData(LineDataSet(
                    listAnalyzeData[position],
                    when(position) {
                        0 -> "温度"
                        1 -> "湿度"
                        else -> "指数"
                    }
                ))
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val view = LineChart(parent.context)
                view.isLogEnabled = true
                view.layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.MarginLayoutParams.MATCH_PARENT,
                    ViewGroup.MarginLayoutParams.MATCH_PARENT
                )
                listChart.add(view)
                return BaseViewHolder(view)
            }
        }
        TabLayoutMediator(tabPageAnalyze,viewPagerAnalyze) { tab, position ->
            tab.text = when(position) {
                0 -> "温度"
                1 -> "湿度数"
                else -> "指数"
            }

        }.attach()
        context?.bindService(Intent(context!!,DeviceService::class.java),connection,Service.BIND_AUTO_CREATE)

    }

    override fun onDestroy() {
        binder?.disconnectDevice(ip,javaClass.name)
        context?.unbindService(connection)
        super.onDestroy()
    }
}
