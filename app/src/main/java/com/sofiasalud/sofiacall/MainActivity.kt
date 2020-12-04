package com.sofiasalud.sofiacall

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import com.sofiasalud.callscreen.CallScreen
import com.sofiasalud.callscreen.VCRoom
import com.sofiasalud.sofiacall.lobby.LobbyScreen
import com.sofiasalud.sofiacall.ui.SofiaCallTheme
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() {
  private var analytics: Analytics? = null
  private val appStore = AppStore()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    analytics = Analytics(applicationContext)
    analytics?.trackAppLoaded()
    val prefs = getPreferences(Context.MODE_PRIVATE)

    setContent {
      MainView(openUrl = ::openUrl, prefs = prefs, analytics = analytics!!, appStore = appStore)
    }
    requestPermissions()
  }

  override fun onPause() {
    super.onPause()
    appStore.onPause()
  }

  override fun onResume() {
    super.onResume()
    appStore.onResume()
  }

  override fun onDestroy() {
    analytics?.onDestroy()
    super.onDestroy()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
  }

  @AfterPermissionGranted(RC_VIDEO_APP_PERM)
  private fun requestPermissions() {
    val perms = arrayOf(
      Manifest.permission.INTERNET,
      Manifest.permission.CAMERA,
      Manifest.permission.RECORD_AUDIO
    )
    if (EasyPermissions.hasPermissions(this, *perms)) {
      // TODO: Do something here
    } else {
      EasyPermissions.requestPermissions(
        this,
        "This app needs access to your camera and mic to make video calls",
        RC_VIDEO_APP_PERM,
        *perms
      )
    }
  }

  fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
  }

  companion object {
    private val LOG_TAG = MainActivity::class.java.simpleName
    private const val RC_SETTINGS_SCREEN_PERM = 123
    private const val RC_VIDEO_APP_PERM = 124
  }
}

@Composable
fun MainView(openUrl: (url: String) -> Unit, prefs: SharedPreferences, analytics: Analytics, appStore: AppStore) {
  val (name, setName) = remember { mutableStateOf(prefs.getString("SOFIA_CALL_NAME", null)) }
  var currentRoom by remember { mutableStateOf<VCRoom?>(null) }
  val (createRoomLoading, setCreateRoomLoading) = remember { mutableStateOf(false) }
  val ctx = ContextAmbient.current
  val roomStore = remember { RoomStore(ctx) }

  SofiaCallTheme {
    Surface(color = MaterialTheme.colors.background) {
      when {
        name == null -> {
          LoginScreen(
            analytics = analytics,
            onSubmit = { newName ->
              with(prefs.edit()) {
                putString("SOFIA_CALL_NAME", newName)
                apply()
              }
              analytics.trackSetUsername(newName)
              setName(newName)
            }
          )
        }

        currentRoom == null -> {
          LobbyScreen(
            analytics = analytics,
            openUrl = openUrl,
            rooms = roomStore.rooms.values.toList(),
            onStartCall = { room ->
              currentRoom = room
              roomStore.joinRoom(room.id, name)
            },
            onDeleteRoom = { room -> roomStore.deleteRoom(room.id) },
            onCreateRoom = { roomName ->
              setCreateRoomLoading(true)
              roomStore.createRoom(roomName, name) {
                setCreateRoomLoading(false)
                // Do we want to put you into the room immediately after you create it?
//                setCurrentRoom(room)
              }
            },
            name = name,
            resetName = {
              with(prefs.edit()) {
                remove("SOFIA_CALL_NAME")
                apply()
              }
              setName(null)
            },
            createRoomLoading = createRoomLoading
          )
        }
        else -> {
          CallScreen(
            analytics = analytics,
            isActivityPaused = appStore.paused,
            name = name,
            room = currentRoom!!,
            debugStats = true,
            onGoBack = {
              val room: VCRoom = currentRoom!!
              roomStore.leaveRoom(room.id, name)
              currentRoom = null
              if (room.feedbackUrl.isNotBlank()) {
                openUrl(room.feedbackUrl)
              }
            }
          )
        }
      }
    }
  }
}
