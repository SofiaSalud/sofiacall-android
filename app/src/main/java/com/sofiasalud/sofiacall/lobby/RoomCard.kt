package com.sofiasalud.sofiacall.lobby

import android.text.format.DateUtils
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.sofiasalud.callscreen.VCRoom
import java.util.*


@Composable
fun RoomCard(
  room: VCRoom,
  onDelete: () -> Unit,
  onOpenUrl: () -> Unit,
  contextMenuExpanded: Boolean = false,
  setContextMenuExpanded: (Boolean) -> Unit,
  onPress: () -> Unit
) {
  val currTime = Calendar.getInstance().timeInMillis
  val createdTime = room.createdTime * 1000L
  val timeDiff = currTime - createdTime
  val timeAgo = if (timeDiff < 60000L) {
    "just now"
  } else {
    DateUtils.getRelativeTimeSpanString(
      createdTime,
      currTime,
      DateUtils.MINUTE_IN_MILLIS,
      DateUtils.FORMAT_ABBREV_ALL
    )
  }

  Card(
    shape = MaterialTheme.shapes.medium,
    elevation = 4.dp,
    modifier = Modifier
      .padding(8.dp)
      .fillMaxWidth()
      .clickable(onClick = onPress)
  ) {
    val body1 = MaterialTheme.typography.body1
    val body2 = MaterialTheme.typography.body2
    val bold = { type: TextStyle -> type.merge(TextStyle(fontWeight = FontWeight.Bold)) }

    val menuPress: (fn: () -> Unit) -> (() -> Unit) = { fn ->
      {
        setContextMenuExpanded(false)
        fn()
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).weight(1f)) {
        Row {
          Text(text = "Name: ", style = bold(body1))
          Text(room.name, style = body1)
        }
        Row {
          Text(text = "Created by: ", style = bold(body2))
          Text(text = room.createdBy, style = body2)
        }
        Row {
          Text(text = "Created: ", style = bold(body2))
          Text(text = timeAgo.toString(), style = body2)
        }
      }
      DropdownMenu(
        toggle = {
          IconButton(onClick = { setContextMenuExpanded(true) }) {
            Icon(Icons.Default.MoreVert)
          }
        },
        expanded = contextMenuExpanded,
        onDismissRequest = { setContextMenuExpanded(false) }
      ) {
        DropdownMenuItem(onClick = menuPress(onPress)) {
          Text("Join Room")
        }
        DropdownMenuItem(onClick = menuPress(onOpenUrl)) {
          Text("Join on Web")
        }
//          DropdownMenuItem(onClick = {}) {
//            Text("View Details")
//          }
        DropdownMenuItem(onClick = menuPress(onDelete)) {
          Text("Delete", color = Color.Red)
        }
      }
    }
  }

}

@Preview
@Composable
fun PreviewRoomCard() {
  RoomCard(
    room = VCRoom(
      apiKey = "123",
      createdTime = 1600000000L,
      createdBy = "Ian",
      expireTime = 1600000000L,
      id = "abc",
      name = "Ian's Room",
      sessionId = "111222333",
      sessionType = "routed",
      token = "xyz",
      feedbackUrl = "",
      url = ""
    ),
    contextMenuExpanded = false,
    setContextMenuExpanded = {},
    onDelete = {},
    onOpenUrl = {},
    onPress = {}
  )
}
