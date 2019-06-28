package com.ycz.lanhome.dao

import androidx.room.*
import com.ycz.lanhome.model.User
import retrofit2.http.DELETE


@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE is_login = :login LIMIT 1")
    fun findByIsLogin(login: Boolean):User

    @Query("SELECT * FROM user WHERE user_name LIKE :name OR phone_number LIKE :number")
    fun findByUsernameOrPhoneNumber(name:String, number: String):List<User>

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)

    @Update
    fun update(user: User)

    @Update
    fun updateAll(vararg users: User)


}