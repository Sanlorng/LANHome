package com.ycz.lanhome.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ycz.lanhome.BuildConfig
import com.ycz.lanhome.network.RestService
import com.ycz.lanhome.R
import com.ycz.lanhome.ShellActivity
import com.ycz.lanhome.model.RestResult
import com.ycz.lanhome.model.UpdateInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppUpdateService : Service() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("startCheckUpdate", "true")
        val info = UpdateInfo(
            packageName = BuildConfig.APPLICATION_ID
        )
        RestService.newestUpdate(info).enqueue(object : Callback<RestResult<UpdateInfo>> {
            override fun onFailure(call: Call<RestResult<UpdateInfo>>, t: Throwable) {

            }

            override fun onResponse(
                call: Call<RestResult<UpdateInfo>>,
                response: Response<RestResult<UpdateInfo>>
            ) {
                if (response.body()?.code == 200) {
                    onUpdateCheckSuccess(response.body()?.data!!)
                }
            }
        })
//        updateImpl.doRequest(packageName)
        return super.onStartCommand(intent, flags, startId)
    }


    fun onUpdateCheckSuccess(updateInfo: UpdateInfo) {
        updateInfo.apply {
            var currentCode = 0L
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                currentCode =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS)
                        .versionCode.toLong()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                currentCode =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS)
                        .longVersionCode
            if (updateInfo.versionCode.toLong() > currentCode) {
                val builder = NotificationCompat.Builder(this@AppUpdateService, "1")
                    .setSmallIcon(R.drawable.ic_update_black_24dp)
                    .setContentTitle("检查到新版本")
                    .setContentText("版本号：$versionName")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("版本号：$versionName\n$changelog")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            this@AppUpdateService,
                            0,
                            Intent(this@AppUpdateService, ShellActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    ).addAction(
                        NotificationCompat.Action.Builder(R.drawable.ic_file_download_black_24dp,
                            "立即下载",
                            PendingIntent.getActivity(
                                this@AppUpdateService,
                                0,
                                Intent(this@AppUpdateService, ShellActivity::class.java).apply {
                                    putExtra(AppConfig.KEY_NAVIGATE_ID, R.id.updateFragment)
                                },
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        ).build()
                    )
                this@AppUpdateService.createNotificationChannel()
                with(NotificationManagerCompat.from(this@AppUpdateService)) {
                    notify(1, builder.build())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "应用更新"
            val descriptionText = "应用更新"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("2", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

        }
    }
}
