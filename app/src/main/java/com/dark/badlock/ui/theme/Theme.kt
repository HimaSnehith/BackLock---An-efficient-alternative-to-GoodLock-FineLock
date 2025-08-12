package com.dark.badlock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define the colors to match Good Lock's clean aesthetic
val GoodLockBlue = Color(0xFF6B4BFF)
val GoodLockBackground = Color(0xFFF0F0F0)
val GoodLockCard = Color(0xFFFFFFFF)
val GoodLockPurple = Color(0xFF5A44E5) // A variant of blue used in buttons

private val LightColorScheme = lightColorScheme(
    primary = GoodLockBlue,
    secondary = GoodLockBackground,
    tertiary = GoodLockCard,
    background = GoodLockBackground,
    surface = GoodLockCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = GoodLockBlue,
    secondary = GoodLockBackground,
    tertiary = GoodLockCard,
    background = GoodLockBackground,
    surface = GoodLockCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun BadlockTheme(
    darkTheme: Boolean = false, // You can make this dynamic if you want
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}