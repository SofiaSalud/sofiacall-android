package com.sofiasalud.callscreen

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ui.tooling.preview.Preview
import com.opentok.android.BaseVideoRenderer
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.roundToInt

data class VCRoom(
  val apiKey: String = "",
  val createdTime: Long = 0L,
  val expireTime: Long = 0L,
  val createdBy: String = "",
  val feedbackUrl: String = "",
  val url: String = "",
  val id: String = "",
  val name: String = "",
  var needsPartner: String? = null,
  val sessionId: String = "",
  val sessionType: String = "",
  val token: String = ""
)

interface AppStore {
  var paused: Boolean
}

@Composable
fun LoadingOverlay() {
  Box(modifier = Modifier.fillMaxSize().background(Color(0x44000000))) {
    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
  }
}

@Composable
fun CallScreen(
  name: String,
  room: VCRoom?,
  onGoBack: () -> Unit,
  analytics: CallScreenAnalytics,
  appStore: AppStore,
) {
  val context = ContextAmbient.current
  remember(room) {
    analytics.trackScreenVideoCall()
    analytics.trackVideoLoad(room?.sessionId ?: "")
  }

  val initCallViewModel = { rm: VCRoom?, audioOnly: Boolean ->
    if (rm == null) {
      null
    } else {
      CallViewModel(context, analytics, audioOnly).also {
        it.init(name = name, apiKey = rm.apiKey, sessionId = rm.sessionId, token = rm.token)
      }
    }
  }
  val (callData, setCallData) = remember { mutableStateOf(Pair(false, 1))}
  val (audioOnly, callId) = callData

  val (callViewModel, setCallViewModel) = remember { mutableStateOf(initCallViewModel(room, false)) }

  DisposableEffect(room, audioOnly, callId) {
    onDispose {
      callViewModel?.end()
      setCallViewModel(initCallViewModel(room, audioOnly))
    }
  }
  val resetCallViewModel = { audio: Boolean ->
    setCallData(Pair(audio, callId + 1))
  }

  val subscribers = callViewModel?.subscribers?.values?.toList()

  remember(appStore.paused) {
    if (appStore.paused) {
      callViewModel?.pause()
    } else {
      callViewModel?.resume()
    }
  }

  CallScreenUI(
    room = room,
    onGoBack = {
      Log.d("CallScreen", "on go back START")
      callViewModel?.end()
      onGoBack()
      Log.d("CallScreen", "on go back END")
    },
    subscriberViews = subscribers?.map {
      {
        val reconnecting = callViewModel.subscribersReconnecting[it.stream.streamId]
          ?: false
        val v = it.view
        if (v is GLSurfaceView) {
          v.setZOrderOnTop(false)
        }

        Box(modifier = Modifier.fillMaxSize()) {
          AndroidView(viewBlock = { v })
          if (reconnecting) LoadingOverlay()
        }
      }
    } ?: listOf(),
//    publisherSize = publisherSize,
    publisherView = {
      callViewModel?.publisher?.let { publisher ->
        val v = publisher.view
        publisher.renderer.setStyle(
          BaseVideoRenderer.STYLE_VIDEO_SCALE,
          BaseVideoRenderer.STYLE_VIDEO_FILL
        )
//        setPublisherSize(Pair(
//          publisher.stream.videoWidth,
//          publisher.stream.videoHeight
//        ))

        if (v is GLSurfaceView) {
          v.setZOrderOnTop(true)
          v.setZOrderMediaOverlay(true)
        }

        Box(modifier = Modifier.fillMaxSize()) {
          AndroidView(viewBlock = { v })
          if (callViewModel.publisherReconnecting) LoadingOverlay()
        }
      }
    },
    subscriberName = subscribers?.joinToString(separator = ", ") { it.stream?.name ?: "" },
    subscriberTitle = room?.name,
    muted = callViewModel?.muted ?: false,
    setMuted = { callViewModel?.setPublisherMuted(it) },
    cameraEnabled = callViewModel?.cameraEnabled ?: true,
    setCameraEnabled = { callViewModel?.setPublisherCameraEnabled(it) },
    cameraFacingFront = callViewModel?.cameraFacingFront ?: true,
    setCameraFacingFront = { callViewModel?.togglePublisherCameraFacingFront() },
    resetCall = resetCallViewModel,
    videoSpeed = callViewModel?.subscriberVideoStats?.average ?: 0f,
    audioSpeed = callViewModel?.subscriberAudioStats?.average ?: 0f,
  )
}

@Composable
fun CallScreenUI(
  room: VCRoom?,
  onGoBack: () -> Unit,
  subscriberViews: List<@Composable ColumnScope.() -> Unit>,
  publisherView: @Composable BoxScope.() -> Unit,
  subscriberName: String?,
  subscriberTitle: String?,
  muted: Boolean,
  setMuted: (Boolean) -> Unit,
  cameraEnabled: Boolean,
  setCameraEnabled: (Boolean) -> Unit,
  cameraFacingFront: Boolean,
  setCameraFacingFront: (Boolean) -> Unit,
  resetCall: (audioOnly: Boolean) -> Unit,
  videoSpeed: Float,
  audioSpeed: Float,
) {
  val (resetMenuExpanded, setResetMenuExpanded) = remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.primary)) {
    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
      Column(modifier = Modifier.matchParentSize().background(Color.Transparent)) {
        subscriberViews.map {
          Column(modifier = Modifier.fillMaxSize()) {
            // Comment Bug START
            it()
            // Comment Bug END
          }
        }
      }
      Card(
        modifier = Modifier
//          .width(publisherWidth.dp)
//          .height(publisherHeight.dp)
          .width(140.dp)
          .height(180.dp)
          .padding(24.dp)
          .align(Alignment.TopEnd),
        backgroundColor = Color.Black,
        shape = MaterialTheme.shapes.large,
        elevation = 4.dp
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Comment Bug START
          publisherView()
          // Comment Bug END
        }
      }
      Column(
        modifier = Modifier.padding(24.dp).align(Alignment.TopStart)
      ) {
        DropdownMenu(
          toggle = {
            CallButton(
              onClick = { setResetMenuExpanded(true) },
              active = false,
              activeIcon = R.drawable.ic_restart,
              inactiveIcon = R.drawable.ic_restart,
            )
          },
          expanded = resetMenuExpanded,
          onDismissRequest = { setResetMenuExpanded(false) }
        ) {
          DropdownMenuItem(onClick = {
            resetCall(false)
            setResetMenuExpanded(false)
          }) {
            Text("Restart call")
          }
          DropdownMenuItem(onClick = {
            resetCall(true)
            setResetMenuExpanded(false)
          }) {
            Text("Restart without video")
          }
        }
        Column(
          modifier = Modifier.padding(top = 8.dp).background(Color(0x44000000)).padding(4.dp)
        ) {
          Text(
            "V: ${(videoSpeed * 0.008f).toInt()} kb/s",
            color = Color(0x99FFFFFF),
            fontSize = 10.sp
          )
          Text(
            "A: ${(audioSpeed * 0.008f).toInt()} kb/s",
            color = Color(0x99FFFFFF),
            fontSize = 10.sp
          )
        }
      }
      Row(
        modifier = Modifier
          .padding(vertical = 16.dp, horizontal = 28.dp)
          .fillMaxWidth()
          .align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        CallButton(
          onClick = { setCameraFacingFront(!cameraFacingFront) },
          active = !cameraFacingFront,
          activeIcon = R.drawable.ic_cameraflip,
          inactiveIcon = R.drawable.ic_cameraflip,
        )
        CallButton(
          onClick = { setCameraEnabled(!cameraEnabled) },
          active = !cameraEnabled,
          activeIcon = R.drawable.ic_cameraoff,
          inactiveIcon = R.drawable.ic_cameraon,
        )
        CallButton(
          onClick = { setMuted(!muted) },
          active = muted,
          activeIcon = R.drawable.ic_mute,
          inactiveIcon = R.drawable.ic_unmute,
        )
      }
    }
    Row(
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 24.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
        Text(subscriberName ?: "", color = Color.White, style = MaterialTheme.typography.body1)
        Text(subscriberTitle ?: "", color = Color.White, style = MaterialTheme.typography.body2)
      }
      FloatingActionButton(
        onClick = onGoBack,
        modifier = Modifier.size(width = 44.dp, height = 44.dp),
        backgroundColor = Color.Red,
        elevation = FloatingActionButtonConstants.defaultElevation(0.dp, 0.dp)
      ) {
        Icon(vectorResource(R.drawable.ic_hangup), tint = Color.White)
      }
    }
  }
}

@Preview
@Composable
fun PreviewCallScreenUI() {
  CallScreenUI(
    room = VCRoom(
      apiKey = "12345",
      createdTime = 1600000003L,
      expireTime = 1600000004L,
      createdBy = "Dracula",
      id = "abcde",
      name = "Vampires ONLY",
      sessionId = "11122233344445555",
      sessionType = "routed",
      token = "xyzzz",
      url = "",
      feedbackUrl = ""
    ),
    onGoBack = {},
    subscriberViews = listOf({}),
    publisherView = {},
    subscriberName = "Dr. Acula",
    subscriberTitle = "Hematology",
    muted = false,
    setMuted = {},
    cameraEnabled = true,
    setCameraEnabled = {},
    cameraFacingFront = true,
    setCameraFacingFront = {},
    resetCall = {},
    videoSpeed = 0f,
    audioSpeed = 0f,
  )
}
