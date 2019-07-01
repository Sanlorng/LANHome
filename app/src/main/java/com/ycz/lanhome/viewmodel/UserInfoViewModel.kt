package com.ycz.lanhome.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ycz.lanhome.network.CallbackRest
import com.ycz.lanhome.network.RestService
import com.ycz.lanhome.model.RestResult
import com.ycz.lanhome.model.User
import retrofit2.Call
import retrofit2.Response

class UserInfoViewModel : ViewModel() {
    val updateStates = MutableLiveData<RestResult<String>>()
    fun updateInfo(user: User) {
        RestService.updateInfo(user).enqueue(object : CallbackRest<String> {
            override fun onFailure(call: Call<RestResult<String>>, t: Throwable) {
                updateStates.postValue(RestResult.failed())
            }

            override fun onResponse(
                call: Call<RestResult<String>>,
                response: Response<RestResult<String>>
            ) {
                updateStates.postValue(
                    RestResult(
                        code = response.body()?.code ?: -1
                    )
                )
            }
        })
    }
}
