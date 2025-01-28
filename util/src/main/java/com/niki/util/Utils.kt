package com.niki.util

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 确保设置的值是非空并且不同的
 */
suspend fun <T> MutableLiveData<T?>.checkAndSetS(value: T?) = withContext(Dispatchers.Main) {
    if (value != this@checkAndSetS.value && value != null)
        this@checkAndSetS.value = value
}