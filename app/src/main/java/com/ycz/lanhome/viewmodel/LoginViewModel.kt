package com.ycz.lanhome.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.room.Room
import com.ycz.lanhome.network.CallbackRest
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.app.AppDatabase
import com.ycz.lanhome.model.DeviceBind
import com.ycz.lanhome.network.RestService
import com.ycz.lanhome.model.RestResult
import com.ycz.lanhome.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val db = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java, AppConfig.KEY_DB_NAME
    ).build()
    val loginStates = MutableLiveData<RestResult<Int>>()
    val tabSelectItem = MutableLiveData<Int>().apply {
        value = 0
    }
    val users = MutableLiveData<List<User>>()
    val loginUser = MutableLiveData<User>()
    val userInfo = MutableLiveData<RestResult<User>>()
    fun getUsers() {
        viewModelScope.launch {
            users.postValue(db.userDao().getAll())
        }
    }

    fun register(user: User) {
        loginStates.postValue(RestResult.loading())
        viewModelScope.launch {
            RestService.userRegister(user).enqueue(object : Callback<RestResult<Int>> {
                override fun onFailure(call: Call<RestResult<Int>>, t: Throwable) {
                    loginStates.postValue(RestResult.loadFinish())
                    loginStates.postValue(RestResult(message = t.message ?: ""))
                }

                override fun onResponse(
                    call: Call<RestResult<Int>>,
                    response: Response<RestResult<Int>>
                ) {
                    loginStates.postValue(RestResult.loadFinish())
                    val body = response.body()
                    loginStates.postValue(RestResult(code = body?.code ?: -1, data = -1))
                    if (body?.code == 200)
                        loginUser.postValue(user.apply {
                            userId = body.data!!
                        })
                }
            })
        }
    }

    fun getUserInfo(user: User) {
        var tempUser = User(userId = user.userId)
        viewModelScope.launch {
            RestService.getUserInfo(tempUser).enqueue(object : CallbackRest<User> {
                override fun onFailure(call: Call<RestResult<User>>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<RestResult<User>>,
                    response: Response<RestResult<User>>
                ) {
                    userInfo.postValue(response.body() ?: RestResult.failed())
                }
            })
        }
    }

    fun autoLogin() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val user = db.userDao().findByIsLogin(true)
                if (user != null)
                    login(user)
            }
        }
    }

    fun login(user: User) {
        loginStates.postValue(RestResult.loading())
        viewModelScope.launch {
            RestService.userLogin(user).enqueue(object : Callback<RestResult<Int>> {
                override fun onFailure(call: Call<RestResult<Int>>, t: Throwable) {
                    loginStates.postValue(RestResult.loadFinish())
                    loginStates.postValue(RestResult.failed(t.message ?: ""))
                }

                override fun onResponse(
                    call: Call<RestResult<Int>>,
                    response: Response<RestResult<Int>>
                ) {
                    loginStates.postValue(RestResult.loadFinish())
                    val body = response.body()
                    loginStates.postValue(RestResult(code = body?.code ?: -1, data = body?.data))
                    if (body?.code == 200) {
                        user.userId = body.data!!
                        loginUser.postValue(user)
                        launch(Dispatchers.IO) {
                            val tempUser = db.userDao()
                                .findByUsernameOrPhoneNumber(user.userName, user.phoneNumber)
                            if (tempUser.isNotEmpty()) {
                                val temp = tempUser[0]
                                temp.isLogin = true
                                AppConfig.userId = body.data!!
                                AppConfig.userName = temp.userName
                                AppConfig.userPhone = temp.phoneNumber
                                db.userDao().update(temp)
                            }
                        }
                        AppConfig.bindList.clear()
                        RestService.getBindDevice(user).enqueue(object : CallbackRest<List<DeviceBind>> {
                            override fun onResponse(
                                call: Call<RestResult<List<DeviceBind>>>,
                                response: Response<RestResult<List<DeviceBind>>>
                            ) {
                                if (response.body()?.code == 200) {
                                    response.body()?.data?.forEach {
                                        AppConfig.bindList.add(it)
                                    }
                                }
                            }

                            override fun onFailure(
                                call: Call<RestResult<List<DeviceBind>>>,
                                t: Throwable
                            ) {

                            }
                        })
                    }
                }
            })
        }
    }
}
