package com.ycz.lanhome.app

import com.ycz.lanhome.model.User

object AppConfig {
    const val KEY_NAVIGATE_ID = "navigateId"
    const val KEY_DEVICE_ADDRESS = "deviceAddress"
    const val KEY_DEVICE_NAME = "deviceName"
    const val KEY_DEVICE_MAC = "deviceMac"
    const val KEY_DEVICE = "device"
    const val KEY_CONTROL_NOTIFICATION_CHANNEL = "Control"
    const val KET_CONTROL_NOTIFICATION_CHANNEL_INT = 1
    var user:User = User()
    var userId: Int
    get() {
        return user.userId
    }
    set(value) {
        user.userId = value
    }
    var userName: String
    get() = user.userName
    set(value) {
        user.userName = value
    }
    var userPhone: String
    get() = user.phoneNumber
    set(value) {
        user.phoneNumber = value
    }
}