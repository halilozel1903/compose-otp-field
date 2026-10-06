package io.github.halilozel1903.otp.core

/**
 * The text of a code field and its cursor.
 *
 * Codes are contiguous: [value] never has gaps, so cell `i` is filled exactly when `i < value.length`.
 * The cursor points at the active cell. It is `value.length` (the first empty cell) while typing, can point
 * at a filled cell after a tap, and is `length` once the code is complete and no cell is active.
 */
public data class OtpInputState(
    val value: String = "",
    val cursor: Int = value.length,
) {
    init {
        require(cursor in 0..value.length) { "cursor must be in 0..${value.length}, was $cursor" }
    }

    /** The character in cell [index], or `null` when the cell is empty. */
    public fun charAt(index: Int): Char? = value.getOrNull(index)

    /** Whether cell [index] is filled. */
    public fun isFilled(index: Int): Boolean = index in value.indices
}

/**
 * Editing rules for a code field with one cell per character. Everything is a pure function of the state, so the
 * same rules drive the Compose field, a custom keypad and tests.
 *
 * - **Typing** writes at the cursor: into the first empty cell, or over a filled cell the user tapped. The cursor
 *   moves to the next cell. Input past the last cell is ignored.
 * - **Paste** fills from the cursor and trims what does not fit. A paste with at least `length` characters is
 *   taken as the whole code and replaces everything, wherever the cursor is.
 * - **Backspace** clears the active cell when it is filled (later characters move up one cell, codes stay
 *   contiguous) and otherwise deletes the previous character and moves back to it.
 * - **Tap** activates a filled cell, or the first empty cell when an empty one is tapped.
 */
public class OtpEditor(public val format: OtpFormat = OtpFormat.SixDigits) {

    public val length: Int get() = format.length

    /** A state for [value] with the cursor on the first empty cell; invalid characters are dropped. */
    public fun stateOf(value: CharSequence): OtpInputState {
        val clean = OtpNormalizer.sanitize(value, format)
        return OtpInputState(clean, clean.length)
    }

    /** Types or pastes [text] at the cursor. */
    public fun input(state: OtpInputState, text: CharSequence): OtpInputState {
        val chars = OtpNormalizer.normalize(text, format)
        if (chars.isEmpty()) return state
        if (chars.length >= length) {
            val code = chars.take(length)
            return OtpInputState(code, code.length)
        }
        val start = state.cursor.coerceAtMost(state.value.length)
        if (start >= length) return state
        val written = state.value.take(start) + chars + state.value.drop(start + chars.length)
        val value = written.take(length)
        return OtpInputState(value, (start + chars.length).coerceAtMost(value.length))
    }

    /** Same as [input]; named for readability at call sites that handle clipboard text. */
    public fun paste(state: OtpInputState, text: CharSequence): OtpInputState = input(state, text)

    /** Applies one backspace press. */
    public fun backspace(state: OtpInputState): OtpInputState {
        val value = state.value
        if (value.isEmpty()) return OtpInputState()
        val cursor = state.cursor.coerceAtMost(value.length)
        return if (cursor < value.length) {
            OtpInputState(value.removeRange(cursor, cursor + 1), cursor)
        } else {
            OtpInputState(value.dropLast(1), value.length - 1)
        }
    }

    /** Moves the cursor to the cell the user tapped. */
    public fun tap(state: OtpInputState, cell: Int): OtpInputState {
        val target = cell.coerceIn(0, length - 1).coerceAtMost(state.value.length)
        return state.copy(cursor = target)
    }

    /** An empty field. */
    public fun clear(): OtpInputState = OtpInputState()

    /** Whether the code in [state] fills every cell. */
    public fun isComplete(state: OtpInputState): Boolean = state.value.length == length

    /** Whether [value] fills every cell. */
    public fun isComplete(value: CharSequence): Boolean = value.length == length

    /** The cell to highlight: the cursor, or `null` when the code is complete and the cursor is past the end. */
    public fun activeCell(state: OtpInputState): Int? = state.cursor.takeIf { it < length }

    /**
     * Maps a raw edit of the whole text (what a keyboard, autofill or the clipboard did to the hidden text field)
     * to an editor action, so the result always follows the rules above.
     *
     * Insertions become [input] at the cursor, a single deleted character becomes [backspace], a deleted range is
     * removed, and replacements (autofill, a selected range typed over) put the new characters in place.
     */
    public fun applyTextChange(state: OtpInputState, newText: CharSequence): OtpInputState {
        val old = state.value
        val new = newText.toString()
        if (new == old) return state
        val cursor = state.cursor.coerceAtMost(old.length)

        // The common case: characters inserted at the cursor.
        if (new.length > old.length && new.startsWith(old.take(cursor)) && new.endsWith(old.drop(cursor))) {
            val inserted = new.substring(cursor, new.length - (old.length - cursor))
            return input(state.copy(cursor = cursor), inserted)
        }

        var prefix = 0
        val maxPrefix = minOf(old.length, new.length)
        while (prefix < maxPrefix && old[prefix] == new[prefix]) prefix++
        var suffix = 0
        val maxSuffix = minOf(old.length, new.length) - prefix
        while (suffix < maxSuffix && old[old.length - 1 - suffix] == new[new.length - 1 - suffix]) suffix++
        val removedEnd = old.length - suffix
        val inserted = new.substring(prefix, new.length - suffix)
        val removedCount = removedEnd - prefix

        return when {
            inserted.isEmpty() && removedCount == 1 -> backspace(state.copy(cursor = cursor))
            inserted.isEmpty() -> OtpInputState(old.removeRange(prefix, removedEnd), prefix)
            removedCount == 0 -> input(state.copy(cursor = prefix), inserted)
            else -> {
                val chars = OtpNormalizer.normalize(inserted, format)
                if (chars.length >= length) {
                    OtpInputState(chars.take(length), length)
                } else {
                    val value = (old.take(prefix) + chars + old.drop(removedEnd)).take(length)
                    OtpInputState(value, (prefix + chars.length).coerceAtMost(value.length))
                }
            }
        }
    }

    /**
     * The cell to reveal briefly in secure mode after [old] became [new]: the one character that was typed,
     * or `null` for pastes, deletions and programmatic changes of more than one character.
     */
    public fun revealedCell(old: CharSequence, new: CharSequence): Int? {
        if (new.length != old.length && new.length != old.length + 1) return null
        var index = 0
        while (index < old.length && index < new.length && old[index] == new[index]) index++
        if (index >= new.length) return null
        val rest = if (new.length == old.length + 1) {
            // Appended or inserted: everything after the new character must match.
            new.substring(index + 1) == old.substring(index)
        } else {
            new.substring(index + 1) == old.substring(index + 1)
        }
        return index.takeIf { rest }
    }
}
