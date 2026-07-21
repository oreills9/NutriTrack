package com.nutritrack.app.domain.nutritionlabel

data class ParsedNutritionLabel(
    val calories: Double? = null,
    val proteinG: Double? = null,
    val fatG: Double? = null,
    val carbsG: Double? = null,
    val servingSizeG: Double? = null,
) {
    val isEmpty: Boolean
        get() = calories == null && proteinG == null && fatG == null && carbsG == null && servingSizeG == null
}

// Best-effort regex extraction over raw OCR text from a nutrition facts label photo. Real labels
// vary a lot in layout/wording and OCR introduces its own noise, so this is deliberately
// forgiving (bounded non-digit gaps between a keyword and its number) rather than trying to match
// an exact label format - it's meant to get the user most of the way there, not to be perfect.
// The caller is expected to show the parsed values in an editable form for the user to confirm.
object NutritionLabelParser {

    // Up to ~20 non-digit characters (spaces, newlines, punctuation, "per serving" etc.) are
    // allowed between the keyword and its value, to tolerate OCR line-wrapping without matching
    // across unrelated sections of the label.
    private const val GAP = """\D{0,20}?"""

    private val CALORIES_REGEX = Regex("""calories$GAP(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
    private val PROTEIN_REGEX = Regex("""protein$GAP(\d+(?:\.\d+)?)\s*g\b""", RegexOption.IGNORE_CASE)
    private val TOTAL_FAT_REGEX = Regex("""total\s*fat$GAP(\d+(?:\.\d+)?)\s*g\b""", RegexOption.IGNORE_CASE)
    private val FAT_REGEX = Regex("""\bfat$GAP(\d+(?:\.\d+)?)\s*g\b""", RegexOption.IGNORE_CASE)
    private val TOTAL_CARB_REGEX = Regex("""total\s*carb\w*$GAP(\d+(?:\.\d+)?)\s*g\b""", RegexOption.IGNORE_CASE)
    private val CARB_REGEX = Regex("""carb\w*$GAP(\d+(?:\.\d+)?)\s*g\b""", RegexOption.IGNORE_CASE)
    private val SERVING_SIZE_REGEX = Regex("""serving\s*size$GAP(\d+(?:\.\d+)?)\s*g\b""", RegexOption.IGNORE_CASE)

    fun parse(rawText: String): ParsedNutritionLabel {
        val normalized = rawText.replace('\n', ' ')
        return ParsedNutritionLabel(
            calories = CALORIES_REGEX.find(normalized)?.groupValues?.get(1)?.toDoubleOrNull(),
            proteinG = PROTEIN_REGEX.find(normalized)?.groupValues?.get(1)?.toDoubleOrNull(),
            fatG = firstMatch(normalized, TOTAL_FAT_REGEX, FAT_REGEX),
            carbsG = firstMatch(normalized, TOTAL_CARB_REGEX, CARB_REGEX),
            servingSizeG = SERVING_SIZE_REGEX.find(normalized)?.groupValues?.get(1)?.toDoubleOrNull(),
        )
    }

    private fun firstMatch(text: String, preferred: Regex, fallback: Regex): Double? =
        (preferred.find(text) ?: fallback.find(text))?.groupValues?.get(1)?.toDoubleOrNull()
}
