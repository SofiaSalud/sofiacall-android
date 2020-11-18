package com.sofiasalud.sofiacall.lobby

import androidx.compose.material.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview


@Composable
fun LobbyTopBar(
    name: String,
    resetName: () -> Unit,
) {
  TopAppBar(
      title = {
          Row {
              Text(
                  "Name:",
                  style = MaterialTheme
                      .typography
                      .body1
                      .merge(
                          TextStyle(fontWeight = FontWeight.Bold)
                      )
              )
              Text(
                  name,
                  style = MaterialTheme.typography.body1,
                  modifier = Modifier.padding(start = 8.dp)
              )
          }
      },
      actions = {
          TextButton(onClick = resetName, modifier = Modifier.padding(end = 8.dp)) {
              Text("Edit", color = Color.White, style = MaterialTheme.typography.button)
          }
      }
  )
}

@Preview
@Composable
fun LobbyTopBarPreview() {
    LobbyTopBar(
        name = "Roboto",
        resetName = {}
    )
}