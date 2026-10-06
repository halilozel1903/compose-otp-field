package io.github.halilozel1903.otp.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Deep indigo with a warm gold accent for Lumen Bank. */
@Composable
fun BankTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) {
        darkColorScheme(
            primary = Color(0xFFB9C3FF),
            onPrimary = Color(0xFF1B2A78),
            primaryContainer = Color(0xFF33418F),
            onPrimaryContainer = Color(0xFFDDE1FF),
            secondary = Color(0xFFC3C5DD),
            secondaryContainer = Color(0xFF424659),
            onSecondaryContainer = Color(0xFFDFE1F9),
            tertiary = Color(0xFFF2C062),
            tertiaryContainer = Color(0xFF5D4200),
            onTertiaryContainer = Color(0xFFFFDEA6),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
            background = Color(0xFF111318),
            onBackground = Color(0xFFE2E2E9),
            surface = Color(0xFF111318),
            onSurface = Color(0xFFE2E2E9),
            surfaceVariant = Color(0xFF45464F),
            onSurfaceVariant = Color(0xFFC6C5D0),
            surfaceContainerLowest = Color(0xFF0C0E13),
            surfaceContainerLow = Color(0xFF191B20),
            surfaceContainer = Color(0xFF1D1F25),
            surfaceContainerHigh = Color(0xFF282A2F),
            surfaceContainerHighest = Color(0xFF33353A),
            outline = Color(0xFF90909A),
            outlineVariant = Color(0xFF45464F),
            inverseSurface = Color(0xFFE2E2E9),
            inverseOnSurface = Color(0xFF2E3036),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF3A4AA0),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDDE1FF),
            onPrimaryContainer = Color(0xFF001257),
            secondary = Color(0xFF5A5D72),
            secondaryContainer = Color(0xFFDFE1F9),
            onSecondaryContainer = Color(0xFF171B2C),
            tertiary = Color(0xFF7B5800),
            tertiaryContainer = Color(0xFFFFDEA6),
            onTertiaryContainer = Color(0xFF271900),
            error = Color(0xFFBA1A1A),
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),
            background = Color(0xFFF6F6FC),
            onBackground = Color(0xFF1A1B21),
            surface = Color(0xFFF6F6FC),
            onSurface = Color(0xFF1A1B21),
            surfaceVariant = Color(0xFFE2E1EC),
            onSurfaceVariant = Color(0xFF45464F),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFFFFFF),
            surfaceContainer = Color(0xFFEDEDF4),
            surfaceContainerHigh = Color(0xFFE8E7EF),
            surfaceContainerHighest = Color(0xFFE2E2E9),
            outline = Color(0xFF767680),
            outlineVariant = Color(0xFFC6C5D0),
            inverseSurface = Color(0xFF2F3036),
            inverseOnSurface = Color(0xFFF1F0F7),
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
