package com.ycz.lanhome.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey


@Entity
data class User(
    @PrimaryKey
    var userId: Int = -1,
    @ColumnInfo(name = "user_name") var userName: String = "",
    @ColumnInfo(name = "password") var password: String = "",
    @ColumnInfo(name = "phone_number") var phoneNumber: String = "",
    @ColumnInfo(name = "avatar_path") var avatarPath: String = "",
    @ColumnInfo(name = "is_login") var isLogin: Boolean = false,
    @ColumnInfo(name = "user_nick") var userNick: String = "",
    @ColumnInfo(name = "login_time") var loginTime: Long = 0L,
    @ColumnInfo(name = "birthday") var birthday: Long = 0L,
    @ColumnInfo(name = "age") var age: Int = 0,
    @ColumnInfo(name = "signature") var signature: String = "",
    @ColumnInfo(name = "gender") var gender: Int = -1
)

@Entity
data class DeviceData(
    @PrimaryKey
    @ColumnInfo(name = "log_id")
    var logId: Int?,

    @ColumnInfo(name = "log_time")
    val logTime: Long = 0,
    @ColumnInfo(name = "device_mac")
    val mac: String = "",

    @ColumnInfo(name = "device_data")
    val data: String = "",

    @ColumnInfo(name = "data_is_sync")
    var isSync: Boolean = false,

    @ColumnInfo(name = "userId")
    var userId: Int = -1
)