package com.chibaminto.wearcost.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8CC7AA),
    onPrimary = Color(0xFF10251A),
    primaryContainer = Color(0xFF244637),
    onPrimaryContainer = Color(0xFFE4F5EA),
    secondary = Color(0xFFA8B9D4),
    onSecondary = Color(0xFF152234),
    secondaryContainer = Color(0xFF303D51),
    onSecondaryContainer = Color(0xFFEEF3FA),
    tertiary = Color(0xFFE0A294),
    background = Color(0xFF171512),
    surface = Color(0xFF242019),
    surfaceContainer = Color(0xFF2D2821),
    surfaceVariant = Color(0xFF3A332A),
    onBackground = Color(0xFFF3EDE4),
    onSurface = Color(0xFFF3EDE4),
    onSurfaceVariant = Color(0xFFD4C8BB),
    outline = Color(0xFF786B5D),
    outlineVariant = Color(0xFF493F35)
)

private val LightColorScheme = lightColorScheme(
    primary = SoftMint,
    onPrimary = Color.White,
    primaryContainer = SoftMintContainer,
    onPrimaryContainer = SlateText,
    secondary = SoftBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftBlueContainer,
    onSecondaryContainer = SlateText,
    tertiary = SoftCoral,
    background = CloudGray,
    onBackground = SlateText,
    surface = CleanWhite,
    onSurface = SlateText,
    surfaceContainer = Color(0xFFF4EEE6),
    surfaceVariant = Color(0xFFF1E8DD),
    onSurfaceVariant = MutedText,
    outline = MistGray,
    outlineVariant = Color(0xFFF1E7DA)
)

@Composable
fun WearCostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
