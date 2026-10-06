package io.github.halilozel1903.otp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.halilozel1903.otp.core.PinPadLayout

/** Colors of a [PinPad]. Create them with [PinPadDefaults.colors]. */
@Immutable
public class PinPadColors(
    public val key: Color,
    public val keyContent: Color,
    public val letters: Color,
    public val action: Color,
    public val actionContent: Color,
    public val disabledContent: Color,
)

/** Defaults for [PinPad]. */
public object PinPadDefaults {
    /** Colors from the Material 3 color scheme. */
    @Composable
    public fun colors(
        key: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
        keyContent: Color = MaterialTheme.colorScheme.onSurface,
        letters: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        action: Color = Color.Transparent,
        actionContent: Color = MaterialTheme.colorScheme.onSurface,
        disabledContent: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): PinPadColors = PinPadColors(key, keyContent, letters, action, actionContent, disabledContent)
}

/**
 * A numeric keypad for PINs and codes, drawn in the app instead of the system keyboard: three rows of three digits
 * and a last row with an optional [bottomStart] key, `0` and backspace.
 *
 * ```
 * var pin by rememberSaveable { mutableStateOf("") }
 * OtpField(value = pin, onValueChange = { pin = it }, length = 4, secure = true, readOnly = true)
 * PinPad(
 *     onDigit = { if (pin.length < 4) pin += it },
 *     onBackspace = { pin = pin.dropLast(1) },
 *     onBackspaceLongPress = { pin = "" },
 * )
 * ```
 *
 * @param onDigit called with `'0'` to `'9'`.
 * @param onBackspace called when backspace is tapped.
 * @param onBackspaceLongPress called when backspace is held, e.g. to clear everything; `null` to do nothing.
 * @param digits the order of the ten digit keys; [PinPadLayout.shuffled] gives a scrambled keypad.
 * @param showLetters print `ABC`, `DEF`, ... under the digits like a phone keypad.
 * @param bottomStart content of the bottom-left key, such as a biometrics button; empty when `null`.
 * @param keySize the diameter of each key.
 * @param haptics a light haptic on every key press.
 */
@Composable
public fun PinPad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onBackspaceLongPress: (() -> Unit)? = null,
    digits: List<Char> = PinPadLayout.Standard,
    showLetters: Boolean = true,
    colors: PinPadColors = PinPadDefaults.colors(),
    keySize: Dp = 72.dp,
    spacing: Dp = 20.dp,
    haptics: Boolean = true,
    backspaceDescription: String = "Delete",
    bottomStart: (@Composable () -> Unit)? = null,
) {
    require(digits.size == 10) { "digits must have the ten digit keys, had ${digits.size}" }
    val haptic = LocalHapticFeedback.current
    val press: (() -> Unit) -> Unit = { action ->
        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        action()
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing * 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (row in 0 until 3) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                for (column in 0 until 3) {
                    val digit = digits[row * 3 + column]
                    DigitKey(digit, showLetters, enabled, colors, keySize) { press { onDigit(digit) } }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            Box(Modifier.size(keySize), contentAlignment = Alignment.Center) {
                bottomStart?.invoke()
            }
            val last = digits[9]
            DigitKey(last, showLetters, enabled, colors, keySize) { press { onDigit(last) } }
            Box(
                modifier = Modifier
                    .size(keySize)
                    .clip(CircleShape)
                    .background(colors.action)
                    .combinedClickable(
                        enabled = enabled,
                        onLongClick = onBackspaceLongPress?.let { action -> { press(action) } },
                        onClick = { press(onBackspace) },
                    )
                    .semantics {
                        contentDescription = backspaceDescription
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center,
            ) {
                BackspaceIcon(if (enabled) colors.actionContent else colors.disabledContent, keySize * 0.36f)
            }
        }
    }
}

@Composable
private fun DigitKey(
    digit: Char,
    showLetters: Boolean,
    enabled: Boolean,
    colors: PinPadColors,
    size: Dp,
    onClick: () -> Unit,
) {
    val content = if (enabled) colors.keyContent else colors.disabledContent
    val letters = if (showLetters) PinPadLayout.lettersFor(digit) else ""
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.key)
            .combinedClickable(enabled = enabled, onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BasicText(
                text = digit.toString(),
                style = TextStyle(
                    color = content,
                    fontSize = (size.value * 0.36f).sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
            )
            if (showLetters) {
                // Keep every key the same height, with or without letters.
                BasicText(
                    text = letters.ifEmpty { " " },
                    style = TextStyle(
                        color = if (enabled) colors.letters else colors.disabledContent,
                        fontSize = (size.value * 0.13f).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

@Composable
private fun BackspaceIcon(color: Color, size: Dp) {
    Canvas(Modifier.size(size * 1.3f, size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = h * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val outline = Path().apply {
            moveTo(w * 0.3f, h * 0.08f)
            lineTo(w * 0.94f, h * 0.08f)
            lineTo(w * 0.94f, h * 0.92f)
            lineTo(w * 0.3f, h * 0.92f)
            lineTo(w * 0.04f, h * 0.5f)
            close()
        }
        drawPath(outline, color, style = stroke)
        val cx = w * 0.62f
        val cy = h * 0.5f
        val d = h * 0.17f
        drawLine(color, Offset(cx - d, cy - d), Offset(cx + d, cy + d), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(cx - d, cy + d), Offset(cx + d, cy - d), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
