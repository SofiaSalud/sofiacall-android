package com.sofiasalud.sofiacall

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

class AppStore : ViewModel() {
  var paused: Boolean by mutableStateOf(false)
    private set

  fun onPause() {
    paused = true
  }

  fun onResume() {
    paused = false
  }
}