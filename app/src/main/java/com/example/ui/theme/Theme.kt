package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SkillPrimary,
    secondary = SkillSecondary,
    background = SkillBg,
    surface = SkillSurface,
    surfaceVariant = SkillCard,
    onBackground = SkillText,
    onSurface = SkillText,
    onPrimary = Color.White,
    onSecondary = Color.White,
    outline = SkillMutedText,
    outlineVariant = SkillBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Light theme only as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
