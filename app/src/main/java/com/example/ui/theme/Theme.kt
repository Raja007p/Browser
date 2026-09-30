package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ui.ThemeMode

private val AegisDarkColorScheme = darkColorScheme(
    primary = AegisCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFFA6EEFF),
    secondary = AegisIndigo,
    onSecondary = Color(0xFFFFFFFF),
    background = AegisDarkBg,
    onBackground = TextPrimaryDark,
    surface = AegisDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = AegisDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    tertiary = AegisEmerald
)

private val AegisAmoledColorScheme = darkColorScheme(
    primary = AegisCyan,
    onPrimary = Color.Black,
    secondary = AegisIndigo,
    background = AegisAmoledBg,
    onBackground = TextPrimaryDark,
    surface = AegisAmoledSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = AegisAmoledBorder,
    onSurfaceVariant = TextSecondaryDark,
    tertiary = AegisEmerald
)

private val AegisLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    secondary = Color(0xFF4F46E5),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    tertiary = Color(0xFF059669)
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    isIncognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when {
        isIncognito -> darkColorScheme(
            primary = AegisIncognitoPurple,
            onPrimary = Color.White,
            background = AegisIncognitoBg,
            surface = AegisIncognitoSurface,
            onBackground = Color.White,
            onSurface = Color.White,
            surfaceVariant = Color(0xFF2D1F4F),
            tertiary = AegisCyan
        )
        themeMode == ThemeMode.AMOLED_BLACK -> AegisAmoledColorScheme
        themeMode == ThemeMode.DARK -> AegisDarkColorScheme
        themeMode == ThemeMode.LIGHT -> AegisLightColorScheme
        else -> if (systemDark) AegisDarkColorScheme else AegisLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
