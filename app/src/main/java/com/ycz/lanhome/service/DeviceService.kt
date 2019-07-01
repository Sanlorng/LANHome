package com.ycz.lanhome.service

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.ycz.lanhome.network.CallbackRest
import com.ycz.lanhome.network.RestService
import com.ycz.lanhome.R
import com.ycz.lanhome.ShellActivity
import com.ycz.lanhome.network.SocketWrapper
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.app.AppDatabase
import com.ycz.lanhome.defaultPreferences
import com.ycz.lanhome.model.Device
import com.ycz.lanhome.model.DeviceBind
import com.ycz.lanhome.model.DeviceData
import com.ycz.lanhome.model.RestResult
import com.ycz.lanhome.toast
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class DeviceService : Service() {
    private val binder = DeviceBinder()
    private var scanJob: Job? = null
    private var socketWrappers = HashMap<String, SocketWrapper>()
    private var needNotifyGuest = true
    private var syncDataType = 1
    private lateinit var guestPictureNotification: NotificationCompat.Builder
    private lateinit var controlNotification: Notification
    private lateinit var alertNotification: NotificationCompat.Builder
    private lateinit var guestIntent: Intent
    lateinit var db: AppDatabase
    private val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            val keyForegroundServiceKeep =
                getString(R.string.key_setting_using_foreground_service_keep_alive)
            val keyNeedGuestNotification =
                getString(R.string.key_setting_receive_guest_notification)
            val keySyncNowType =
                getString(R.string.key_setting_sync_data)
            Log.e("s", key)
            when (key) {
                keyForegroundServiceKeep -> {
                    Log.e(key, sharedPreferences.getBoolean(key, true).toString())
                    val value = sharedPreferences.getBoolean(key, true)
                    if (value)
                        startForeground(
                            AppConfig.KET_CONTROL_NOTIFICATION_CHANNEL_INT,
                            controlNotification
                        )
                    else
                        stopForeground(true)
                }
                keyNeedGuestNotification -> {
                    needNotifyGuest = sharedPreferences.getBoolean(key, true)
                }
                keySyncNowType -> {
                    syncDataType  = (sharedPreferences.getString(key,"1")?:"1").toInt()
                }
            }
        }

    override fun onCreate() {
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "app"
        ).build()
        createNotification()
        controlNotification =
            NotificationCompat.Builder(this, AppConfig.KEY_CONTROL_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_device_hub_black_24dp)
                .setContentTitle("前台服务")
                .setContentText("保持设备连接")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        if (defaultPreferences.getBoolean(
                getString(R.string.key_setting_using_foreground_service_keep_alive),
                true
            )
        ) {
            startForeground(AppConfig.KET_CONTROL_NOTIFICATION_CHANNEL_INT, controlNotification)
        }
        defaultPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        needNotifyGuest = defaultPreferences.getBoolean(
            getString(R.string.key_setting_using_foreground_service_keep_alive),
            true
        )

        guestPictureNotification =
            NotificationCompat.Builder(this, AppConfig.KEY_PICTURE_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_person_outline_black_24dp)
                .setContentTitle("有新访客")
                .setContentText("点击查看访客图片")
                .setAutoCancel(true)
        alertNotification = NotificationCompat.Builder(this, AppConfig.KEY_CONTROL_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_error_outline_black_24dp)
            .setContentTitle("液化气警告")
            .setContentText("保持设备连接")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        binder.scanDevice()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        defaultPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        scanJob?.cancel()
        super.onDestroy()
    }


    private fun createNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        var channel = NotificationChannel(
            AppConfig.KEY_CONTROL_NOTIFICATION_CHANNEL,
            getString(R.string.foreground_service),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "用来保持设备连接"
        manager.createNotificationChannel(channel)
        channel = NotificationChannel(
            AppConfig.KEY_PICTURE_NOTIFICATION_CHANNEL,
            "访客提醒",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "当有访客来时，发出通知"
        manager.createNotificationChannel(channel)
        channel = NotificationChannel(
            "Alert",
            "液化气警告",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "液化气达到一定程度时，发出通知"
        manager.createNotificationChannel(channel)

    }

    inner class DeviceBinder : Binder() {
        lateinit var socket: DatagramSocket
        val devices = HashMap<String, Device>()
        val listeners = HashMap<String, DeviceScanCallback>()
        val socketCallbacks = HashMap<String, HashMap<String, SocketWrapper.Callback>>()

        init {
            GlobalScope.launch {
                socket = DatagramSocket(null)
                socket.reuseAddress = true
                socket.bind(InetSocketAddress(19998))
            }
        }

        fun scanDevice() {
            Log.e("scanStart", "true")
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

                            val device = Device(hostAddress, name)
                            if (devices[hostAddress] == null) {
                                devices[hostAddress] = device
                                withContext(Dispatchers.Main) {
                                    Log.e("ete", "x")
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

        }

        fun syncData() {
            GlobalScope.launch {

            }
        }

        fun connectDevice(
            ip: String,
            tag: String,
            callback: SocketWrapper.Callback
        ): SocketWrapper? {
            if (socketWrappers.containsKey(ip).not()) {
                socketCallbacks[ip] = HashMap()
                socketWrappers[ip] =
                    SocketWrapper(ip, 19999, callback = DeviceSocketCallback(ip))
            }
            socketCallbacks[ip]?.put(tag, callback)
            return socketWrappers[ip]
        }

        fun disconnectDevice(ip: String, tag: String) {
            socketCallbacks[ip]?.remove(tag)
        }

        fun addDeviceScanCallback(tag: String, listener: DeviceScanCallback) {
            listeners[tag] = listener
        }

        fun removeDeviceScanCallback(tag: String) {
            listeners.remove(tag)
        }

        inner class DeviceSocketCallback(val ip: String) : SocketWrapper.Callback {
            override fun onOpen() {
                socketCallbacks[ip]?.values?.forEach {
                    it.onOpen()
                }
            }

            override fun onClose() {
                socketCallbacks[ip]?.values?.forEach {
                    it.onClose()
                }
                socketWrappers.remove(ip)
            }

            override fun onException(t: Throwable?) {
                socketCallbacks[ip]?.values?.forEach {
                    it.onException(t)
                }
            }

            override fun onReceiveMessage(string: String) {
                socketCallbacks[ip]?.values?.forEach {
                    if (string.startsWith(AppConfig.KEY_CONTROL_MAC)) {
                        devices[ip]?.mac = string.substring(16, 16 + 17)
                        RestService.bindDevice(DeviceBind(
                            userId = AppConfig.userId,
                            mac = devices[ip]?.mac?:"",
                            name = devices[ip]?.name?:"",
                            logTime = System.currentTimeMillis()
                        )).enqueue(object: CallbackRest<DeviceBind> {
                            override fun onFailure(
                                call: Call<RestResult<DeviceBind>>,
                                t: Throwable
                            ) {
                                toast("绑定${devices[ip]?.name}失败")
                            }

                            override fun onResponse(
                                call: Call<RestResult<DeviceBind>>,
                                response: Response<RestResult<DeviceBind>>
                            ) {

                            }
                        })
                    } else if (string.startsWith(AppConfig.KEY_CONTROL_COMMAND)) {
                        val length = string.substring(11, 15).toInt()
                        Log.e("xLeng", length.toString())
                        val char = string[16]
                        if (char in setOf('m', 'h', 't') ) {
                            val deviceData = DeviceData(
                                null,
                                System.currentTimeMillis(),
                                devices[ip]?.mac ?: "",
                                data = string.substring(16, 16 + length),
                                userId = AppConfig.userId
                            )
                            if (char == 'm') {
                                val data = string.substring(17,16 + length).toInt()
                                if (data >= 350) {
                                    alertNotification.setContentTitle("${devices[ip]?.name} 液化气指数过高，请及时处理")
                                        .setContentText("指数已经超过了 ${data} ")
                                    getSystemService(NotificationManager::class.java)
                                        .notify(
                                            3,
                                            alertNotification.build()
                                        )
                                }
                            }
                            GlobalScope.launch {
                                db.deviceDataDao().insert(deviceData)
                            }
                            Log.e("syncData",syncDataType.toString())
                            if (syncDataType == 1 && AppConfig.isUserBind(deviceData.mac))
                            RestService.syncItem(deviceData).enqueue(object : CallbackRest<DeviceData> {
                                override fun onFailure(
                                    call: Call<RestResult<DeviceData>>,
                                    t: Throwable
                                ) {
                                    t.printStackTrace()
                                }

                                override fun onResponse(
                                    call: Call<RestResult<DeviceData>>,
                                    response: Response<RestResult<DeviceData>>
                                ) {
                                    if (response.body()?.code == 200) {
                                        deviceData.isSync = true
                                        GlobalScope.launch {
                                            db.deviceDataDao().update(deviceData)
                                        }
                                    }
                                }
                            })
                        } else if (string.substring(16, 22) == "newPic" && needNotifyGuest) {
                            guestIntent = Intent(applicationContext, ShellActivity::class.java)
                            guestIntent.putExtra(AppConfig.KEY_NAVIGATE_ID, R.id.guestPictureFragment)
                            guestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            guestIntent.putExtra(AppConfig.KEY_DEVICE_ADDRESS, ip)
                            guestPictureNotification
                                .setContentTitle("${devices[ip]?.name} 有新访客")
                                .setContentIntent(
                                    PendingIntent.getActivity(
                                        this@DeviceService,
                                        0,
                                        guestIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                )
                            getSystemService(NotificationManager::class.java)
                                .notify(
                                    AppConfig.KEY_PICTURE_NOTIFICATION_CHANNEL_INT,
                                    guestPictureNotification.build()
                                )
                        }
                    }
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
