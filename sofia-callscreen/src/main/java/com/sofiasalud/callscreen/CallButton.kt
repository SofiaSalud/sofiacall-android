package com.sofiasalud.callscreen

import androidx.compose.foundation.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonConstants
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.sofiasalud.callscreen.ui.solidGray
import com.sofiasalud.callscreen.ui.transparentGray

@Composable
fun CallButton(
  onClick: () -> Unit,
  active: Boolean,
  activeIcon: Int,
  inactiveIcon: Int
) {
  val buttonColor = if (active) Color.White else transparentGray
  val icon = if (active) activeIcon else inactiveIcon
  val iconTint = if (active) solidGray else Color.White

  FloatingActionButton(
    onClick = onClick,
    modifier = Modifier.size(width = 44.dp, height = 44.dp),
    backgroundColor = buttonColor,
    elevation = FloatingActionButtonConstants.defaultElevation(0.dp, 0.dp),
  ) {
    Icon(vectorResource(icon), tint = iconTint)
  }
}