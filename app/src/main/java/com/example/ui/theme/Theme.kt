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

private val DarkColorScheme = darkColorScheme(
    primary = GeoPurplePrimary,
    onPrimary = GeoPurpleOnPrimary,
    primaryContainer = GeoPurpleContainer,
    onPrimaryContainer = GeoPurpleOnContainer,
    secondary = GeoPurplePrimary,
    onSecondary = GeoPurpleOnPrimary,
    secondaryContainer = GeoDarkActionKey,
    onSecondaryContainer = GeoTextPrimary,
    background = GeoDarkBackground,
    onBackground = GeoTextPrimary,
    surface = GeoDarkSurface,
    onSurface = GeoTextPrimary,
    surfaceVariant = GeoDarkSurfaceVariant,
    onSurfaceVariant = GeoTextSecondary,
    surfaceContainer = GeoDarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = GeoPurplePrimaryLight,
    onPrimary = GeoPurpleOnPrimaryLight,
    primaryContainer = GeoPurpleContainerLight,
    onPrimaryContainer = GeoPurpleOnContainerLight,
    secondary = GeoPurplePrimaryLight,
    onSecondary = GeoPurpleOnPrimaryLight,
    secondaryContainer = GeoLightActionKey,
    onSecondaryContainer = GeoPurpleOnContainerLight,
    background = GeoLightBackground,
    onBackground = Color(0xFF1D1B20),
    surface = GeoLightSurface,
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = GeoLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainer = GeoLightSurface
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
