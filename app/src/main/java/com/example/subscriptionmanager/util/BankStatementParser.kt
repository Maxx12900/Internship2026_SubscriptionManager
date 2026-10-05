package com.example.subscriptionmanager.util

import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

data class DetectedSubscription(
    val name: String,
    val price: Double,
    val packageName: String? = null,
    val paymentDate: Calendar = Calendar.getInstance(),
    val selected: Boolean = true
)

object BankStatementParser {

    private val knownKeywords = mapOf(
        // Streaming & Video
        "NETFLIX" to ("Netflix" to "com.netflix.mediaclient"),
        "YOUTUBE" to ("YouTube Premium" to "com.google.android.youtube"),
        "DISNEY" to ("Disney+" to "com.disney.disneyplus"),
        "HBO" to ("Max (HBO)" to "com.hbo.hbonow"),
        "MAX" to ("Max (HBO)" to "com.hbo.hbonow"),
        "HULU" to ("Hulu" to "com.hulu.plus"),
        "PARAMOUNT" to ("Paramount+" to "com.cbs.app"),
        "CRUNCHYROLL" to ("Crunchyroll" to "com.crunchyroll.crunchyroid"),
        "APPLE" to ("Apple Music / TV+" to null),
        "AMAZON PRIME" to ("Amazon Prime" to null),
        "TELEGRAM" to ("Telegram Premium" to "org.telegram.messenger"),

        // Music & Audio
        "SPOTIFY" to ("Spotify Premium" to "com.spotify.music"),
        "DEEZER" to ("Deezer Premium" to "deezer.android.app"),
        "SOUNDCLOUD" to ("SoundCloud Go" to "com.soundcloud.android"),
        "TIDAL" to ("Tidal HiFi" to "com.aspiro.tidal"),
        "AUDIBLE" to ("Audible" to "com.audible.application"),

        // Cloud & Storage
        "GOOGLE ONE" to ("Google One" to "com.google.android.apps.docs"),
        "GOOGLE" to ("Google One" to "com.google.android.apps.docs"),
        "DROPBOX" to ("Dropbox Plus" to "com.dropbox.android"),
        "ICLOUD" to ("Apple iCloud" to null),

        // Gaming
        "PLAYSTATION" to ("PlayStation Plus" to "com.scee.psxdocs"),
        "PS PLUS" to ("PlayStation Plus" to "com.scee.psxdocs"),
        "XBOX" to ("Xbox Game Pass" to "com.gamepass"),
        "GAME PASS" to ("Xbox Game Pass" to "com.gamepass"),
        "NINTENDO" to ("Nintendo Switch Online" to "com.nintendo.znca"),
        "TWITCH" to ("Twitch Sub" to "tv.twitch.android.app"),
        "DISCORD" to ("Discord Nitro" to "com.discord"),
        "PLAYIT" to ("playit.gg" to "gg.playit.app"),

        // AI & Productivity
        "CHATGPT" to ("ChatGPT Plus" to null),
        "OPENAI" to ("ChatGPT Plus" to null),
        "MICROSOFT" to ("Microsoft 365" to "com.microsoft.office.officehubrow"),
        "ADOBE" to ("Adobe Creative Cloud" to "com.adobe.reader"),
        "CANVA" to ("Canva Pro" to "com.canva.editor"),
        "NOTION" to ("Notion Plus" to "notion.id"),
        "GRAMMARLY" to ("Grammarly Premium" to "com.grammarly.android.keyboard"),
        "LINKEDIN" to ("LinkedIn Premium" to "com.linkedin.android"),

        // Education & Fitness
        "DUOLINGO" to ("Duolingo Super" to "com.duolingo"),
        "STRAVA" to ("Strava Summit" to "com.strava"),
        "HEADSPACE" to ("Headspace" to "com.getheadspace.android"),
        "CALM" to ("Calm Premium" to "com.calm.android")
    )

    private val dateFormats = listOf(
        Regex("""\b\d{4}-\d{2}-\d{2}\b""") to SimpleDateFormat("yyyy-MM-dd", Locale.US),
        Regex("""\b\d{4}/\d{2}/\d{2}\b""") to SimpleDateFormat("yyyy/MM/dd", Locale.US),
        Regex("""\b\d{4}\.\d{2}\.\d{2}\b""") to SimpleDateFormat("yyyy.MM.dd", Locale.US),
        Regex("""\b\d{2}/\d{2}/\d{4}\b""") to SimpleDateFormat("MM/dd/yyyy", Locale.US),
        Regex("""\b\d{2}-\d{2}-\d{4}\b""") to SimpleDateFormat("dd-MM-yyyy", Locale.US),
        Regex("""\b\d{2}\.\d{2}\.\d{4}\b""") to SimpleDateFormat("dd.MM.yyyy", Locale.US),
        Regex("""\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2},?\s+\d{4}\b""", RegexOption.IGNORE_CASE) to SimpleDateFormat("MMM dd, yyyy", Locale.US),
        Regex("""\b\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{4}\b""", RegexOption.IGNORE_CASE) to SimpleDateFormat("dd MMM yyyy", Locale.US)
    )

    private val amountRegex = Regex("""-?\s*[$€£¥]?\s*\d{1,3}(?:[,.]\d{3})*[.,]\d{2}\b""")

    private fun parseDateFlexible(text: String): Calendar? {
        for ((regex, format) in dateFormats) {
            val match = regex.find(text) ?: continue
            try {
                format.isLenient = false
                val parsed = format.parse(match.value) ?: continue
                return Calendar.getInstance().apply { time = parsed }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun parseAmount(text: String): Double? {
        val matches = amountRegex.findAll(text).map { it.value }.toList()
        if (matches.isEmpty()) return null

        val negative = matches.firstOrNull { it.contains("-") }
        val chosen = negative ?: matches.first()

        val lastSeparatorIndex = chosen.lastIndexOfAny(charArrayOf(',', '.'))
        if (lastSeparatorIndex == -1) return null

        val decimalPart = chosen.substring(lastSeparatorIndex + 1)
        val integerPart = chosen.substring(0, lastSeparatorIndex).filter { it.isDigit() || it == '-' }
        val normalized = "$integerPart.$decimalPart"

        return normalized.toDoubleOrNull()?.let { kotlin.math.abs(it) }
    }

    private fun splitCsvLine(line: String): List<String> {
        val columns = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    columns.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        columns.add(current.toString().trim())
        return columns
    }

    fun parseCsvStream(inputStream: InputStream): List<DetectedSubscription> {
        val lines = inputStream.bufferedReader().readLines()
        val dataLines = if (lines.firstOrNull()?.any { it.isDigit() } == false) lines.drop(1) else lines
        val rows = dataLines.map { line -> splitCsvLine(line).joinToString(" ") }
        return parseLines(rows)
    }

    fun parsePdfStream(inputStream: InputStream): List<DetectedSubscription> {
        return try {
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document) // Extracts 100% clean plain text!
            document.close()

            parseLines(text.lines())
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseLines(lines: List<String>): List<DetectedSubscription> {
        val detected = mutableListOf<DetectedSubscription>()

        // Combine adjacent lines so split PDF text blocks merge into full transaction rows
        val combinedLines = mutableListOf<String>()
        combinedLines.addAll(lines)

        for (i in 0 until lines.size - 1) {
            combinedLines.add("${lines[i]} ${lines[i + 1]}")
        }

        combinedLines.forEach { line ->
            val upperLine = line.uppercase()

            for ((keyword, info) in knownKeywords) {
                if (upperLine.contains(keyword)) {
                    val (subName, pkgName) = info
                    val price = parseAmount(line) ?: 9.99
                    val paymentDate = parseDateFlexible(line) ?: Calendar.getInstance()

                    detected.add(
                        DetectedSubscription(
                            name = subName,
                            price = price,
                            packageName = pkgName,
                            paymentDate = paymentDate
                        )
                    )
                    break
                }
            }
        }
        return detected.distinctBy { it.name }
    }
}

object NativePdfTextExtractor {

    fun extractTextFromPdfStream(inputStream: InputStream): List<String> {
        val bytes = inputStream.readBytes()
        val lines = mutableListOf<String>()

        try {
            val streams = extractDecodedStreams(bytes)
            for (streamBytes in streams) {
                val content = String(streamBytes, Charsets.ISO_8859_1)

                // Group text blocks (/BT ... /ET) into single lines
                val btRegex = Regex("""/BT[\s\S]*?/ET""")
                btRegex.findAll(content).forEach { btMatch ->
                    val blockText = parsePdfTextBlock(btMatch.value)
                    if (blockText.isNotBlank()) {
                        lines.add(blockText)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (lines.isEmpty()) {
            lines.addAll(rawByteScanFallback(bytes))
        }

        return lines.filter { it.isNotBlank() }
    }

    private fun parsePdfTextBlock(block: String): String {
        val sb = StringBuilder()
        val tokenRegex = Regex("""\((.*?)\)|<([0-9A-Fa-f]+)>""")

        tokenRegex.findAll(block).forEach { match ->
            val literal = match.groups[1]?.value
            val hex = match.groups[2]?.value

            if (literal != null) {
                val clean = literal
                    .replace("\\(", "(")
                    .replace("\\)", ")")
                    .replace("\\\\", "\\")
                sb.append(clean).append(" ")
            } else if (hex != null && hex.length % 2 == 0) {
                val hexText = decodeHexString(hex)
                if (hexText.isNotBlank()) {
                    sb.append(hexText).append(" ")
                }
            }
        }
        return sb.toString().trim()
    }

    private fun decodeHexString(hex: String): String {
        return try {
            val bytes = ByteArray(hex.length / 2)
            for (i in bytes.indices) {
                val index = i * 2
                bytes[i] = hex.substring(index, index + 2).toInt(16).toByte()
            }
            String(bytes, Charsets.UTF_8).filter {
                it.isLetterOrDigit() || it == ' ' || it == '.' || it == ',' || it == '-' || it == '$'
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractDecodedStreams(bytes: ByteArray): List<ByteArray> {
        val text = String(bytes, Charsets.ISO_8859_1)
        val result = mutableListOf<ByteArray>()
        val objRegex = Regex("""\d+\s+\d+\s+obj""")

        var searchFrom = 0
        while (true) {
            val objMatch = objRegex.find(text, searchFrom) ?: break
            val objStart = objMatch.range.last + 1

            val endObjIndex = text.indexOf("endobj", objStart).let { if (it == -1) text.length else it }
            val streamKeywordIndex = text.indexOf("stream", objStart)

            if (streamKeywordIndex == -1 || streamKeywordIndex > endObjIndex) {
                searchFrom = endObjIndex + 6
                continue
            }

            val dictText = text.substring(objStart, streamKeywordIndex)

            var binStart = streamKeywordIndex + "stream".length
            if (binStart < bytes.size && bytes[binStart] == '\r'.code.toByte()) binStart++
            if (binStart < bytes.size && bytes[binStart] == '\n'.code.toByte()) binStart++

            val lengthMatch = Regex("""/Length\s+(\d+)(?!\s+\d+\s+R)""").find(dictText)
            val endStreamIndex = text.indexOf("endstream", binStart)
            val binEnd = when {
                lengthMatch != null -> (binStart + lengthMatch.groupValues[1].toInt()).coerceAtMost(bytes.size)
                endStreamIndex != -1 -> endStreamIndex
                else -> bytes.size
            }

            if (binEnd > binStart) {
                val rawStream = bytes.copyOfRange(binStart, binEnd)
                val isFlateDecoded = dictText.contains("FlateDecode")

                if (isFlateDecoded) {
                    inflate(rawStream)?.let { result.add(it) }
                } else {
                    result.add(rawStream)
                }
            }

            searchFrom = (if (endStreamIndex != -1) endStreamIndex + 9 else endObjIndex + 6)
        }

        return result
    }

    private fun inflate(data: ByteArray): ByteArray? {
        return try {
            val inflater = java.util.zip.Inflater()
            inflater.setInput(data)
            val output = java.io.ByteArrayOutputStream(data.size * 3)
            val buffer = ByteArray(4096)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) break
                }
                output.write(buffer, 0, count)
            }
            inflater.end()
            output.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun rawByteScanFallback(bytes: ByteArray): List<String> {
        val lines = mutableListOf<String>()
        val sb = StringBuilder()
        for (b in bytes) {
            val char = b.toInt().toChar()
            if (char.isLetterOrDigit() || char == ' ' || char == '.' || char == ',' || char == '-' || char == '/' || char == '$') {
                sb.append(char)
            } else {
                if (sb.length >= 4) lines.add(sb.toString().trim())
                sb.clear()
            }
        }
        if (sb.length >= 4) lines.add(sb.toString().trim())
        return lines
    }
}