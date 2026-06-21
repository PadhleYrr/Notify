package com.notifyforwarder.app

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object TelegramSender {
    private const val TAG = "TelegramSender"

    /**
     * Sends a text message to the configured Telegram chat.
     * Must be called from a background thread - performs blocking network IO.
     * Returns true on HTTP 200, false otherwise.
     */
    fun sendMessage(token: String, chatId: String, text: String): Boolean {
        if (token.isBlank() || chatId.isBlank()) {
            Log.w(TAG, "Missing token or chat id, skipping send")
            return false
        }
        return try {
            val url = URL("https://api.telegram.org/bot$token/sendMessage")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val body = "chat_id=${enc(chatId)}&text=${enc(text)}&disable_web_page_preview=true"
            conn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }

            val code = conn.responseCode
            if (code != 200) {
                val err = conn.errorStream?.bufferedReader()?.readText()
                Log.e(TAG, "Telegram send failed, code=$code body=$err")
            }
            conn.disconnect()
            code == 200
        } catch (e: Exception) {
            Log.e(TAG, "Telegram send exception", e)
            false
        }
    }

    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")
}
