package com.example.data.util

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {

    /**
     * Extracts "YYYY-MM" from date string formats such as "YYYY-MM-DD", "YYYY-MM",
     * "DD-MM-YYYY", "DD/MM/YYYY", etc.
     */
    fun extractMonthYear(dateString: String?): String? {
        if (dateString.isNullOrBlank()) return null
        val trimmed = dateString.trim()

        // 1. Standard ISO format: "YYYY-MM-DD" or "YYYY-MM"
        if (trimmed.length >= 7 && (trimmed[4] == '-' || trimmed[4] == '/')) {
            val y = trimmed.substring(0, 4)
            val m = trimmed.substring(5, 7)
            if (y.all { it.isDigit() } && m.all { it.isDigit() }) {
                return "$y-$m"
            }
        }

        // 2. DD-MM-YYYY or DD/MM/YYYY
        if (trimmed.length >= 10 && (trimmed[2] == '-' || trimmed[2] == '/') && (trimmed[5] == '-' || trimmed[5] == '/')) {
            val parts = trimmed.split('-', '/')
            if (parts.size == 3 && parts[2].length == 4 && parts[2].all { it.isDigit() }) {
                val year = parts[2]
                val month = parts[1].padStart(2, '0')
                return "$year-$month"
            }
        }

        // 3. Fallback parsing
        val patterns = listOf("yyyy-MM-dd", "yyyy-MM", "dd-MM-yyyy", "dd/MM/yyyy", "yyyy/MM/dd")
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.US)
                sdf.isLenient = false
                val date = sdf.parse(trimmed)
                if (date != null) {
                    return SimpleDateFormat("yyyy-MM", Locale.US).format(date)
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    /**
     * Checks whether the student's admission/joining date is on or before the target month ("YYYY-MM").
     * Returns true if joiningDate is blank/null (to prevent excluding unassigned students).
     * Otherwise returns true only if the admission month is <= targetMonth.
     */
    fun isStudentAdmittedInOrBefore(joiningDate: String?, targetMonth: String): Boolean {
        if (joiningDate.isNullOrBlank()) return true
        val admissionMonth = extractMonthYear(joiningDate) ?: return true
        return admissionMonth <= targetMonth
    }
}
