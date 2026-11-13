package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.TemplateCatalog
import com.example.util.CopyrightUtils
import com.example.util.CsvParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("IDVANTA", appName)
    }

    @Test
    fun `copyright footer uses dynamic year`() {
        val year = CopyrightUtils.currentYear()
        assertTrue(year >= 2026)
        assertEquals("© $year All Rights Reserved.", CopyrightUtils.copyrightText(year))
        assertEquals("© 2027 All Rights Reserved.", CopyrightUtils.copyrightText(2027))
    }

    @Test
    fun `template catalog meets minimum unique template counts`() {
        assertTrue(TemplateCatalog.cvTemplates.size >= 20)
        assertTrue(TemplateCatalog.idTemplates.size >= 15)
        assertTrue(TemplateCatalog.biodataTemplates.size >= 6)
        val allIds = TemplateCatalog.allTemplates.map { it.id }
        assertEquals(allIds.size, allIds.toSet().size)
    }

    @Test
    fun `csv parser handles hindi and english student records`() {
        val parsed = CsvParser.parseStudentsCsv(CsvParser.sampleCsvContent)
        assertEquals(3, parsed.records.size)
        assertEquals("Aarav Sharma", parsed.records[0].studentName)
        assertEquals("आरव गुप्ता", parsed.records[2].studentName)
        assertEquals("ADM-2026-003", parsed.records[2].admissionNumber)
    }
}
