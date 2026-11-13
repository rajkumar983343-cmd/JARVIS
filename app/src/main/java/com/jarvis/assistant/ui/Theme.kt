
package com.jarvis.assistant.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object JarvisColors {
    val Background = Color(0xFF050B16)
    val Surface = Color(0xFF101C2C)
    val Cyan = Color(0xFF00D9FF)
    val Blue = Color(0xFF1261A0)
    val TextPrimary = Color(0xFFEAFBFF)
    val TextSecondary = Color(0xFF91A9B8)
}

private val JarvisDarkColors = darkColorScheme(
    primary = JarvisColors.Cyan,
    secondary = JarvisColors.Blue,
    background = JarvisColors.Background,
    surface = JarvisColors.Surface,
    onPrimary = Color.Black,
    onBackground = JarvisColors.TextPrimary,
    onSurface = JarvisColors.TextPrimary
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JarvisDarkColors,
        content = content
    )
}
