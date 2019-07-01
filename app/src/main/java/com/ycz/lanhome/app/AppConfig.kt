package com.ycz.lanhome.app

import com.ycz.lanhome.model.Device
import com.ycz.lanhome.model.DeviceBind
import com.ycz.lanhome.model.User

object AppConfig {
    const val KEY_DB_NAME = "app"
    const val KEY_NAVIGATE_ID = "navigateId"
    const val KEY_DEVICE_ADDRESS = "deviceAddress"
    const val KEY_DEVICE_NAME = "deviceName"
    const val KEY_DEVICE_MAC = "deviceMac"
    const val KEY_GET_TYPE = "getType"
    const val KEY_DEVICE = "device"
    const val KEY_CONTROL_NOTIFICATION_CHANNEL = "Control"
    const val KET_CONTROL_NOTIFICATION_CHANNEL_INT = 1
    const val KEY_PICTURE_NOTIFICATION_CHANNEL = "图片"
    const val KEY_PICTURE_NOTIFICATION_CHANNEL_INT = 2
    const val KEY_CONTROL_WINDOW = "ow"
    const val KEY_CONTROL_FAN = "of"
    const val KEY_CONTROL_OPEN_DOOR = "od"
    const val KEY_CONTROL_CLOSE_DOOR = "cd"
    const val KEY_CONTROL_GET_INFO = "getInfo"
    const val KEY_CONTROL_COMMAND = "com"
    const val KEY_CONTROL_VIDEO = "vid"
    const val KEY_CONTROL_PICTURE = "pic"
    const val KEY_CONTROL_MAC = "mac"
    const val KEY_CONTROL_GET_VIDEO = "getVideo"
    val bindList = ArrayList<DeviceBind>()
    fun isUserBind(mac: String):Boolean {
        bindList.forEach {
            if (it.userId == userId && it.mac == mac)
                return true
        }
        return false
    }
    var user: User = User()
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