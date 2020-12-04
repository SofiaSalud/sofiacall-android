package com.sofiasalud.callscreen

import android.content.Context
import android.util.AttributeSet
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import com.sofiasalud.callscreen.ui.CallScreenTheme

class CallView(context: Context, attrs: AttributeSet? = null, defStyleArr: Int = 0) : AbstractComposeView(context, attrs, defStyleArr) {
    var room by mutableStateOf<VCRoom?>(null)
    var name by mutableStateOf("")
    var title by mutableStateOf<String?>(null)
    var subtitle by mutableStateOf<String?>(null)
    var paused by mutableStateOf(false)
    var onGoBack by mutableStateOf({})
    var analytics by mutableStateOf<CallScreenAnalytics?>(null)
    var debugStats by mutableStateOf(false)

    @Composable
    override fun Content() {
        CallScreenTheme {
            Surface(color = MaterialTheme.colors.background) {
                CallScreen(
                        name = name,
                        titleOverride = title,
                        subtitleOverride = subtitle,
                        room = room,
                        onGoBack = onGoBack,
                        analytics = analytics,
                        isActivityPaused = paused,
                        debugStats = debugStats
                )
            }
        }
    }
}