package com.sofiasalud.sofiacall

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

class AppStore : ViewModel(), com.sofiasalud.callscreen.AppStore {
  override var paused: Boolean by mutableStateOf(false)

  fun onPause() {
    paused = true
  }

  fun onResume() {
    paused = false
  }
}