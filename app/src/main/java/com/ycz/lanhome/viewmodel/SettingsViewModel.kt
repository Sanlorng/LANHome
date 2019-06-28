package com.ycz.lanhome.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ycz.lanhome.BuildConfig
import com.ycz.lanhome.Network.RestService
import com.ycz.lanhome.model.RestResult
import com.ycz.lanhome.model.UpdateInfo
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val updateInfo = MutableLiveData<UpdateInfo>()
    val updateState = MutableLiveData<RestResult<String>>()

    fun getAppUpdate() {
        viewModelScope.launch {
            RestService.newestUpdate(
                UpdateInfo(
                packageName = BuildConfig.APPLICATION_ID
            )).enqueue(object : Callback<RestResult<UpdateInfo>> {
                override fun onFailure(call: Call<RestResult<UpdateInfo>>, t: Throwable) {
                    updateState.postValue(RestResult.failed(t.message?:""))
                }

                override fun onResponse(
                    call: Call<RestResult<UpdateInfo>>,
                    response: Response<RestResult<UpdateInfo>>
                ) {
                    val info = response.body()
                    if (info?.code == 200) {
                        updateState.postValue(RestResult.success(""))
                        updateInfo.postValue(info.data)
                    }else {
                        updateState.postValue(RestResult.failed((info?.code?:-1).toString()))
                    }
                }
            })
        }
    }
}
