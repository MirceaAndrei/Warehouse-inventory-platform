package com.inventory.scanner.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary          = Purple80,
    onPrimary        = SurfaceDark,
    primaryContainer = Color(0xFF1E2240),
    onPrimaryContainer = Purple80,

    secondary          = PurpleGrey80,
    onSecondary        = SurfaceDark,
    secondaryContainer = Color(0xFF1C2035),
    onSecondaryContainer = PurpleGrey80,

    tertiary          = Blue80,
    onTertiary        = SurfaceDark,
    tertiaryContainer = Color(0xFF1A2540),
    onTertiaryContainer = Blue80,

    background        = BackgroundDark,
    onBackground      = Color(0xFFE4E8F5),

    surface           = SurfaceCardDark,
    onSurface         = Color(0xFFE4E8F5),
    surfaceVariant    = Color(0xFF191D30),
    onSurfaceVariant  = Color(0xFF8890B0),

    outline           = Color(0xFF3A4060),
    error             = StatusDanger,
    onError           = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary          = Purple40,
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8EBFF),
    onPrimaryContainer = Color(0xFF1A1D3A),

    secondary          = PurpleGrey40,
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDE7F6),
    onSecondaryContainer = Color(0xFF2D1B4E),

    tertiary          = Blue40,
    onTertiary        = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE3F2FD),
    onTertiaryContainer = Color(0xFF0D47A1),

    background        = BackgroundLight,
    onBackground      = Color(0xFF1A1D3A),

    surface           = SurfaceCardLight,
    onSurface         = Color(0xFF1A1D3A),
    surfaceVariant    = Color(0xFFE3E6F3),
    onSurfaceVariant  = Color(0xFF5A6080),

    outline           = Color(0xFFD5D9EE),
    error             = StatusDanger,
    onError           = Color(0xFFFFFFFF)
)

@Composable
fun InventoryScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}
