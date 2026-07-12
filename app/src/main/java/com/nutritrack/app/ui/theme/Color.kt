package com.nutritrack.app.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Fixed traffic-light semantics for calorie pacing - independent of the M3 color scheme
// so "on track" always reads as green and "ahead of pace" always reads as amber.
val PaceGreen = Color(0xFF2E7D32)
val PaceAmber = Color(0xFFF9A825)

// Fixed 4-step AHA blood pressure severity scale, also independent of the M3 color scheme.
val BpNormalGreen = PaceGreen
val BpElevatedAmber = PaceAmber
val BpStage1Orange = Color(0xFFEF6C00)
val BpStage2Red = Color(0xFFC62828)