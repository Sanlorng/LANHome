package com.ycz.lanhome.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey


@Entity
data class User(
    @PrimaryKey
    var userId:Int = -1,
    @ColumnInfo(name = "user_name") var userName:String = "",
    @ColumnInfo(name = "password") var password:String = "",
    @ColumnInfo(name = "phone_number") var phoneNumber:String = "",
    @ColumnInfo(name = "is_login") var isLogin:Boolean = false
)