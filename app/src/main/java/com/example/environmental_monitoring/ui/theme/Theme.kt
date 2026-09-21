package com.example.environmental_monitoring.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary            = Teal80,
    onPrimary          = Teal20,
    primaryContainer   = Teal30,
    onPrimaryContainer = Teal90,
    secondary            = BlueGrey80,
    onSecondary          = BlueGrey30,
    secondaryContainer   = BlueGrey30,
    onSecondaryContainer = BlueGrey90,
    tertiary            = Amber80,
    onTertiary          = Color(0xFF3A1F00),
    tertiaryContainer   = Amber40,
    onTertiaryContainer = Amber90,
    background   = SurfaceDark1,
    onBackground = Teal95,
    surface        = SurfaceDark2,
    onSurface      = Teal99,
    surfaceVariant   = SurfaceDark3,
    onSurfaceVariant = BlueGrey80,
    error            = AlarmRed,
    onError          = Color(0xFF690005),
    errorContainer   = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline        = Color(0xFF4A6663),
    outlineVariant = SurfaceDark4,
    inverseSurface      = Teal90,
    inverseOnSurface    = Teal10,
    inversePrimary      = Teal40
)

private val LightColorScheme = lightColorScheme(
    primary            = Teal40,
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Teal90,
    onPrimaryContainer = Teal10,
    secondary            = BlueGrey40,
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = BlueGrey90,
    onSecondaryContainer = BlueGrey30,
    tertiary            = Amber40,
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Amber90,
    onTertiaryContainer = Color(0xFF3A1F00),
    background   = Teal99,
    onBackground = Teal10,
    surface        = Color(0xFFFFFFFF),
    onSurface      = Teal10,
    surfaceVariant   = Teal95,
    onSurfaceVariant = Teal20,
    error          = AlarmRedDk,
    outline        = Teal40,
    outlineVariant = Teal95,
    inverseSurface   = Teal20,
    inverseOnSurface = Teal95,
    inversePrimary   = Teal80
)

@Composable
fun EnvironmentalMonitoringTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Usamos nuestro esquema personalizado (teal ambiental) siempre;
    // no se usa dynamic color para garantizar consistencia en todos los dispositivos.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
