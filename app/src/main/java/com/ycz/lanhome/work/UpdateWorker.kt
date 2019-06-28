package com.ycz.lanhome.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ycz.lanhome.BuildConfig
import com.ycz.lanhome.Network.RestService
import com.ycz.lanhome.model.RestResult
import com.ycz.lanhome.model.UpdateInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateWorker(appContext: Context, workerParameters: WorkerParameters)
    :Worker(appContext,workerParameters){

    override fun doWork(): Result {
        RestService.newestUpdate(UpdateInfo(
            packageName = BuildConfig.APPLICATION_ID
        )).enqueue(object : Callback<RestResult<UpdateInfo>> {
            override fun onFailure(call: Call<RestResult<UpdateInfo>>, t: Throwable) {

            }

            override fun onResponse(call: Call<RestResult<UpdateInfo>>, response: Response<RestResult<UpdateInfo>>) {
                val updateInfo = response.body()
                if (updateInfo?.code == 200 && updateInfo.data?.versionCode?:0 > BuildConfig.VERSION_CODE) {

                }
            }
        })
        return Result.failure()
    }
}