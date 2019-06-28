package com.ycz.lanhome

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.app.AppUpdateService
import com.ycz.lanhome.model.Device
import com.ycz.lanhome.service.DeviceService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_input_device.view.*
import kotlinx.android.synthetic.main.layout_navigation_header_main.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private var binder: DeviceService.DeviceBinder? = null
    private val list = ArrayList<Device>()
    private val connection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            binder = p1 as DeviceService.DeviceBinder
            binder?.addDeviceScanCallback(javaClass.name, scanCallback)
//            binder?.scanDevice()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            binder?.removeDeviceScanCallback(javaClass.name)
            binder = null
        }
    }

    private val scanCallback = object : DeviceService.DeviceScanCallback {
        override fun onNewDeviceAdded(device: Device) {
            list.add(device)
            listDeviceMain.adapter?.notifyItemInserted(list.lastIndex)
        }

        override fun onNewDeviceScanFinish() {
            swipeLayoutMain.isRefreshing = false
        }

        override fun onNewDeviceScanStart() {
            swipeLayoutMain.isRefreshing = true
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val updateWork =
        WorkManager.getInstance(this)
//            .enqueue()
        val preVersionCode = PreferenceManager.getDefaultSharedPreferences(this).getInt("versionCode",0)
        if (BuildConfig.VERSION_CODE > preVersionCode)
//            startActivity(Intent(this,ShellActivity::class.java).apply {
//                putExtra("navigateId",R.id.setupWizardFragment)
//                putExtra("canFinish",false)
//            })
        if (AppConfig.userId == -1 ) {
//            startActivity(Intent(this,ShellActivity::class.java).apply {
//                putExtra("navigateId",R.id.loginFragment)
//                putExtra("canFinish",false)
//            })
        }
        startService(Intent(this,AppUpdateService::class.java))
        setContentView(R.layout.activity_main)
        listDeviceMain.itemAnimator = DefaultItemAnimator()
        listDeviceMain.adapter = DeviceListAdapter(list)
        window.translucentSystemUI(true)
        setSupportActionBar(toolbarMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toggle = ActionBarDrawerToggle(
            this,layoutDrawerMain,toolbarMain,
            R.string.open_drawer,R.string.close_drawer
        )
        layoutDrawerMain.addDrawerListener(toggle)
        navigationDrawerMain.setNavigationItemSelectedListener {
            startActivity(Intent(this,ShellActivity::class.java).apply {
                putExtra(AppConfig.KEY_NAVIGATE_ID,it.itemId)
            })
            true
        }
        navigationDrawerMain.getHeaderView(0).avatarDrawer.setOnClickListener {
            startActivity(Intent(this,ShellActivity::class.java).apply {
                putExtra(AppConfig.KEY_NAVIGATE_ID,R.id.loginFragment)
            })
        }
        addDevice.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.layout_input_device,null)
            MaterialAlertDialogBuilder(this)
                .setTitle("手动添加设备")
                .setView(view)
                .setCancelable(true)
                .setPositiveButton("添加") { which, i ->
                    val str = view.dialogDeviceIP.editText?.editableText?.trim()?.toString()?:""
                    val name = view.dialogDeviceName.editText?.editableText?.trim()?.toString()?:""
                    val points = str.split(".")
                    if (points.size == 4) {
                        list.add(Device(str,name))
                        listDeviceMain.adapter?.notifyItemInserted(list.lastIndex)
//                        startActivity(Intent(this, ShellActivity::class.java).apply {
//                            putExtra(AppConfig.KEY_NAVIGATE_ID, R.id.deviceDetailsFragment)
//                            putExtra(AppConfig.KEY_DEVICE_ADDRESS, str)
//                        })
                    }
                    else
                        toast("输入的不是ip地址")
                }
                .create()
                .show()
        }
//        val socket = SocketWrapper("192.168.137.1",18000,object : SocketWrapper.Callback {
//            override fun onReceiveMessage(string: String) {
//                Log.e("socket",string)
//            }
//        })
//        socket.sendMessage("nihaohdshdiasdas")
//        socket.sendMessage("djisjdisadiasdjasidjas")
//        socket.sendMessage("xsaijdiosdiadjioadjiojdiajsidoa")
        swipeLayoutMain.setOnRefreshListener {
            list.clear()
            listDeviceMain.adapter?.notifyDataSetChanged()
            binder?.scanDevice()
        }
        toggle.syncState()
        bindService(Intent(this,DeviceService::class.java),connection,Service.BIND_AUTO_CREATE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main_app_bar,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putInt("versionCode",BuildConfig.VERSION_CODE)
        }
        unbindService(connection)
        stopService(Intent(this, DeviceService::class.java))
        super.onDestroy()
    }
}
