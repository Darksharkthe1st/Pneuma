package com.farha.pneuma

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ClaudeService {

    // Do NOT commit this key.
    private const val API_KEY = "ipsjLFFJ01KTbRdd3QTCZXtAoKkgOcJC"
    private const val ENDPOINT = "https://api.mistral.ai/v1/chat/completions"

    private const val SYSTEM_PROMPT = """You are a clinical documentation assistant. Given patient vitals, a voice intake transcript, and a visual symptom description, output ONLY a valid JSON object with no markdown fences and no extra text:
{"subjective":"<what patient said, 1-2 sentences>","objective":"Heart Rate: <x> bpm\nAge/Sex: <x> / <x>\nVisual: <visual description>","assessment":{"urgency":"High Risk|Moderate Risk|Low Risk","narrative":"<1 sentence clinical summary>","flags":["<flag1>","<flag2>"]},"plan":["<step1>","<step2>"]}
Rules: urgency must be exactly "High Risk", "Moderate Risk", or "Low Risk". Format objective with literal \n between each line. Be concise and clinical."""

    suspend fun generateSoapNote(
        heartRate: Int,
        patientInfo: PatientInfo,
        transcript: List<TranscriptTurn>,
        visualSymptom: String,
    ): SoapNote = withContext(Dispatchers.IO) {
        val transcriptText = transcript.joinToString("\n") { "Q: ${it.question}\nA: ${it.answer}" }
        val userContent = buildString {
            appendLine("HR: ${heartRate}bpm. Age: ${patientInfo.age}${patientInfo.sex.firstOrNull() ?: ""}.")
            appendLine("Transcript:")
            appendLine(transcriptText)
            append("Visual: $visualSymptom")
        }

        val requestBody = JSONObject().apply {
            put("model", "mistral-small-latest")
            put("temperature", 0.2)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userContent)
                })
            })
        }.toString()

        val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Bearer $API_KEY")
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 30_000
            readTimeout = 30_000
        }

        try {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(requestBody) }
            val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
            parseResponse(responseText)
        } finally {
            conn.disconnect()
        }
    }

    private fun parseResponse(raw: String): SoapNote {
        val contentText = JSONObject(raw)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val obj = JSONObject(contentText)
        val assessmentObj = obj.getJSONObject("assessment")
        val flags = assessmentObj.getJSONArray("flags").let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        }
        val plan = obj.getJSONArray("plan").let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        }
        return SoapNote(
            subjective = obj.getString("subjective"),
            objective = obj.getString("objective"),
            assessment = Assessment(
                urgency = assessmentObj.getString("urgency"),
                narrative = assessmentObj.optString("narrative", ""),
                flags = flags,
            ),
            plan = plan,
        )
    }
}
