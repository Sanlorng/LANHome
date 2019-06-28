package com.ycz.lanhome.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.room.Room
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.app.AppDatabase
import com.ycz.lanhome.Network.RestService
import com.ycz.lanhome.model.RestResult
import com.ycz.lanhome.model.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val db = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,"app"
    ).build()
    val loginStates = MutableLiveData<RestResult<String>>()
    val tabSelectItem = MutableLiveData<Int>().apply {
        value = 0
    }
    val users = MutableLiveData<List<User>>()

    fun getUsers() {
        viewModelScope.launch {
            users.postValue(db.userDao().getAll())
        }
    }
    fun register(user: User) {
        loginStates.postValue(RestResult.loading())
        viewModelScope.launch {
            RestService.userRegister(user).enqueue(object : Callback<RestResult<String>> {
                override fun onFailure(call: Call<RestResult<String>>, t: Throwable) {
                    loginStates.postValue(RestResult.loadFinish())
                    loginStates.postValue(RestResult( message = t.message?:""))
                }

                override fun onResponse(call: Call<RestResult<String>>, response: Response<RestResult<String>>) {
                    loginStates.postValue(RestResult.loadFinish())
                    val body = response.body()
                    loginStates.postValue(RestResult(code = body?.code?:-1,data = body?.data))
                }
            })
        }
    }

    fun login(user: User) {
        loginStates.postValue(RestResult.loading())
        viewModelScope.launch {
            RestService.userRegister(user).enqueue(object : Callback<RestResult<String>> {
                override fun onFailure(call: Call<RestResult<String>>, t: Throwable) {
                    loginStates.postValue(RestResult.loadFinish())
                    loginStates.postValue(RestResult.failed(t.message?:""))
                }

                override fun onResponse(call: Call<RestResult<String>>, response: Response<RestResult<String>>) {
                    loginStates.postValue(RestResult.loadFinish())
                    val body = response.body()
                    loginStates.postValue(RestResult(code = body?.code?:-1,data = body?.data))
                    if (body?.code == 200) {
                        val tempUser = db.userDao().findByUsernameOrPhoneNumber(user.userName, user.phoneNumber)
                        if (tempUser.isNotEmpty()) {
                            val temp = tempUser[0]
                            temp.isLogin = true
                            AppConfig.userId = temp.userId
                            AppConfig.userName = temp.userName
                            AppConfig.userPhone = temp.phoneNumber
                            db.userDao().update(temp)
                        }
                    }
                }
            })
        }
    }
}
