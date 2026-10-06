package io.github.halilozel1903.otp.core

/**
 * Finds the one-time code in an SMS, a notification or clipboard text.
 *
 * By default it looks for a group of [OtpFormat.length] digits that stands on its own, allowing one space or
 * dash inside (`482913`, `482-913`, `482 913`), and skips longer numbers such as phone or account numbers.
 * Alphanumeric formats look for a word of `length` letters and digits with at least one digit, so ordinary
 * uppercase words are not taken for codes.
 *
 * Pass a [pattern] for your own message format. When it has a group named `code`, or otherwise a first group,
 * that group is the code; else the whole match is. The match is normalized (separators dropped, full-width digits
 * converted) and only kept when it has exactly `length` characters.
 *
 * ```
 * OtpCodeExtractor(OtpFormat(6)).extract("Your Lumen code is 482-913. It expires in 10 minutes.") // "482913"
 * OtpCodeExtractor(OtpFormat(6), Regex("""G-(\d{6})""")).extract("G-104257 is your code")      // "104257"
 * ```
 */
public class OtpCodeExtractor(
    public val format: OtpFormat = OtpFormat.SixDigits,
    public val pattern: Regex? = null,
) {
    private val regex: Regex = pattern ?: defaultPattern(format)

    /** The first code in [message], or `null` when there is none. */
    public fun extract(message: CharSequence?): String? = extractAll(message).firstOrNull()

    /** Every code in [message], in order. */
    public fun extractAll(message: CharSequence?): List<String> {
        if (message.isNullOrEmpty()) return emptyList()
        val text = asciiDigits(message)
        return regex.findAll(text).mapNotNull { match ->
            val raw = codeGroup(match) ?: return@mapNotNull null
            OtpNormalizer.normalize(raw, format).takeIf { it.length == format.length }
        }.toList()
    }

    private fun codeGroup(match: MatchResult): String? {
        if (pattern != null) {
            val named = runCatching { match.groups["code"] }.getOrNull()
            if (named != null) return named.value
            if (match.groups.size > 1) return match.groups[1]?.value
        }
        return match.value
    }

    public companion object {
        /** The pattern [OtpCodeExtractor] uses for [format] when no pattern is given. */
        public fun defaultPattern(format: OtpFormat): Regex {
            val n = format.length
            return when (format.charset) {
                OtpCharset.Digits -> {
                    val body = if (n == 1) "\\d" else "\\d(?:[ \\-]?\\d){${n - 1}}"
                    // Not glued to letters or digits, and not part of a longer number like "+1 555 123 4567".
                    Regex("(?<!\\d[ \\-]?)(?<![\\p{L}\\d])$body(?![ \\-]?\\d)(?![\\p{L}\\d])")
                }
                OtpCharset.Alphanumeric -> {
                    val letters = "A-Za-z"
                    Regex("(?<![\\p{L}\\d])(?=[$letters\\d]{$n}(?![\\p{L}\\d]))[$letters]*\\d[$letters\\d]*(?![\\p{L}\\d])")
                }
            }
        }

        private fun asciiDigits(text: CharSequence): String = buildString(text.length) {
            for (char in text) {
                if (char !in '0'..'9' && Character.isDigit(char)) {
                    val digit = Character.digit(char, 10)
                    append(if (digit in 0..9) '0' + digit else char)
                } else {
                    append(char)
                }
            }
        }
    }
}
