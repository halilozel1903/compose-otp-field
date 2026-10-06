package io.github.halilozel1903.otp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/** One drawn cell of an [OtpField]. */
@Composable
internal fun OtpCell(
    char: Char?,
    masked: Boolean,
    active: Boolean,
    showCursor: Boolean,
    blinkCursor: Boolean,
    isError: Boolean,
    enabled: Boolean,
    style: OtpFieldStyle,
    textStyle: TextStyle,
    contentScale: () -> Float,
    modifier: Modifier = Modifier,
) {
    val colors = style.colors
    val filled = char != null
    val borderTarget = when {
        !enabled -> colors.disabled
        isError -> colors.errorBorder
        active -> colors.focusedBorder
        filled -> colors.filledBorder
        else -> colors.border
    }
    val containerTarget = when {
        isError -> colors.errorContainer
        filled -> colors.filledContainer
        else -> colors.container
    }
    val contentColor = when {
        !enabled -> colors.disabled
        isError -> colors.errorContent
        else -> colors.content
    }
    val emphasized = (active || isError) && enabled
    val borderColor by animateColorAsState(borderTarget, animationSpec = tween(180), label = "otpBorder")
    val containerColor by animateColorAsState(containerTarget, animationSpec = tween(180), label = "otpContainer")
    val borderWidth by animateDpAsState(
        targetValue = if (emphasized) style.focusedBorderWidth else style.borderWidth,
        animationSpec = tween(180),
        label = "otpBorderWidth",
    )
    // Characters pop in with a little overshoot.
    val fill by animateFloatAsState(
        targetValue = if (filled) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "otpFill",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawCell(
                    shape = style.shape,
                    container = containerColor,
                    border = borderColor,
                    borderPx = borderWidth.toPx(),
                    cornerPx = style.cornerRadius.toPx(),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        when {
            char != null && masked -> {
                Canvas(
                    Modifier
                        .size(style.dotSize)
                        .graphicsLayer {
                            scaleX = fill * contentScale()
                            scaleY = fill * contentScale()
                        },
                ) {
                    drawCircle(contentColor)
                }
            }
            char != null -> {
                BasicText(
                    text = char.toString(),
                    style = textStyle.merge(TextStyle(color = contentColor)),
                    modifier = Modifier.graphicsLayer {
                        val scale = (0.6f + 0.4f * fill) * contentScale()
                        scaleX = scale
                        scaleY = scale
                        alpha = fill.coerceIn(0f, 1f)
                    },
                )
            }
            style.placeholder != null && !(active && showCursor) -> {
                BasicText(
                    text = style.placeholder.toString(),
                    style = textStyle.merge(TextStyle(color = colors.placeholder)),
                    modifier = Modifier.graphicsLayer {
                        scaleX = contentScale()
                        scaleY = contentScale()
                    },
                )
            }
        }
        if (active && showCursor && char == null && enabled) {
            OtpCursor(
                color = colors.cursor,
                width = style.cursorWidth,
                heightFraction = style.cursorHeightFraction,
                blink = blinkCursor,
            )
        }
    }
}

@Composable
private fun OtpCursor(color: Color, width: Dp, heightFraction: Float, blink: Boolean) {
    val alpha = if (blink) {
        val transition = rememberInfiniteTransition(label = "otpCursor")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
            label = "otpCursorPhase",
        )
        if (phase < 0.55f) 1f else 0f
    } else {
        1f
    }
    Canvas(Modifier.width(width).fillMaxHeight(heightFraction.coerceIn(0f, 1f))) {
        drawRoundRect(
            color = color.copy(alpha = color.alpha * alpha),
            cornerRadius = CornerRadius(size.width / 2f),
        )
    }
}

private fun DrawScope.drawCell(
    shape: OtpCellShape,
    container: Color,
    border: Color,
    borderPx: Float,
    cornerPx: Float,
) {
    val half = borderPx / 2f
    when (shape) {
        OtpCellShape.Boxed, OtpCellShape.Rounded -> {
            val radius = CornerRadius(cornerPx.coerceAtMost(size.minDimension / 2f))
            drawRoundRect(color = container, cornerRadius = radius)
            if (borderPx > 0f && border.alpha > 0f) {
                drawRoundRect(
                    color = border,
                    topLeft = Offset(half, half),
                    size = Size(size.width - borderPx, size.height - borderPx),
                    cornerRadius = CornerRadius((radius.x - half).coerceAtLeast(0f)),
                    style = Stroke(width = borderPx),
                )
            }
        }
        OtpCellShape.Circle -> {
            val radius = size.minDimension / 2f
            drawCircle(color = container, radius = radius)
            if (borderPx > 0f && border.alpha > 0f) {
                drawCircle(color = border, radius = radius - half, style = Stroke(width = borderPx))
            }
        }
        OtpCellShape.Underline -> {
            val radius = CornerRadius(cornerPx.coerceAtMost(size.minDimension / 2f))
            if (container.alpha > 0f) drawRoundRect(color = container, cornerRadius = radius)
            if (borderPx > 0f) {
                drawLine(
                    color = border,
                    start = Offset(half, size.height - half),
                    end = Offset(size.width - half, size.height - half),
                    strokeWidth = borderPx,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
