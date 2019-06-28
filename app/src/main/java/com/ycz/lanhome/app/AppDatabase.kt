package com.ycz.lanhome.app

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ycz.lanhome.dao.UserDao
import com.ycz.lanhome.model.User

@Database(entities = [User::class],version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}