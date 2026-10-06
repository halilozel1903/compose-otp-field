package io.github.halilozel1903.otp.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val DEMO_CODE = "135790"
const val DEMO_PIN = "2580"
const val MASKED_PHONE = "+1 (555) ••• ••42"

/** The Lumen Bank mark (a sun over a horizon) and name. */
@Composable
fun LumenLogo(modifier: Modifier = Modifier, showName: Boolean = true) {
    val primary = MaterialTheme.colorScheme.primary
    val gold = MaterialTheme.colorScheme.tertiary
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(Modifier.size(30.dp)) {
            drawRoundRect(primary, cornerRadius = CornerRadius(size.width * 0.28f))
            drawCircle(gold, radius = size.width * 0.2f, center = Offset(size.width / 2f, size.height * 0.56f))
            drawLine(
                Color.White,
                Offset(size.width * 0.18f, size.height * 0.7f),
                Offset(size.width * 0.82f, size.height * 0.7f),
                strokeWidth = size.height * 0.08f,
                cap = StrokeCap.Round,
            )
        }
        if (showName) {
            Text("Lumen Bank", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/** A round avatar with initials. */
@Composable
fun Initials(text: String, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Box(
        modifier
            .size(size)
            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun BackArrow(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val stroke = size.width * 0.09f
        drawLine(color, Offset(size.width * 0.2f, size.height / 2f), Offset(size.width * 0.82f, size.height / 2f), stroke, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.2f, size.height / 2f), Offset(size.width * 0.46f, size.height * 0.24f), stroke, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.2f, size.height / 2f), Offset(size.width * 0.46f, size.height * 0.76f), stroke, StrokeCap.Round)
    }
}

@Composable
fun FingerprintIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(30.dp)) {
        val center = Offset(size.width / 2f, size.height * 0.56f)
        val stroke = Stroke(width = size.width * 0.06f, cap = StrokeCap.Round)
        for (i in 0 until 4) {
            val r = size.width * (0.12f + i * 0.11f)
            drawArc(
                color = color,
                startAngle = 200f - i * 6f,
                sweepAngle = 140f + i * 12f,
                useCenter = false,
                topLeft = Offset(center.x - r, center.y - r),
                size = Size(r * 2, r * 2),
                style = stroke,
            )
        }
        drawLine(color, Offset(center.x, center.y - size.width * 0.04f), Offset(center.x, size.height * 0.86f), stroke.width, StrokeCap.Round)
    }
}

/** A phone receiving a message with a code, guarded by a shield. */
@Composable
fun PhoneCodeIllustration(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Canvas(modifier) {
        val s = size.minDimension
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(scheme.primaryContainer, radius = s * 0.48f, center = c)
        drawCircle(scheme.primary.copy(alpha = 0.10f), radius = s * 0.36f, center = c)
        // Phone
        val pw = s * 0.36f
        val ph = s * 0.62f
        val pl = c.x - pw / 2f - s * 0.06f
        val pt = c.y - ph / 2f
        drawRoundRect(scheme.surfaceContainerLowest, Offset(pl, pt), Size(pw, ph), CornerRadius(s * 0.06f))
        drawRoundRect(scheme.primary, Offset(pl, pt), Size(pw, ph), CornerRadius(s * 0.06f), style = Stroke(s * 0.022f))
        drawLine(scheme.primary, Offset(pl + pw * 0.38f, pt + s * 0.045f), Offset(pl + pw * 0.62f, pt + s * 0.045f), s * 0.018f, StrokeCap.Round)
        // Message bubble with the code
        val bl = pl + pw * 0.38f
        val bt = pt + ph * 0.26f
        val bw = s * 0.42f
        val bh = s * 0.17f
        drawRoundRect(scheme.tertiaryContainer, Offset(bl, bt), Size(bw, bh), CornerRadius(bh * 0.4f))
        for (i in 0 until 4) {
            drawCircle(scheme.onTertiaryContainer, radius = s * 0.016f, center = Offset(bl + bw * (0.2f + i * 0.2f), bt + bh / 2f))
        }
        // Text lines
        for (i in 0 until 3) {
            val y = pt + ph * (0.6f + i * 0.1f)
            drawLine(scheme.outlineVariant, Offset(pl + pw * 0.18f, y), Offset(pl + pw * (0.82f - i * 0.12f), y), s * 0.02f, StrokeCap.Round)
        }
        drawShield(Offset(c.x + s * 0.2f, c.y + s * 0.17f), s * 0.2f, scheme.primary, scheme.onPrimary)
    }
}

/** A large shield with a check mark and floating code chips, for the tablet card. */
@Composable
fun ShieldIllustration(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Canvas(modifier) {
        val s = size.minDimension
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(scheme.surfaceContainerLowest.copy(alpha = 0.55f), radius = s * 0.46f, center = c)
        drawCircle(scheme.surfaceContainerLowest.copy(alpha = 0.8f), radius = s * 0.33f, center = c)
        drawShield(c, s * 0.42f, scheme.primary, scheme.onPrimary)
        // Code chips around the shield
        chip(Offset(c.x - s * 0.47f, c.y - s * 0.3f), s, scheme.tertiaryContainer, scheme.onTertiaryContainer)
        chip(Offset(c.x + s * 0.2f, c.y + s * 0.22f), s, scheme.surfaceContainerLowest, scheme.primary)
        drawCircle(scheme.tertiary, radius = s * 0.025f, center = Offset(c.x + s * 0.36f, c.y - s * 0.3f))
        drawCircle(scheme.primary.copy(alpha = 0.5f), radius = s * 0.018f, center = Offset(c.x - s * 0.36f, c.y + s * 0.34f))
    }
}

private fun DrawScope.chip(topLeft: Offset, s: Float, background: Color, dot: Color) {
    val w = s * 0.3f
    val h = s * 0.11f
    drawRoundRect(background, topLeft, Size(w, h), CornerRadius(h / 2f))
    for (i in 0 until 3) {
        drawCircle(dot, radius = s * 0.014f, center = Offset(topLeft.x + w * (0.27f + i * 0.23f), topLeft.y + h / 2f))
    }
}

private fun DrawScope.drawShield(center: Offset, height: Float, fill: Color, mark: Color) {
    val w = height * 0.82f
    val top = center.y - height / 2f
    val path = Path().apply {
        moveTo(center.x, top)
        lineTo(center.x + w / 2f, top + height * 0.16f)
        lineTo(center.x + w / 2f, top + height * 0.5f)
        cubicTo(
            center.x + w / 2f, top + height * 0.78f,
            center.x + w * 0.2f, top + height * 0.93f,
            center.x, top + height,
        )
        cubicTo(
            center.x - w * 0.2f, top + height * 0.93f,
            center.x - w / 2f, top + height * 0.78f,
            center.x - w / 2f, top + height * 0.5f,
        )
        lineTo(center.x - w / 2f, top + height * 0.16f)
        close()
    }
    drawPath(path, fill)
    val check = Path().apply {
        moveTo(center.x - w * 0.2f, center.y + height * 0.02f)
        lineTo(center.x - w * 0.04f, center.y + height * 0.17f)
        lineTo(center.x + w * 0.24f, center.y - height * 0.12f)
    }
    drawPath(check, mark, style = Stroke(width = height * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
