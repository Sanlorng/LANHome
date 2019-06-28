package com.ycz.lanhome.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ShellViewModel(application: Application): AndroidViewModel(application) {
    val canFinish = MutableLiveData<Boolean>()
}