package com.example.util

import com.example.data.local.StudentRecordEntity

data class CsvParseResult(
    val records: List<StudentRecordEntity>,
    val skippedRows: Int,
    val errors: List<String>
)

object CsvParser {

    val sampleCsvContent: String = """
        studentName,fatherName,motherName,className,section,rollNumber,admissionNumber,dateOfBirth,address,emergencyContact,bloodGroup,academicSession,validityDate,schoolName
        Aarav Sharma,Rajesh Sharma,Sunita Sharma,10,A,101,ADM-2026-001,15/05/2011,Sector 14 Gurugram,+91 98110 22334,B+,2026-27,31/03/2027,Unity Public Senior Secondary School
        Ananya Verma,Vikram Verma,Kavita Verma,10,A,102,ADM-2026-002,22/08/2011,Model Town Delhi,+91 98112 44556,O+,2026-27,31/03/2027,Unity Public Senior Secondary School
        आरव गुप्ता,सुनील गुप्ता,मीरा गुप्ता,9,B,204,ADM-2026-003,03/11/2012,सिविल लाइन्स प्रयागराज,+91 98390 77889,A+,2026-27,31/03/2027,यूनिटी पब्लिक स्कूल
    """.trimIndent()

    fun parseStudentsCsv(csvText: String, defaultSchoolName: String = ""): CsvParseResult {
        val lines = csvText
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return CsvParseResult(emptyList(), 0, listOf("CSV file is empty."))
        }

        val headerTokens = splitCsvLine(lines.first()).map { it.trim().lowercase() }
        val hasHeader = headerTokens.any {
            it.contains("name") || it.contains("roll") || it.contains("admission") || it.contains("class")
        }

        val dataLines = if (hasHeader) lines.drop(1) else lines
        val colIndex = if (hasHeader) buildHeaderMap(headerTokens) else emptyMap()

        val parsed = mutableListOf<StudentRecordEntity>()
        val errors = mutableListOf<String>()
        var skipped = 0

        dataLines.forEachIndexed { idx, line ->
            val cols = splitCsvLine(line)
            val rowNum = if (hasHeader) idx + 2 else idx + 1

            fun getCol(key: String, fallbackPos: Int): String {
                val i = colIndex[key] ?: fallbackPos
                return cols.getOrNull(i)?.trim().orEmpty()
            }

            val studentName = getCol("studentname", 0)
            if (studentName.isBlank()) {
                skipped++
                errors.add("Row $rowNum: Missing student name.")
                return@forEachIndexed
            }

            val schoolName = getCol("schoolname", 13).ifBlank { defaultSchoolName }
            parsed.add(
                StudentRecordEntity(
                    batchName = "Imported Batch",
                    schoolName = schoolName,
                    studentName = studentName,
                    fatherName = getCol("fathername", 1),
                    motherName = getCol("mothername", 2),
                    className = getCol("classname", 3),
                    section = getCol("section", 4),
                    rollNumber = getCol("rollnumber", 5),
                    admissionNumber = getCol("admissionnumber", 6),
                    dateOfBirth = getCol("dateofbirth", 7),
                    address = getCol("address", 8),
                    emergencyContact = getCol("emergencycontact", 9),
                    bloodGroup = getCol("bloodgroup", 10),
                    academicSession = getCol("academicsession", 11).ifBlank { "2026-27" },
                    validityDate = getCol("validitydate", 12),
                    isSelected = true
                )
            )
        }

        return CsvParseResult(parsed, skipped, errors)
    }

    private fun buildHeaderMap(headers: List<String>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        headers.forEachIndexed { index, raw ->
            val key = raw.replace("_", "").replace(" ", "").replace("'", "")
            when {
                key in listOf("studentname", "name", "student") -> map["studentname"] = index
                key in listOf("fathername", "father", "fathersname") -> map["fathername"] = index
                key in listOf("mothername", "mother", "mothersname") -> map["mothername"] = index
                key in listOf("classname", "class", "grade", "std") -> map["classname"] = index
                key in listOf("section", "sec", "division") -> map["section"] = index
                key in listOf("rollnumber", "rollno", "roll") -> map["rollnumber"] = index
                key in listOf("admissionnumber", "admissionno", "admno", "admission") -> map["admissionnumber"] = index
                key in listOf("dateofbirth", "dob", "birthdate") -> map["dateofbirth"] = index
                key in listOf("address", "residence") -> map["address"] = index
                key in listOf("emergencycontact", "contact", "phone", "mobile") -> map["emergencycontact"] = index
                key in listOf("bloodgroup", "blood") -> map["bloodgroup"] = index
                key in listOf("academicsession", "session", "year") -> map["academicsession"] = index
                key in listOf("validitydate", "validupto", "validity") -> map["validitydate"] = index
                key in listOf("schoolname", "school", "institution") -> map["schoolname"] = index
            }
        }
        return map
    }

    fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
