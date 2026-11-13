
package com.jarvis.assistant.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object AiClient {

    private val client = OkHttpClient()

    suspend fun sendMessage(
        backendUrl: String,
        message: String
    ): String = withContext(Dispatchers.IO) {

        require(backendUrl.startsWith("https://")) {
            "Please configure a secure HTTPS backend URL."
        }

        val json = JSONObject()
            .put("message", message)

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(backendUrl)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->

            val responseText = response.body?.string()
                ?: throw Exception("Empty server response")

            if (!response.isSuccessful) {
                throw Exception("Server error: ${response.code}")
            }

            JSONObject(responseText).optString(
                "reply",
                "I couldn't understand the server response."
            )
        }
    }
}
