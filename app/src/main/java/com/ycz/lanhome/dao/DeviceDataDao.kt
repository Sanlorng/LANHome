package com.ycz.lanhome.dao

import androidx.room.*
import com.ycz.lanhome.model.DeviceData

@Dao
interface DeviceDataDao {

    @Query("select * from DeviceData where userId == :uid")
    fun findAllNotSyncData(uid: Int):List<DeviceData>
    @Query("select * from DeviceData where device_mac == :mac")
    fun findAllDeviceData(mac:String):List<DeviceData>
    @Insert
    fun insertAll(vararg deviceData: DeviceData)

    @Insert
    fun insert(deviceData: DeviceData)

    @Delete
    fun delete(deviceData: DeviceData)

    @Delete
    fun delete(vararg deviceData: DeviceData)

    @Update
    fun update(deviceData: DeviceData)

    @Update
    fun update(vararg deviceData: DeviceData)
}