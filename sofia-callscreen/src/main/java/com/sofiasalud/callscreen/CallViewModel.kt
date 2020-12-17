package com.sofiasalud.callscreen

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.opentok.android.*
import java.util.*

fun OpentokError.didConnectionFail(): Boolean {
  return when (this.errorCode) {
    OpentokError.ErrorCode.ConnectionFailed -> true
    OpentokError.ErrorCode.ConnectionRefused -> true
    OpentokError.ErrorCode.ConnectionTimedOut -> true
    else -> false
  }
}

fun randomStr(length: Int): String {
  val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
  return (1..length)
          .map { allowedChars.random() }
          .joinToString("")
}

class CallViewModel(private val context: Context, private val analytics: CallScreenAnalytics?, private val audioOnly: Boolean = false) : ViewModel() {
  companion object {
    const val NETWORK_STAT_WINDOW = 5.0
    const val STAT_TIER_POOR = 50
    const val STAT_TIER_ACCEPTABLE = 150
    const val STAT_TIER_GOOD = 300
    fun log(message: String) = Log.d("CallViewModel", message)
  }

  // This model ID is just for debugging purposes.
  val modelId: String = randomStr(6)
  private var session: Session? by mutableStateOf(null)
  private var publisherName: String? = null

  var publisher: Publisher? by mutableStateOf(null)
    private set
  var publisherReconnecting: Boolean by mutableStateOf(false)
    private set

  var subscribers: MutableMap<String, Subscriber> = mutableStateMapOf()
    private set
  var subscribersReconnecting: MutableMap<String, Boolean> = mutableStateMapOf()
  val subscriberVideoStats = StatQueue(NETWORK_STAT_WINDOW)
  val subscriberAudioStats = StatQueue(NETWORK_STAT_WINDOW)

  var muted: Boolean by mutableStateOf(false)
    private set
  var cameraEnabled: Boolean by mutableStateOf(!audioOnly)
    private set
  var cameraFacingFront: Boolean by mutableStateOf(true)
    private set

  fun init(name: String, apiKey: String, sessionId: String, token: String) {
    log("Init $modelId ||| $name ||| $apiKey ||| $sessionId ||| $token")
    publisherName = name
    session =
      Session.Builder(context, apiKey, sessionId).sessionOptions(object : Session.SessionOptions() {
        override fun useTextureViews(): Boolean {
          // OpenTok can use either TextureViews or GLSurfaceViews. GLSurfaceViews are more performant,
          // but getting them to layout properly is difficult as they use a lower-level layout system.
          // TextureViews behave like any ol' Android view.
          return true
        }
      })
        .build()
    session?.connect(token)
    session?.setSessionListener(sessionListener)
    analytics?.trackVideoSessionConnecting(sessionId)
  }

  fun end() {
    log("End...")
    analytics?.trackVideoHangUp(session?.sessionId ?: "")

    session?.setSessionListener(null)
    session?.unpublish(publisher)
    publisher?.setPublisherListener(null)
    publisher?.destroy()

    for ((_, subscriber) in subscribers) {
      subscriber.setStreamListener(null)
      subscriber.setSubscriberListener(null)
      subscriber.setVideoStatsListener(null)
      subscriber.setAudioStatsListener(null)

      session?.unsubscribe(subscriber)
      subscriber.destroy()
    }
    subscriberAudioStats.clear()
    subscriberVideoStats.clear()

    subscribers = mutableStateMapOf()
    subscribersReconnecting = mutableStateMapOf()

    session?.disconnect()
    log("End end...")
  }

  fun pause() {
    session?.onPause()
  }

  fun resume() {
    session?.onResume()
  }

  fun setPublisherMuted(value: Boolean) {
    analytics?.trackVideoUpdateAudioEnabled(session?.sessionId ?: "", !value)
    muted = value
    publisher?.publishAudio = !value
  }

  fun setPublisherCameraEnabled(value: Boolean) {
    analytics?.trackVideoUpdateCameraEnabled(session?.sessionId ?: "", value)
    cameraEnabled = value
    publisher?.publishVideo = value
  }

  fun togglePublisherCameraFacingFront() {
    analytics?.trackVideoToggleCamera(session?.sessionId ?: "")
    cameraFacingFront = !cameraFacingFront
    publisher?.cycleCamera()
  }

  /**
   * SESSION LISTENER METHODS
   */

  private val sessionListener = object : Session.SessionListener, Session.ReconnectionListener {
    override fun onConnected(session: Session?) {
      log("onConnected")
      analytics?.trackVideoSessionConnected(session?.sessionId ?: "")
      publisher = Publisher
        .Builder(context)
        // Android is having audio problems so setting the default to LOW as a temporary measure
        .resolution(Publisher.CameraCaptureResolution.LOW)
        .name(publisherName ?: "")
        .build()
        .also {
          it.publishVideo = !audioOnly && cameraEnabled
          it.publishAudio = !muted
          if (!cameraFacingFront) it.cycleCamera()

          analytics?.trackVideoPublisherCreated(session?.sessionId ?: "")
          it.setPublisherListener(publisherListener)
          log("About to publish...")
          session?.publish(it)
        }
    }

    override fun onDisconnected(session: Session?) {
      analytics?.trackVideoSessionDisconnected(session?.sessionId ?: "")
      Log.i("CallViewModel", "Session Disconnected")
    }

    override fun onStreamReceived(session: Session?, stream: Stream?) {
      log("onStreamReceived ${stream?.streamId}")
      analytics?.trackVideoStreamCreated(session?.sessionId ?: "", stream?.streamId ?: "")
      val id = stream?.streamId ?: ""
      subscribers[id] = Subscriber.Builder(context, stream).build().also {
        analytics?.trackVideoSubscriberSubscribed(
          session?.sessionId ?: "",
          stream?.streamId ?: "",
          stream?.name ?: ""
        )
        it.subscribeToVideo = !audioOnly
        it.setStreamListener(streamListener)
        it.setSubscriberListener(subscriberListener)
        it.setVideoStatsListener(subscriberListener)
        it.setAudioStatsListener(subscriberListener)
        log("About to subscribe...")
        session?.subscribe(it)
      }
      subscribersReconnecting[id] = false
    }

    override fun onStreamDropped(session: Session?, stream: Stream?) {
      log("onStreamDropped")
      analytics?.trackVideoStreamDestroyed(session?.sessionId ?: "", stream?.streamId ?: "")
      val id = stream?.streamId ?: ""
      if (subscribers.containsKey(id)) {
        session?.unsubscribe(subscribers[id])
        analytics?.trackVideoSubscriberUnsubscribed(
          session?.sessionId ?: "",
          stream?.streamId ?: ""
        )
        subscribers.remove(id)
        subscribersReconnecting.remove(id)
      }
    }

    override fun onReconnected(session: Session?) {
      publisherReconnecting = false
    }

    // Called when the user has lost connection to the Opentok session. If the user is able to reconnect,
    // onReconnected is called. Otherwise, onDisconnected is called.
    override fun onReconnecting(session: Session?) {
      publisherReconnecting = true
    }

    override fun onError(session: Session?, opentokError: OpentokError?) {
      analytics?.trackVideoSessionError(session?.sessionId ?: "", opentokError?.message ?: "")
      Log.e("CallViewModel", "Session error: " + opentokError?.message)
    }
  }

  /**
   * PUBLISHER LISTENER METHODS
   */

  private val publisherListener = object : PublisherKit.PublisherListener {
    override fun onStreamCreated(publisherKit: PublisherKit?, stream: Stream?) {
      log("publisher stream created")
      analytics?.trackVideoPublisherStreamCreated(
        publisherKit?.session?.sessionId ?: "",
        stream?.streamId ?: ""
      )
    }

    override fun onStreamDestroyed(publisherKit: PublisherKit?, stream: Stream?) {
      log("publisher stream destroyed")
      analytics?.trackVideoPublisherStreamDestroyed(
        publisherKit?.session?.sessionId ?: "",
        stream?.streamId ?: ""
      )
    }

    override fun onError(publisherKit: PublisherKit?, opentokError: OpentokError?) {
      Log.e("CallViewModel", "Publisher error: " + opentokError?.message)
      analytics?.trackVideoPublisherError(
        publisherKit?.session?.sessionId ?: "",
        publisherKit?.stream?.streamId ?: "",
        opentokError?.message ?: ""
      )
    }
  }

  /**
   * SUBSCRIBER LISTENER METHODS
   */

  private val subscriberListener = object : SubscriberKit.SubscriberListener, SubscriberKit.VideoStatsListener, SubscriberKit.AudioStatsListener {
    override fun onConnected(subscriberKit: SubscriberKit?) {
      log("subscriber connected")
      analytics?.trackVideoSubscriberConnected(
        subscriberKit?.session?.sessionId ?: "",
        subscriberKit?.stream?.streamId ?: ""
      )
    }

    override fun onDisconnected(subscriberKit: SubscriberKit?) {
      log("subscriber disconnected")
      analytics?.trackVideoSubscriberDisconnected(
        subscriberKit?.session?.sessionId ?: "",
        subscriberKit?.stream?.streamId ?: ""
      )
    }

    override fun onVideoStats(subscriberKit: SubscriberKit?, stats: SubscriberKit.SubscriberVideoStats?) {
      val stat = Stat(time = stats!!.timeStamp, bytes = stats.videoBytesReceived)
      subscriberVideoStats.add(stat)
    }

    override fun onAudioStats(subscriberKit: SubscriberKit, stats: SubscriberKit.SubscriberAudioStats?) {
      val stat = Stat(time = stats!!.timeStamp, bytes = stats.audioBytesReceived)
      subscriberAudioStats.add(stat)
    }

    override fun onError(subscriberKit: SubscriberKit?, opentokError: OpentokError?) {
      log("subscriber error ${opentokError?.message ?: ""}")
      analytics?.trackVideoSubscriberError(
        subscriberKit?.session?.sessionId ?: "",
        subscriberKit?.stream?.streamId ?: "",
        opentokError?.message ?: ""
      )
    }
  }

  private val streamListener = object : SubscriberKit.StreamListener {
    override fun onDisconnected(subscriberKit: SubscriberKit?) {
      subscribersReconnecting[subscriberKit?.stream?.streamId ?: ""] = true
    }

    override fun onReconnected(subscriberKit: SubscriberKit?) {
      subscribersReconnecting[subscriberKit?.stream?.streamId ?: ""] = false
    }
  }
}

data class Stat(val time: Double, var bytes: Int)

// In order to measure the network speed, we want to sample the last N seconds of bytes received.
// This queue will take in network stats, and hold onto
class StatQueue(private val maxTime: Double) {
  private val list = LinkedList<Stat>()
  var total = 0
    private set
  var average by mutableStateOf(0f)
    private set
  private var lastBytesReceived = 0

  fun add(stat: Stat) {
    val bytes = stat.bytes
    stat.bytes -= lastBytesReceived
    lastBytesReceived = bytes

    list.offer(stat)
    total += stat.bytes

    while (list.size > 0 && stat.time - list.peekFirst()!!.time > maxTime * 1000.0) {
      val removed = list.remove()
      total -= removed.bytes
    }

    average = if (list.size > 1) {
      total.toFloat() / ((stat.time - list.peekFirst()!!.time).toFloat() / 1000f)
    } else {
      stat.bytes.toFloat()
    }
  }

  fun clear() {
    list.clear()
    total = 0
    average = 0f
    lastBytesReceived = 0
  }
}
