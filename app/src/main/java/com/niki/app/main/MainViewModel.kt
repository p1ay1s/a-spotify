package com.niki.app.main

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var authClintIsRunning = false

    var allowAutoSetProgress = true

    var notedProgress = 0

    var lastBackPressedTimeSet = -1L
}