package com.ycz.lanhome

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.app.AppUpdateService
import com.ycz.lanhome.model.Device
import com.ycz.lanhome.model.User
import com.ycz.lanhome.service.DeviceService
import com.ycz.lanhome.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_input_device.view.*
import kotlinx.android.synthetic.main.layout_navigation_header_main.*
import kotlinx.android.synthetic.main.layout_navigation_header_main.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private var binder: DeviceService.DeviceBinder? = null
    private val list = ArrayList<Device>()
    private lateinit var loginViewModel: LoginViewModel
    private val connection = object : ServiceConnection {
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
            if (listDeviceMain.adapter == null) {
                listDeviceMain.adapter = DeviceListAdapter(list)
            }
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
//        val updateWork =
//            WorkManager.getInstance(this)
        loginViewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]
//            .enqueue()
        val preVersionCode =
            PreferenceManager.getDefaultSharedPreferences(this).getInt("versionCode", 0)
        if (BuildConfig.VERSION_CODE > preVersionCode)
//            startActivity(Intent(this,ShellActivity::class.java).apply {
//                putExtra("navigateId",R.id.setupWizardFragment)
//                putExtra("canFinish",false)
//            })
            if (AppConfig.userId == -1) {
                loginViewModel.loginUser.observe(this, Observer {
                    AppConfig.user = it
                    loginViewModel.getUserInfo(it)

                })
                loginViewModel.userInfo.observe(this, Observer {
                    if (it.code == 200) {
                        val user = AppConfig.user
                        val temp = it.data!!
                        user.age = temp.age
                        user.avatarPath = temp.avatarPath
                        user.birthday = temp.birthday
                        user.gender = temp.gender
                        user.signature = temp.signature
                        user.userNick = temp.userNick
                        user.phoneNumber = temp.phoneNumber
                        user.userName = temp.userName
//                        if (user.avatarPath.isEmpty().not())
//                            Glide.with(this)
//                                .load(user.avatarPath)
//                                .into(avatarDrawer)
                    }
                })
                loginViewModel.autoLogin()
            }

        startService(Intent(this, AppUpdateService::class.java))
        setContentView(R.layout.activity_main)
        listDeviceMain.itemAnimator = DefaultItemAnimator()
        listDeviceMain.adapter = DeviceListAdapter(list)
        window.translucentSystemUI(true)
        setSupportActionBar(toolbarMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toggle = ActionBarDrawerToggle(
            this, layoutDrawerMain, toolbarMain,
            R.string.open_drawer, R.string.close_drawer
        )
        layoutDrawerMain.addDrawerListener(toggle)
        navigationDrawerMain.setNavigationItemSelectedListener {
            startActivity(Intent(this, ShellActivity::class.java).apply {
                putExtra(AppConfig.KEY_NAVIGATE_ID, it.itemId)
            })
            true
        }
//        navigationDrawerMain.getHeaderView(0).setOnClickListener {
//            if (AppConfig.userId == -1)
//                startActivity(Intent(this, ShellActivity::class.java).apply {
//                    putExtra(AppConfig.KEY_NAVIGATE_ID, R.id.loginFragment)
//                })
//        }
        addDevice.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.layout_input_device, null,false)
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.manumal_add_device))
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.add)) { which, i ->
                    val str = view.dialogDeviceIP.editText?.editableText?.trim()?.toString() ?: ""
                    val name =
                        view.dialogDeviceName.editText?.editableText?.trim()?.toString() ?: ""
                    val points = str.split(".")
                    if (points.size == 4) {
                        list.add(Device(str, name))
                        listDeviceMain.adapter?.notifyItemInserted(list.lastIndex)
                    } else
                        toast(getString(R.string.input_not_a_ip_address))
                }
                .create()
                .show()
        }
        swipeLayoutMain.setOnRefreshListener {
            list.clear()
            listDeviceMain.adapter?.notifyDataSetChanged()
            binder?.scanDevice()
        }
        navigationDrawerMain.getHeaderView(0).loginButton.setOnClickListener {
            if (AppConfig.userId == -1)
                startActivity(Intent(this, ShellActivity::class.java).apply {
                    putExtra(AppConfig.KEY_NAVIGATE_ID, R.id.loginFragment)
                })
            else {
                AppConfig.user = User()
                toast("已退出登录")
                switchLoginStatus()
            }

        }
        toggle.syncState()
        bindService(Intent(this, DeviceService::class.java), connection, Service.BIND_AUTO_CREATE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main_app_bar,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        switchLoginStatus()
    }

    private fun switchLoginStatus() {
        navigationDrawerMain.getHeaderView(0).apply {
            if (AppConfig.userId > 0) {
                userNameDrawer.text = AppConfig.userName
                loginButton.text = "退出登录"
                loginButton.setTextColor(Color.parseColor("#E53935"))
//                loginButton.isVisible = false
            } else {
                userNameDrawer.text = "尚未登陆"
//                loginButton.isVisible = true
                loginButton.text = "登录/注册"
                val type = TypedValue()
                theme.resolveAttribute(R.attr.colorAccent,type,true)
                loginButton.setTextColor(type.data)
            }
        }
    }

    override fun onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putInt("versionCode", BuildConfig.VERSION_CODE)
        }
        unbindService(connection)
        stopService(Intent(this, DeviceService::class.java))
        super.onDestroy()
    }
}
