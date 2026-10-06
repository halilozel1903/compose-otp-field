package io.github.halilozel1903.otp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.halilozel1903.otp.core.OtpCharset
import io.github.halilozel1903.otp.core.OtpCodeExtractor
import io.github.halilozel1903.otp.core.OtpEditor
import io.github.halilozel1903.otp.core.OtpFormat
import io.github.halilozel1903.otp.core.OtpInputState
import io.github.halilozel1903.otp.core.OtpNormalizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A one-time code or PIN field with one cell per character.
 *
 * It is a single hidden [BasicTextField] with the cells drawn on top, so the keyboard, SMS autofill, the keyboard's
 * clipboard suggestions and accessibility services see one ordinary text field. Every edit goes through
 * [OtpEditor]: typing writes at the active cell, pasted codes are cleaned up (`482-913` → `482913`, full-width digits
 * converted) and fill from the active cell, backspace clears the active cell or moves back, and tapping a cell makes
 * it active. Long-pressing a cell offers to paste the code found in the clipboard.
 *
 * ```
 * var code by rememberSaveable { mutableStateOf("") }
 * OtpField(
 *     value = code,
 *     onValueChange = { code = it },
 *     length = 6,
 *     style = OtpFieldDefaults.boxed(),
 *     onComplete = { viewModel.verify(it) },
 * )
 * ```
 *
 * @param value the code, at most [length] characters.
 * @param onValueChange called with the new code after every edit in the field.
 * @param length number of cells.
 * @param style the look of the cells; see [OtpFieldDefaults].
 * @param charset digits only, or letters and digits.
 * @param secure draws dots instead of characters; the last typed character is shown for [revealMillis] first.
 * @param isError draws the cells in error colors and, each time it turns `true`, shakes the field and performs a
 *   reject haptic (see [hapticOnError]).
 * @param readOnly no keyboard and no edits from the field itself, for codes entered with a [PinPad].
 * @param autoFocus focuses the field and opens the keyboard when it first appears.
 * @param autofill marks the field as an SMS one-time code for autofill services (`ContentType.SmsOtpCode`).
 * @param onComplete called when an edit in the field fills the last cell.
 * @param blinkCursor blink the cursor in the active empty cell.
 * @param showCursorWithoutFocus highlight the active cell even when the field is not focused, for screens that enter
 *   the code with an on-screen keypad.
 * @param pasteLabel the label of the paste bubble shown on long press.
 * @param contentDescription what accessibility services announce for the field.
 * @param errorDescription what accessibility services announce while [isError] is `true`.
 * @param focusRequester to move focus to the field yourself.
 */
@Composable
public fun OtpField(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 6,
    style: OtpFieldStyle = OtpFieldDefaults.boxed(),
    modifier: Modifier = Modifier,
    charset: OtpCharset = OtpCharset.Digits,
    secure: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    autoFocus: Boolean = false,
    autofill: Boolean = true,
    onComplete: ((String) -> Unit)? = null,
    revealMillis: Long = 800L,
    blinkCursor: Boolean = true,
    showCursorWithoutFocus: Boolean = false,
    hapticOnError: Boolean = true,
    pasteLabel: String = "Paste",
    contentDescription: String = "Verification code",
    errorDescription: String = "Wrong code",
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val format = remember(length, charset) { OtpFormat(length, charset) }
    val editor = remember(format) { OtpEditor(format) }
    val extractor = remember(format) { OtpCodeExtractor(format) }
    val textState = rememberTextFieldState(OtpNormalizer.sanitize(value, format))
    val transformation = remember(editor) { OtpInputTransformation(editor) }

    val currentValue by rememberUpdatedState(value)
    val currentEditor by rememberUpdatedState(editor)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnComplete by rememberUpdatedState(onComplete)

    // Changes from outside (clearing after an error, a code from SMS, a PinPad) go into the hidden text field.
    LaunchedEffect(value, editor) {
        val clean = OtpNormalizer.sanitize(value, editor.format)
        if (textState.text.toString() != clean) textState.setCode(OtpInputState(clean, clean.length))
    }
    // Edits in the field go out through onValueChange.
    LaunchedEffect(textState) {
        snapshotFlow { textState.text.toString() }.collect { text ->
            val editorNow = currentEditor
            if (text != OtpNormalizer.sanitize(currentValue, editorNow.format)) {
                currentOnValueChange(text)
                if (editorNow.isComplete(text)) currentOnComplete?.invoke(text)
            }
        }
    }

    val text = textState.text.toString()
    val inputState = OtpInputState(text, textState.selection.min.coerceIn(0, text.length))
    var focused by remember { mutableStateOf(false) }
    val activeCell = editor.activeCell(inputState)
    val highlightActive = enabled && (focused || showCursorWithoutFocus)

    // Secure mode: show the character that was just typed for a moment.
    var revealed by remember { mutableStateOf<Int?>(null) }
    val previous = remember { arrayOf(text) }
    LaunchedEffect(text, secure, revealMillis) {
        val index = if (secure && revealMillis > 0) editor.revealedCell(previous[0], text) else null
        previous[0] = text
        revealed = index
        if (index != null) {
            delay(revealMillis)
            revealed = null
        }
    }

    // Error: shake and a reject haptic each time isError turns true.
    val shake = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(isError) {
        if (isError) {
            if (hapticOnError) haptics.performHapticFeedback(HapticFeedbackType.Reject)
            shake.snapTo(0f)
            shake.animateTo(1f, tween(durationMillis = 450, easing = LinearEasing))
            shake.snapTo(0f)
        }
    }

    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(autoFocus) {
        if (autoFocus && enabled && !readOnly) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var pasteCandidate by remember { mutableStateOf<String?>(null) }

    fun current(): OtpInputState {
        val now = textState.text.toString()
        return OtpInputState(now, textState.selection.min.coerceIn(0, now.length))
    }

    val onCellTap by rememberUpdatedState<(Int) -> Unit> { index ->
        if (enabled && !readOnly) {
            textState.setCode(currentEditor.tap(current(), index))
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    val onCellLongPress by rememberUpdatedState<() -> Unit> {
        if (enabled && !readOnly) {
            scope.launch {
                val clip = clipboard.readText() ?: return@launch
                val code = extractor.extract(clip) ?: OtpNormalizer.sanitize(clip, format)
                if (code.isNotEmpty()) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    pasteCandidate = code
                }
            }
        }
    }

    // Written while measuring when the cells have to shrink to fit; read only when drawing.
    val contentScale = remember { mutableFloatStateOf(1f) }
    val readScale: () -> Float = remember { { contentScale.floatValue } }

    Box(
        modifier = modifier.graphicsLayer {
            val progress = shake.value
            translationX = if (progress > 0f && progress < 1f) {
                sin(progress * PI * 6).toFloat() * (1f - progress) * 12.dp.toPx()
            } else {
                0f
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        Layout(
            content = {
                BasicTextField(
                    state = textState,
                    modifier = Modifier
                        .alpha(0f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focused = it.isFocused }
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Backspace && enabled && !readOnly) {
                                if (event.type == KeyEventType.KeyDown) {
                                    val now = current()
                                    if (now.value.isNotEmpty()) textState.setCode(currentEditor.backspace(now))
                                }
                                true
                            } else {
                                false
                            }
                        }
                        .semantics {
                            this.contentDescription = contentDescription
                            stateDescription = "${text.length} of $length"
                            if (autofill) contentType = ContentType.SmsOtpCode
                            if (secure) password()
                            if (isError) error(errorDescription)
                        },
                    enabled = enabled,
                    readOnly = readOnly,
                    inputTransformation = transformation,
                    textStyle = TextStyle(color = Color.Transparent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = if (charset == OtpCharset.Alphanumeric) {
                            KeyboardCapitalization.Characters
                        } else {
                            KeyboardCapitalization.None
                        },
                        autoCorrectEnabled = false,
                        keyboardType = when {
                            charset == OtpCharset.Digits -> KeyboardType.NumberPassword
                            secure -> KeyboardType.Password
                            else -> KeyboardType.Ascii
                        },
                        imeAction = ImeAction.Done,
                    ),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    cursorBrush = SolidColor(Color.Transparent),
                )
                for (index in 0 until length) {
                    OtpCell(
                        char = inputState.charAt(index),
                        masked = secure && revealed != index,
                        active = highlightActive && activeCell == index,
                        showCursor = highlightActive,
                        blinkCursor = blinkCursor,
                        isError = isError,
                        enabled = enabled,
                        style = style,
                        textStyle = style.textStyle,
                        contentScale = readScale,
                        modifier = Modifier
                            .clearAndSetSemantics {}
                            .pointerInput(index) {
                                detectTapGestures(
                                    onTap = { onCellTap(index) },
                                    onLongPress = { onCellLongPress() },
                                )
                            },
                    )
                }
            },
        ) { measurables, constraints ->
            val cellCount = measurables.size - 1
            val groupSize = style.groupSize?.takeIf { it in 1 until cellCount }
            val gaps = if (groupSize == null) 0 else (cellCount - 1) / groupSize
            val cellWidth = style.cellWidth.toPx()
            val spacing = style.spacing.toPx()
            val groupSpacing = style.groupSpacing.toPx()
            val natural = cellWidth * cellCount + spacing * (cellCount - 1 - gaps) + groupSpacing * gaps
            val scale = if (constraints.hasBoundedWidth && natural > constraints.maxWidth) {
                constraints.maxWidth / natural
            } else {
                1f
            }
            Snapshot.withoutReadObservation {
                if (contentScale.floatValue != scale) contentScale.floatValue = scale
            }

            val cellW = (cellWidth * scale).roundToInt().coerceAtLeast(1)
            val cellH = (style.cellHeight.toPx() * scale).roundToInt().coerceAtLeast(1)
            val cellConstraints = Constraints.fixed(cellW, cellH)
            val cells = measurables.drop(1).map { it.measure(cellConstraints) }
            val xs = IntArray(cellCount)
            var x = 0f
            for (index in 0 until cellCount) {
                if (index > 0) {
                    x += if (groupSize != null && index % groupSize == 0) groupSpacing * scale else spacing * scale
                }
                xs[index] = x.roundToInt()
                x += cellW
            }
            val contentWidth = x.roundToInt()
            val width = contentWidth.coerceIn(constraints.minWidth, constraints.maxWidth.coerceAtLeast(constraints.minWidth))
            val height = cellH.coerceIn(constraints.minHeight, constraints.maxHeight.coerceAtLeast(constraints.minHeight))
            val field = measurables[0].measure(Constraints.fixed(contentWidth.coerceAtLeast(1), cellH))
            val left = (width - contentWidth) / 2
            val top = (height - cellH) / 2
            layout(width, height) {
                field.place(left, top)
                cells.forEachIndexed { index, cell -> cell.place(left + xs[index], top) }
            }
        }

        val candidate = pasteCandidate
        if (candidate != null) {
            LaunchedEffect(candidate) {
                delay(4_000)
                pasteCandidate = null
            }
            val offsetY = with(LocalDensity.current) { -(style.cellHeight + 12.dp).roundToPx() }
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, offsetY),
                onDismissRequest = { pasteCandidate = null },
                properties = PopupProperties(focusable = true),
            ) {
                PasteBubble(
                    label = pasteLabel,
                    onClick = {
                        pasteCandidate = null
                        textState.setCode(currentEditor.paste(current(), candidate))
                        if (!readOnly) focusRequester.requestFocus()
                    },
                )
            }
        }
    }
}

@Composable
private fun PasteBubble(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .shadow(6.dp, shape)
            .background(MaterialTheme.colorScheme.inverseSurface, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        BasicText(
            text = label,
            style = MaterialTheme.typography.labelLarge.merge(
                TextStyle(color = MaterialTheme.colorScheme.inverseOnSurface),
            ),
        )
    }
}

/** Writes a code and its cursor into the hidden text field, skipping edits that change nothing. */
private fun TextFieldState.setCode(code: OtpInputState) {
    val selectionNow = TextRange(code.cursor)
    if (text.toString() == code.value && selection == selectionNow) return
    edit {
        if (asCharSequence().toString() != code.value) replace(0, this.length, code.value)
        selection = selectionNow
    }
}

/** Routes every keyboard, autofill and clipboard edit of the hidden text field through [OtpEditor]. */
private class OtpInputTransformation(private val editor: OtpEditor) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val original = originalText.toString()
        val before = OtpInputState(original, originalSelection.min.coerceIn(0, original.length))
        val after = editor.applyTextChange(before, asCharSequence())
        if (asCharSequence().toString() != after.value) replace(0, this.length, after.value)
        selection = TextRange(after.cursor)
    }
}
