package io.github.halilozel1903.otp

import androidx.compose.ui.platform.Clipboard

/** The plain text of the first clipboard item, or `null`. */
internal suspend fun Clipboard.readText(): String? {
    val clip = getClipEntry()?.clipData ?: return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0)?.text?.toString()
}
