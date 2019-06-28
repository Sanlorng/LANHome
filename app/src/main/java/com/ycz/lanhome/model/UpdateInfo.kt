package com.ycz.lanhome.model

data class UpdateInfo(

    var updateId: Int? = 1,

    val packageName: String = "",

    val versionCode: Int = 0,

    var versionName: String = "",

    var changelog: String = "",

    var path: String = "",

    var minSDK: Int = 0,

    var targetSDK: Int = 0,

    var label: String = ""
)