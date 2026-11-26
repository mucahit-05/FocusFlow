package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.ui.TimerMode

enum class AppThemeMode(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    BLACK("black");

    companion object {
        fun fromCode(code: String?): AppThemeMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
    }
}

fun getModeColorScheme(
    mode: TimerMode,
    darkTheme: Boolean,
    pureBlack: Boolean = false
): ColorScheme {
    val (primaryColor, primaryContainerColor, onPrimaryContainerColor) = when (mode) {
        TimerMode.FOCUS -> Triple(
            FocusRed,
            if (darkTheme) FocusRedContainer else Color(0xFFFFDAD3),
            if (darkTheme) OnFocusRedContainer else Color(0xFF3E0500)
        )
        TimerMode.SHORT_BREAK -> Triple(
            BreakSage,
            if (darkTheme) BreakSageContainer else Color(0xFFBCEEE1),
            if (darkTheme) OnBreakSageContainer else Color(0xFF00201A)
        )
        TimerMode.LONG_BREAK -> Triple(
            LongBreakBlue,
            if (darkTheme) LongBreakBlueContainer else Color(0xFFD4E3FF),
            if (darkTheme) OnLongBreakBlueContainer else Color(0xFF001C38)
        )
    }

    return if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryContainerColor,
            onPrimaryContainer = onPrimaryContainerColor,
            secondary = primaryColor,
            onSecondary = Color.White,
            secondaryContainer = primaryContainerColor,
            onSecondaryContainer = onPrimaryContainerColor,
            tertiary = AmberAccent,
            onTertiary = Color.Black,
            tertiaryContainer = AmberContainer,
            onTertiaryContainer = OnAmberContainer,
            background = if (pureBlack) BackgroundPureBlack else BackgroundDark,
            onBackground = TextPrimaryDark,
            surface = if (pureBlack) SurfacePureBlack else SurfaceDark,
            onSurface = TextPrimaryDark,
            surfaceVariant = if (pureBlack) SurfaceVariantPureBlack else SurfaceVariantDark,
            onSurfaceVariant = TextSecondaryDark,
            outline = TextTertiaryDark
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryContainerColor,
            onPrimaryContainer = onPrimaryContainerColor,
            secondary = primaryColor,
            onSecondary = Color.White,
            secondaryContainer = primaryContainerColor,
            onSecondaryContainer = onPrimaryContainerColor,
            tertiary = AmberAccent,
            onTertiary = Color.Black,
            tertiaryContainer = Color(0xFFFFE082),
            onTertiaryContainer = Color(0xFF261900),
            background = BackgroundLight,
            onBackground = TextPrimaryLight,
            surface = SurfaceLight,
            onSurface = TextPrimaryLight,
            surfaceVariant = SurfaceVariantLight,
            onSurfaceVariant = TextSecondaryLight,
            outline = TextTertiaryLight
        )
    }
}

@Composable
fun FocusFlowTheme(
    mode: TimerMode = TimerMode.FOCUS,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.BLACK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit
) {
    val pureBlack = themeMode == AppThemeMode.BLACK
    val colorScheme = getModeColorScheme(mode, darkTheme, pureBlack = pureBlack)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
