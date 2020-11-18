package com.sofiasalud.sofiacall.lobby

import androidx.compose.material.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.sofiasalud.callscreen.VCRoom
import com.sofiasalud.sofiacall.Analytics
import com.sofiasalud.sofiacall.ui.purple500
import java.util.*
import kotlin.concurrent.timerTask

@Composable
fun LobbyScreen(
  analytics: Analytics?,
  openUrl: (url: String) -> Unit,
  rooms: List<VCRoom>,
  onStartCall: (room: VCRoom) -> Unit,
  onCreateRoom: (name: String) -> Unit,
  onDeleteRoom: (room: VCRoom) -> Unit,
  createRoomLoading: Boolean,
  name: String,
  resetName: () -> Unit,
) {
  remember { analytics?.trackScreenWaitingRoom() }
  val scaffoldState = rememberScaffoldState()
  var contextMenuIndex by remember { mutableStateOf(-1) }
  val (newRoomMenuOpen, setNewRoomMenuOpen) = remember { mutableStateOf(false) }
  val (selectedTab, setSelectedTab) = remember { mutableStateOf(0) }

  Scaffold(
    scaffoldState = scaffoldState,
    topBar = { LobbyTopBar(name, resetName) },
    bodyContent = {
      Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
      ) {
        TabRow(
          selectedTabIndex = selectedTab,
          backgroundColor = Color.White,
          contentColor = purple500
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { setSelectedTab(0) },
            text = { Text("All Rooms") }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { setSelectedTab(1) },
            text = { Text("Needs Partner") }
          )
        }
        LazyColumnForIndexed(
          items = when (selectedTab) {
            1 -> rooms.filter { it.needsPartner != null }.sortedBy { it.id }
            else -> rooms.sortedBy { it.id }
          },
          modifier = Modifier.weight(1f).padding(top = 8.dp)
        ) { index, room ->
          RoomCard(
            room = room,
            onOpenUrl = { openUrl(room.url) },
            onDelete = { onDeleteRoom(room) },
            contextMenuExpanded = contextMenuIndex == index,
            setContextMenuExpanded = {
              contextMenuIndex = if (it) index else -1
            },
            onPress = { onStartCall(room) }
          )
        }
        CreateRoomButton(
          onClick = { setNewRoomMenuOpen(true) },
          loading = createRoomLoading,
          modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        NewRoomModal(
          onCreateRoom = onCreateRoom,
          open = newRoomMenuOpen,
          setOpen = setNewRoomMenuOpen
        )
      }
    })
}

@Preview
@Composable
fun PreviewLobbyScreen() {
  val (loading, setLoading) = remember { mutableStateOf(false) }
  LobbyScreen(
    rooms = listOf(
      VCRoom(
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
      VCRoom(
        apiKey = "1234",
        createdTime = 1600000001L,
        expireTime = 1600000002L,
        id = "abcd",
        createdBy = "Dwayne \"The Rock\" Johnson",
        name = "Meeting Room 1",
        sessionId = "1112223334444",
        sessionType = "routed",
        token = "xyzz",
        feedbackUrl = "",
        url = ""
      ),
      VCRoom(
        apiKey = "12345",
        createdTime = 1600000003L,
        expireTime = 1600000004L,
        createdBy = "Dracula",
        id = "abcde",
        name = "Vampires ONLY",
        sessionId = "11122233344445555",
        sessionType = "routed",
        token = "xyzzz",
        feedbackUrl = "",
        url = ""
      )
    ),
    createRoomLoading = loading,
    onStartCall = {},
    onCreateRoom = {
      setLoading(true)
      Timer().schedule(timerTask {
        setLoading(false)
      }, 2000)
    },
    name = "Test",
    openUrl = {},
    onDeleteRoom = {},
    resetName = {},
    analytics = null,
  )
}