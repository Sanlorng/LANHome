package com.ycz.lanhome.network

import android.util.Log
import com.ycz.lanhome.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

typealias CallRest<T> = Call<RestResult<T>>
typealias CallbackRest<T> = Callback<RestResult<T>>

interface RestService {


    @POST("user/register")
    fun userRegister(@Body user: User): Call<RestResult<Int>>

    @POST("user/login")
    fun userLogin(@Body user: User): Call<RestResult<Int>>

    @POST("update/query")
    fun newestUpdate(@Body info: UpdateInfo): Call<RestResult<UpdateInfo>>

    @POST("user/updateInfo")
    fun updateInfo(@Body user: User): Call<RestResult<String>>

    @POST("device/syncItem")
    fun syncItem(@Body deviceData: DeviceData): CallRest<DeviceData>

    @POST("user/getUserInfo")
    fun getUserInfo(@Body user: User): CallRest<User>

    @POST("device/bind")
    fun bindDevice(@Body deviceBind: DeviceBind): CallRest<DeviceBind>

    @POST("device/getBindList")
    fun getBindDevice(@Body user: User) : CallRest<List<DeviceBind>>
    companion object {
        private val instance by lazy(LazyThreadSafetyMode.NONE) {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                    Log.e("RetrofitLog", it)
                }).setLevel(HttpLoggingInterceptor.Level.BODY)).build()
            Retrofit.Builder()
                .baseUrl("") //Server Url
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(RestService::class.java)
        }

        fun userRegister(user: User) = instance.userRegister(user)
        fun userLogin(user: User) = instance.userLogin(user)
        fun newestUpdate(info: UpdateInfo) = instance.newestUpdate(info)
        fun updateInfo(user: User) = instance.updateInfo(user)
        fun syncItem(deviceData: DeviceData) = instance.syncItem(deviceData)
        fun getUserInfo(user: User) = instance.getUserInfo(user)
        fun bindDevice(deviceBind: DeviceBind) = instance.bindDevice(deviceBind)
        fun getBindDevice(user: User) = instance.getBindDevice(user)
    }
}