package com.example.util

import android.os.Build
import java.time.Year
import java.util.Calendar

object CopyrightUtils {
    fun currentYear(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Year.now().value
        } else {
            Calendar.getInstance().get(Calendar.YEAR)
        }
    }

    fun copyrightText(year: Int = currentYear()): String {
        return "© $year All Rights Reserved."
    }
}
