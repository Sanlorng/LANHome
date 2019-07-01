package com.ycz.lanhome.model

data class Device(
    val ip: String,
    val name: String,
    var mac: String = ""
)