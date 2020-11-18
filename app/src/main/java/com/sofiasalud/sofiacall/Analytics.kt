package com.sofiasalud.sofiacall

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.sofiasalud.callscreen.CallScreenAnalytics

class Analytics(context: Context) : CallScreenAnalytics {
  private val mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN)

  fun trackAppLoaded() {
    mixpanel.trackMap(
      "App Loaded",
      mapOf(
        "gitHash" to BuildConfig.GIT_HASH,
        "gitCount" to BuildConfig.GIT_COUNT,
        "gitBranch" to BuildConfig.GIT_BRANCH
      )
    )
  }

  // NAVIGATION EVENTS

  fun trackScreenUsername() {
    mixpanel.track("Screen - Username")
  }

  fun trackScreenWaitingRoom() {
    mixpanel.track("Screen - Waiting Room")
  }

  override fun trackScreenVideoCall() {
    mixpanel.track("Screen - Video Call")
  }

  fun trackSetUsername(username: String) {
    mixpanel.trackMap("Set Username", mapOf("username" to username))
  }

  // VIDEO EVENTS

  override fun trackVideoLoad(sessionId: String) =
    mixpanel.trackMap("Video - Load", mapOf("sessionId" to sessionId))

  override fun trackVideoToggleCamera(sessionId: String) =
    mixpanel.trackMap("Video - Toggle Camera", mapOf("sessionId" to sessionId))

  override fun trackVideoUpdateCameraEnabled(sessionId: String, cameraEnabled: Boolean) =
    mixpanel.trackMap(
      "Video - Update Camera Enabled",
      mapOf("sessionId" to sessionId, "cameraEnabled" to cameraEnabled)
    )

  override fun trackVideoUpdateAudioEnabled(sessionId: String, audioEnabled: Boolean) =
    mixpanel.trackMap(
      "Video - Update Audio Enabled",
      mapOf("sessionId" to sessionId, "audioEnabled" to audioEnabled)
    )

  override fun trackVideoHangUp(sessionId: String) =
    mixpanel.trackMap("Video - Hang Up", mapOf("sessionId" to sessionId))

  // Video sessions

  override fun trackVideoSessionConnecting(sessionId: String) =
    mixpanel.trackMap("Video - Session Connecting", mapOf("sessionId" to sessionId))

  override fun trackVideoSessionConnected(sessionId: String) =
    mixpanel.trackMap("Video - Session Connected", mapOf("sessionId" to sessionId))

  override fun trackVideoSessionDisconnected(sessionId: String) =
    mixpanel.trackMap("Video - Session Disconnected", mapOf("sessionId" to sessionId))

  override fun trackVideoSessionError(sessionId: String, message: String) =
    mixpanel.trackMap(
      "Video - Session Error",
      mapOf("sessionId" to sessionId, "message" to message)
    )

  // Video streams

  override fun trackVideoStreamCreated(sessionId: String, streamId: String) =
    mixpanel.trackMap(
      "Video - Stream Created",
      mapOf("sessionId" to sessionId, "streamId" to streamId)
    )

  override fun trackVideoStreamDestroyed(sessionId: String, streamId: String) =
    mixpanel.trackMap(
      "Video - Stream Destroyed",
      mapOf("sessionId" to sessionId, "streamId" to streamId)
    )

  // Video publisher

  override fun trackVideoPublisherCreated(sessionId: String) =
    mixpanel.trackMap("Video - Publisher Created", mapOf("sessionId" to sessionId))

  override fun trackVideoPublisherStreamCreated(sessionId: String, streamId: String) =
    mixpanel.trackMap(
      "Video - Publisher Stream Created",
      mapOf("sessionId" to sessionId, "streamId" to streamId)
    )

  override fun trackVideoPublisherStreamDestroyed(sessionId: String, streamId: String) =
    mixpanel.trackMap(
      "Video - Publisher Stream Destroyed",
      mapOf("sessionId" to sessionId, "streamId" to streamId)
    )

  override fun trackVideoPublisherError(sessionId: String, streamId: String, message: String) =
    mixpanel.trackMap(
      "Video - Publisher Error",
      mapOf("sessionId" to sessionId, "streamId" to streamId, "message" to message)
    )

  // Video subscriber

  override fun trackVideoSubscriberSubscribed(sessionId: String, streamId: String, partnerName: String) =
    mixpanel.trackMap(
      "Video - Subscriber Subscribed",
      mapOf("sessionId" to sessionId, "streamId" to streamId, "partnerName" to partnerName)
    )

  override fun trackVideoSubscriberUnsubscribed(sessionId: String, streamId: String) =
    mixpanel.trackMap(
      "Video - Subscriber Unsubscribed",
      mapOf("sessionId" to sessionId, "streamId" to streamId)
    )

  override fun trackVideoSubscriberConnected(sessionId: String, streamId: String) =
    mixpanel.trackMap(
      "Video - Subscriber Connected",
      mapOf("sessionId" to sessionId, "streamId" to streamId)
    )

  override fun trackVideoSubscriberDisconnected(sessionId: String, streamId: String) =
    mixpanel.trackMap(
      "Video - Subscriber Disconnected",
      mapOf("sessionId" to sessionId, "streamId" to streamId)
    )

  override fun trackVideoSubscriberError(sessionId: String, streamId: String, message: String) =
    mixpanel.trackMap(
      "Video - Subscriber Error",
      mapOf("sessionId" to sessionId, "streamId" to streamId, "message" to message)
    )

  fun onDestroy() {
    mixpanel.flush()
  }
}