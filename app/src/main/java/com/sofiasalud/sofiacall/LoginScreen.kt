package com.sofiasalud.sofiacall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.sofiasalud.sofiacall.ui.purple500

@Composable
fun LoginScreen(
    onSubmit: (name: String) -> Unit,
    analytics: Analytics?
) {

  val (name, setName) = remember { mutableStateOf("") }
  remember {
    analytics?.trackScreenUsername()
  }

  val submit = {
    val n = name.trim()
    if (n.isNotBlank()) {
      onSubmit(n)
    }
  }

  Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
    Column {
      Text("Build Information", style = MaterialTheme.typography.h6)
      Text("Git Build Number: ${BuildConfig.GIT_COUNT}", style = MaterialTheme.typography.body1)
      Text("Git Commit Date: ${BuildConfig.GIT_DATE}", style = MaterialTheme.typography.body1)
      Text("Git Hash: ${BuildConfig.GIT_HASH}", style = MaterialTheme.typography.body1)
    }
    Column(
      modifier = Modifier
        .weight(1f)
        .padding(top = 64.dp)
        .fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "Enter your name:",
        modifier = Modifier.align(Alignment.Start),
        style = MaterialTheme.typography.body1
      )
      TextField(
        value = name,
        onValueChange = { setName(it.replace("\n", ""))},
        label = {},
        backgroundColor = Color.Transparent,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.NoAction, keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()
      )
      TextButton(
        colors = ButtonConstants.defaultButtonColors(backgroundColor = purple500),
        enabled = name.isNotBlank(),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
        onClick = {
          submit()
        }
      ) {
        Text(text = "Start", color = Color.White, style = MaterialTheme.typography.button)
      }
    }
  }
}

@Preview
@Composable
fun LoginScreenPreview() {
  LoginScreen(onSubmit = {}, analytics = null)
}
