package com.sofiasalud.sofiacall.lobby

import androidx.compose.material.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonConstants
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.ui.tooling.preview.Preview
import com.sofiasalud.sofiacall.ui.purple500

@Composable
fun NewRoomModal(
  onCreateRoom: (name: String) -> Unit,
  open: Boolean,
  setOpen: (Boolean) -> Unit,
) {
  val (roomNameInput, setRoomNameInput) = remember { mutableStateOf("") }

  if (open) {
    AlertDialog(
      onDismissRequest = {
        setRoomNameInput("")
        setOpen(false)
      },
      title = { Text("Enter room name:") },
      text = {
        TextField(
          value = roomNameInput,
          onValueChange = {
            setRoomNameInput(it.replace("\n", ""))
          },
          label = { /* no label */ },
          backgroundColor = Color.Transparent,
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.NoAction,
            keyboardType = KeyboardType.Text,
          ),
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            setOpen(false)
            onCreateRoom(roomNameInput)
            setRoomNameInput("")
          },
          colors = ButtonConstants.defaultButtonColors(backgroundColor = purple500),
          enabled = roomNameInput.isNotBlank()
        ) {
          Text("Create", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { setOpen(false) }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Preview
@Composable
fun PreviewNewRoomModal() {
  Column(modifier = Modifier.fillMaxSize()) {
    NewRoomModal(
      onCreateRoom = {},
      open = true,
      setOpen = {}
    )
  }
}