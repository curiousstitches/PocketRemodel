package com.pocketremodel.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandTeal = Color(0xFF16E0B5)
val BrandDark = Color(0xFF0E1116)
val Glass = Color(0xCC11161D)

private val Dark = darkColorScheme(
    primary = BrandTeal,
    onPrimary = BrandDark,
    background = BrandDark,
    surface = Glass,
    onSurface = Color.White
)

private val Light = lightColorScheme(
    primary = BrandTeal,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color(0xF2FFFFFF),
    onSurface = BrandDark
)

@Composable
fun PocketRemodelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        typography = MaterialTheme.typography,
        content = content
    )
}
