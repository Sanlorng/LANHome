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
import com.ycz.lanhome.BaseViewHolder

import com.ycz.lanhome.R
import com.ycz.lanhome.ShellActivity
import com.ycz.lanhome.network.SocketWrapper
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.service.DeviceService
import com.ycz.lanhome.toast
import com.ycz.lanhome.viewmodel.DeviceDetailsViewModel
import kotlinx.android.synthetic.main.device_details_fragment.*

class DeviceDetailsFragment : Fragment() {
    private var binder: DeviceService.DeviceBinder? = null
    private var ip = ""
    private var name = ""
    private val listChart = ArrayList<LineChart>()
    private var needStates = false
    private val listAnalyzeData = List(3) {
        ArrayList<Entry>()
    }

    companion object {
        fun newInstance() = DeviceDetailsFragment()
    }

    private lateinit var viewModel: DeviceDetailsViewModel
    private var socketWrapper: SocketWrapper? = null
    private val callback = object : SocketWrapper.Callback {

        override fun onClose() {
            super.onClose()
            toast("连接已关闭")
            activity?.finish()
        }

        override fun onReceiveMessage(string: String) {
            val sub = string.substring(11, 15)
            val length = sub.toInt()
            if (string.startsWith(AppConfig.KEY_CONTROL_PICTURE).not() && string.startsWith(AppConfig.KEY_CONTROL_VIDEO).not()) {
                val command = string.substring(16, 16 + length)
                Log.e("command", command)
                if (string.substring(0, 3) == AppConfig.KEY_CONTROL_COMMAND)
                    when {
                        command == AppConfig.KEY_CONTROL_OPEN_DOOR -> controlDoor.isChecked = true
                        command == AppConfig.KEY_CONTROL_CLOSE_DOOR -> controlDoor.isChecked = false
                        command.startsWith("m") -> {
                            val value = command.removePrefix("m").toInt()
                            listAnalyzeData[2].add(
                                Entry(
                                    listAnalyzeData[2].size.toFloat(),
                                    value.toFloat()
                                )
                            )
                            val chart = listChart[2]
                            val chartData= chart.data.getDataSetByIndex(0)
                            chartData.addEntry(Entry(listAnalyzeData[2].size.toFloat(), value.toFloat()))
                            if (chartData.entryCount > 10) {
                                chartData.removeEntry(0)
                            }
                            chart.data.notifyDataChanged()
                            chart.notifyDataSetChanged()
                            chart.invalidate()
                            concenDetails.text = String.format("指数：%s",value)
                        }
                        command.startsWith("h") -> {
                            val value = command.removePrefix("h").toInt()
                            listAnalyzeData[1].add(
                                Entry(
                                    listAnalyzeData[1].size.toFloat(),
                                    value.toFloat()
                                )
                            )
                            val chart = listChart[1]
                            val chartData =
                            chart.data.getDataSetByIndex(0)
                                chartData.addEntry(Entry(listAnalyzeData[1].size.toFloat(), value.toFloat()))
                            if (chartData.entryCount > 10) {
                                chartData.removeEntry(0)
                            }
                            chart.data.notifyDataChanged()
                            chart.notifyDataSetChanged()
                            chart.invalidate()
                            humiDetails.text = String.format("湿度：%s",value)
                        }
                        command.startsWith("t") -> {
                            val value = command.removePrefix("t").toInt()
                            listAnalyzeData[0].add(
                                Entry(
                                    listAnalyzeData[0].size.toFloat(),
                                    value.toFloat()
                                )
                            )
                            val chart = listChart[0]
                            val chartData = chart.data.getDataSetByIndex(0)
                                chartData.addEntry(Entry(listAnalyzeData[0].size.toFloat(), value.toFloat()))
                            if (chartData.entryCount > 10) {
                                chartData.removeEntry(0)
                            }
                            chart.data.notifyDataChanged()
                            chart.notifyDataSetChanged()
                            chart.invalidate()
                            tempDetails.text = String.format("温度：%s",value)
                        }
                        command.startsWith(AppConfig.KEY_CONTROL_WINDOW) -> {
                            val s = command.removePrefix(AppConfig.KEY_CONTROL_WINDOW)
                            controlWindowLevel.currentLevel = s.toInt()
                        }
                        command.startsWith(AppConfig.KEY_CONTROL_FAN) -> {
                            val s = command.removePrefix(AppConfig.KEY_CONTROL_FAN)
                            controlFanLevel.currentLevel = s.toInt()
                        }
                    }


            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            binder = p1 as DeviceService.DeviceBinder
            socketWrapper =
                binder?.connectDevice(ip, this@DeviceDetailsFragment.javaClass.name, callback)
            socketWrapper?.sendMessage(AppConfig.KEY_CONTROL_GET_INFO)
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
        ip = arguments?.getString(AppConfig.KEY_DEVICE_ADDRESS, "") ?: ""
        name = arguments?.getString(AppConfig.KEY_DEVICE_NAME) ?: ""
        if (ip.isNotEmpty()) {
            titleTextDeviceDetails.text = String.format(getString(R.string.val_device),name)
            subTitleTextDeviceDetails.text = String.format(getString(R.string.val_address),ip)
        }
        controlDoor.setOnClickListener {
            if (controlDoor.isChecked)
                socketWrapper?.sendMessage(AppConfig.KEY_CONTROL_OPEN_DOOR)
            else
                socketWrapper?.sendMessage(AppConfig.KEY_CONTROL_CLOSE_DOOR)
        }
        controlFanLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var isNotDrag = true
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                if (p1 == 0)
                    controlFanLevel.titleText = "风扇：关"
                else
                    controlFanLevel.titleText = "风扇：$p1 级"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isNotDrag = false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                isNotDrag = true
                val p1 = p0?.progress ?: 0
                if (isNotDrag) {
                }
                socketWrapper?.sendMessage("${AppConfig.KEY_CONTROL_FAN}$p1")
            }
        })
        controlWindowLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var isNotDrag = true
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p1 == 0)
                    controlWindowLevel.titleText = "窗户：关"
                else
                    controlWindowLevel.titleText = "窗户：$p1 级"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isNotDrag = false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                isNotDrag = true
                val p1 = p0?.progress ?: 0
                if (isNotDrag) {

                }
                socketWrapper?.sendMessage("${AppConfig.KEY_CONTROL_WINDOW}$p1")
            }
        })

        previewImageDeviceDetails.setOnClickListener {
            startActivity(Intent(context!!, ShellActivity::class.java).apply {
                putExtra(AppConfig.KEY_NAVIGATE_ID, R.id.guestPictureFragment)
                putExtra(AppConfig.KEY_DEVICE_ADDRESS, ip)
            })
        }
        previewVideoDeviceDetails.setOnClickListener {
            startActivity(Intent(context!!, ShellActivity::class.java).apply {
                putExtra(AppConfig.KEY_NAVIGATE_ID, R.id.videoStreamFragment)
                putExtra(AppConfig.KEY_DEVICE_ADDRESS, ip)
                socketWrapper?.sendMessage(AppConfig.KEY_CONTROL_GET_VIDEO)
            })
        }
        viewPagerAnalyze.offscreenPageLimit = 3
        viewPagerAnalyze.adapter = object : RecyclerView.Adapter<BaseViewHolder>() {
            override fun getItemCount(): Int {
                return 3
            }

            override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                val chart = (holder.itemView as LineChart)
                chart.xAxis.setLabelCount(10, true)
                chart.data = LineData(LineDataSet(
                    listAnalyzeData[position],
                    when (position) {
                        0 -> "温度"
                        1 -> "湿度"
                        else -> "指数"
                    }
                ).apply {
                    setDrawFilled(true)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                    notifyDataSetChanged()
                    chart.notifyDataSetChanged()
                    chart.invalidate()
                })
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val view = LineChart(parent.context)
//                view.isLogEnabled = true
                view.layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.MarginLayoutParams.MATCH_PARENT,
                    ViewGroup.MarginLayoutParams.MATCH_PARENT
                )
                view.animateX(2500)
                listChart.add(view)
                return BaseViewHolder(view)
            }
        }
        TabLayoutMediator(tabPageAnalyze, viewPagerAnalyze) { tab, position ->
            tab.text = when (position) {
                0 -> "温度"
                1 -> "湿度数"
                else -> "液化气指数"
            }

        }.attach()
        context?.bindService(
            Intent(context!!, DeviceService::class.java),
            connection,
            Service.BIND_AUTO_CREATE
        )

    }

    override fun onDestroy() {
        binder?.disconnectDevice(ip, javaClass.name)
        context?.unbindService(connection)
        super.onDestroy()
    }
}
