package com.ycz.lanhome.model

data class DeviceBind(
    var userId: Int = -1,
    val logTime: Long = 0,
    val mac:String = "",
    val name: String = ""
)