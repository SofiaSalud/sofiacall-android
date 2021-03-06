package com.sofiasalud.callscreen

interface CallScreenAnalytics {
  fun trackScreenVideoCall()
  fun trackVideoLoad(sessionId: String)
  fun trackVideoToggleCamera(sessionId: String)
  fun trackVideoUpdateCameraEnabled(sessionId: String, cameraEnabled: Boolean)
  fun trackVideoUpdateAudioEnabled(sessionId: String, audioEnabled: Boolean)
  fun trackVideoHangUp(sessionId: String)
  fun trackVideoSessionConnecting(sessionId: String)
  fun trackVideoSessionConnected(sessionId: String)
  fun trackVideoSessionDisconnected(sessionId: String)
  fun trackVideoSessionError(sessionId: String, message: String)
  fun trackVideoStreamCreated(sessionId: String, streamId: String)
  fun trackVideoStreamDestroyed(sessionId: String, streamId: String)
  fun trackVideoPublisherCreated(sessionId: String)
  fun trackVideoPublisherStreamCreated(sessionId: String, streamId: String)
  fun trackVideoPublisherStreamDestroyed(sessionId: String, streamId: String)
  fun trackVideoPublisherError(sessionId: String, streamId: String, message: String)
  fun trackVideoSubscriberSubscribed(sessionId: String, streamId: String, partnerName: String)
  fun trackVideoSubscriberUnsubscribed(sessionId: String, streamId: String)
  fun trackVideoSubscriberConnected(sessionId: String, streamId: String)
  fun trackVideoSubscriberDisconnected(sessionId: String, streamId: String)
  fun trackVideoSubscriberError(sessionId: String, streamId: String, message: String)
}