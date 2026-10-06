package io.github.halilozel1903.otp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** How each cell of an [OtpField] is drawn. */
public enum class OtpCellShape {
    /** An outlined box with small corners. */
    Boxed,

    /** A filled box with large corners; the outline appears on the active cell. */
    Rounded,

    /** A line under each character, no box. */
    Underline,

    /** A filled circle, often used for PINs. */
    Circle,
}

/** Colors of an [OtpField]. Create them with [OtpFieldDefaults.colors]. */
@Immutable
public class OtpFieldColors(
    /** Fill of an empty cell. */
    public val container: Color,
    /** Fill of a filled cell. */
    public val filledContainer: Color,
    /** Fill of every cell while the field shows an error. */
    public val errorContainer: Color,
    /** Outline (or underline) of an empty cell. */
    public val border: Color,
    /** Outline of a filled cell. */
    public val filledBorder: Color,
    /** Outline of the active cell. */
    public val focusedBorder: Color,
    /** Outline of every cell while the field shows an error. */
    public val errorBorder: Color,
    /** Characters and secure dots. */
    public val content: Color,
    /** Characters and dots while the field shows an error. */
    public val errorContent: Color,
    /** The blinking cursor. */
    public val cursor: Color,
    /** The placeholder character of empty cells. */
    public val placeholder: Color,
    /** Outlines and characters of a disabled field. */
    public val disabled: Color,
) {
    /** A copy with some colors changed. */
    public fun copy(
        container: Color = this.container,
        filledContainer: Color = this.filledContainer,
        errorContainer: Color = this.errorContainer,
        border: Color = this.border,
        filledBorder: Color = this.filledBorder,
        focusedBorder: Color = this.focusedBorder,
        errorBorder: Color = this.errorBorder,
        content: Color = this.content,
        errorContent: Color = this.errorContent,
        cursor: Color = this.cursor,
        placeholder: Color = this.placeholder,
        disabled: Color = this.disabled,
    ): OtpFieldColors = OtpFieldColors(
        container, filledContainer, errorContainer, border, filledBorder, focusedBorder, errorBorder,
        content, errorContent, cursor, placeholder, disabled,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OtpFieldColors) return false
        return container == other.container && filledContainer == other.filledContainer &&
            errorContainer == other.errorContainer && border == other.border &&
            filledBorder == other.filledBorder && focusedBorder == other.focusedBorder &&
            errorBorder == other.errorBorder && content == other.content &&
            errorContent == other.errorContent && cursor == other.cursor &&
            placeholder == other.placeholder && disabled == other.disabled
    }

    override fun hashCode(): Int {
        var result = container.hashCode()
        for (color in listOf(
            filledContainer, errorContainer, border, filledBorder, focusedBorder, errorBorder,
            content, errorContent, cursor, placeholder, disabled,
        )) {
            result = 31 * result + color.hashCode()
        }
        return result
    }
}

/**
 * The look of an [OtpField]. Start from [OtpFieldDefaults.boxed], [OtpFieldDefaults.rounded],
 * [OtpFieldDefaults.underline] or [OtpFieldDefaults.circle] and change what you need with `copy`.
 *
 * Cells shrink evenly when the field gets less width than [cellWidth] times the number of cells needs.
 *
 * @property groupSize splits the cells into groups with [groupSpacing] between them, e.g. `3` for `123 456`.
 * @property placeholder drawn in empty cells, e.g. `'•'` or `'0'`; `null` for nothing.
 */
@Immutable
public data class OtpFieldStyle(
    val shape: OtpCellShape,
    val colors: OtpFieldColors,
    val textStyle: TextStyle,
    val cellWidth: Dp = 48.dp,
    val cellHeight: Dp = 56.dp,
    val spacing: Dp = 8.dp,
    val cornerRadius: Dp = 8.dp,
    val borderWidth: Dp = 1.dp,
    val focusedBorderWidth: Dp = 2.dp,
    val dotSize: Dp = 12.dp,
    val cursorWidth: Dp = 2.dp,
    val cursorHeightFraction: Float = 0.42f,
    val groupSize: Int? = null,
    val groupSpacing: Dp = 20.dp,
    val placeholder: Char? = null,
)

/** Default styles and colors for [OtpField], taken from the current [MaterialTheme]. */
public object OtpFieldDefaults {

    /** Colors from the Material 3 color scheme. */
    @Composable
    public fun colors(
        container: Color = MaterialTheme.colorScheme.surface,
        filledContainer: Color = container,
        errorContainer: Color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        border: Color = MaterialTheme.colorScheme.outlineVariant,
        filledBorder: Color = MaterialTheme.colorScheme.outline,
        focusedBorder: Color = MaterialTheme.colorScheme.primary,
        errorBorder: Color = MaterialTheme.colorScheme.error,
        content: Color = MaterialTheme.colorScheme.onSurface,
        errorContent: Color = MaterialTheme.colorScheme.error,
        cursor: Color = MaterialTheme.colorScheme.primary,
        placeholder: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        disabled: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): OtpFieldColors = OtpFieldColors(
        container = container,
        filledContainer = filledContainer,
        errorContainer = errorContainer,
        border = border,
        filledBorder = filledBorder,
        focusedBorder = focusedBorder,
        errorBorder = errorBorder,
        content = content,
        errorContent = errorContent,
        cursor = cursor,
        placeholder = placeholder,
        disabled = disabled,
    )

    /** Large, centered, semi-bold digits. */
    @Composable
    public fun textStyle(): TextStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
    )

    /** Outlined boxes with small corners. */
    @Composable
    public fun boxed(
        colors: OtpFieldColors = OtpFieldDefaults.colors(),
        textStyle: TextStyle = OtpFieldDefaults.textStyle(),
    ): OtpFieldStyle = OtpFieldStyle(
        shape = OtpCellShape.Boxed,
        colors = colors,
        textStyle = textStyle,
        cornerRadius = 8.dp,
    )

    /** Filled boxes with large corners and an outline on the active cell. */
    @Composable
    public fun rounded(
        colors: OtpFieldColors = OtpFieldDefaults.colors(
            container = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = Color.Transparent,
            filledBorder = Color.Transparent,
        ),
        textStyle: TextStyle = OtpFieldDefaults.textStyle(),
    ): OtpFieldStyle = OtpFieldStyle(
        shape = OtpCellShape.Rounded,
        colors = colors,
        textStyle = textStyle,
        cornerRadius = 16.dp,
        borderWidth = 0.dp,
    )

    /** A line under each character. */
    @Composable
    public fun underline(
        colors: OtpFieldColors = OtpFieldDefaults.colors(
            container = Color.Transparent,
            errorContainer = Color.Transparent,
        ),
        textStyle: TextStyle = OtpFieldDefaults.textStyle(),
    ): OtpFieldStyle = OtpFieldStyle(
        shape = OtpCellShape.Underline,
        colors = colors,
        textStyle = textStyle,
        cellWidth = 40.dp,
        cellHeight = 52.dp,
        spacing = 12.dp,
        borderWidth = 2.dp,
        focusedBorderWidth = 3.dp,
    )

    /** Filled circles, a good fit for PINs together with `secure = true`. */
    @Composable
    public fun circle(
        colors: OtpFieldColors = OtpFieldDefaults.colors(
            container = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = Color.Transparent,
            filledBorder = Color.Transparent,
        ),
        textStyle: TextStyle = OtpFieldDefaults.textStyle(),
    ): OtpFieldStyle = OtpFieldStyle(
        shape = OtpCellShape.Circle,
        colors = colors,
        textStyle = textStyle,
        cellWidth = 52.dp,
        cellHeight = 52.dp,
        spacing = 14.dp,
        borderWidth = 0.dp,
    )
}
