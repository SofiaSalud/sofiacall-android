package com.sofiasalud.sofiacall.lobby

import androidx.compose.animation.animateContentSize
import androidx.compose.material.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonConstants
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.sofiasalud.sofiacall.ui.purple200
import com.sofiasalud.sofiacall.ui.purple500

@Composable
fun CreateRoomButton(
  onClick: () -> Unit,
  loading: Boolean,
  modifier: Modifier = Modifier,
) {
  Button(
    onClick = onClick,
    enabled = !loading,
    colors = ButtonConstants.defaultButtonColors(
      backgroundColor = if (loading) purple200 else purple500
    ),
    elevation = ButtonConstants.defaultElevation(defaultElevation = 8.dp, disabledElevation = 4.dp),
    modifier = modifier
      .padding(16.dp)
      .animateContentSize()
  ) {
    if (loading) {
      CircularProgressIndicator(
        color = purple500,
      )
    } else {
      Text(text = "Create Room")
    }
  }
}

@Preview
@Composable
fun PreviewCreateRoomButton() {
  CreateRoomButton(onClick = {}, loading = false)
}

@Preview
@Composable
fun PreviewCreateRoomButtonLoading() {
  CreateRoomButton(onClick = {}, loading = true)
}