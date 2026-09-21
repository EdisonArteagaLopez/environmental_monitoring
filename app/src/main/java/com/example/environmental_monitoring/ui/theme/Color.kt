package com.example.environmental_monitoring.ui.theme

import androidx.compose.ui.graphics.Color

// === Paleta Teal — color primario del tema ===
val Teal10  = Color(0xFF00201C)
val Teal20  = Color(0xFF003733)
val Teal30  = Color(0xFF004F4B)
val Teal40  = Color(0xFF006A63)
val Teal80  = Color(0xFF4DDBD0)
val Teal90  = Color(0xFF6FF7EB)
val Teal95  = Color(0xFFB8FBF6)
val Teal99  = Color(0xFFF0FFFE)

// === Azul-gris secundario ===
val BlueGrey30 = Color(0xFF37474F)
val BlueGrey40 = Color(0xFF455A64)
val BlueGrey80 = Color(0xFFB0BEC5)
val BlueGrey90 = Color(0xFFCFD8DC)

// === Ámbar — advertencias / terciario ===
val Amber40 = Color(0xFFB45309)
val Amber80 = Color(0xFFFCD34D)
val Amber90 = Color(0xFFFDE68A)

// === Superficies oscuras con tinte teal ===
val SurfaceDark1 = Color(0xFF0F1A19)
val SurfaceDark2 = Color(0xFF1A2625)
val SurfaceDark3 = Color(0xFF253332)
val SurfaceDark4 = Color(0xFF2F3E3D)

// === Colores de estado de alarma ===
val AlarmRed   = Color(0xFFEF5350)
val AlarmRedDk = Color(0xFFB71C1C)
val OkGreen    = Color(0xFF4CAF50)
val WarnAmber  = Color(0xFFFF9800)

// === Temperatura — codificación por color ===
val TempCold = Color(0xFF4FC3F7)   // Azul   – por debajo del mínimo
val TempOk   = Color(0xFF80CBC4)   // Teal   – en rango normal
val TempHot  = Color(0xFFEF9A9A)   // Rojo   – por encima del máximo

// === Humedad — codificación por color ===
val HumLow  = Color(0xFFFFCC80)    // Ámbar  – seco (< mínimo)
val HumOk   = Color(0xFF80DEEA)    // Cyan   – en rango normal
val HumHigh = Color(0xFFCE93D8)    // Violeta – húmedo (> máximo)
