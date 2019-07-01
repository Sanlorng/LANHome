package com.ycz.lanhome.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ycz.lanhome.BuildConfig
import com.ycz.lanhome.network.RestService
import com.ycz.lanhome.model.RestResult
import com.ycz.lanhome.model.UpdateInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateViewModel : ViewModel() {
    val updateInfo = MutableLiveData<RestResult<UpdateInfo>>()
    fun getAppUpdate() {
        RestService.newestUpdate(
            UpdateInfo(
                packageName = BuildConfig.APPLICATION_ID
            )
        ).enqueue(object : Callback<RestResult<UpdateInfo>> {
            override fun onFailure(call: Call<RestResult<UpdateInfo>>, t: Throwable) {
                updateInfo.postValue(RestResult.failed(t.message ?: ""))
            }

            override fun onResponse(
                call: Call<RestResult<UpdateInfo>>,
                response: Response<RestResult<UpdateInfo>>
            ) {
                updateInfo.postValue(response.body() ?: RestResult.failed())
            }
        })
    }
}
