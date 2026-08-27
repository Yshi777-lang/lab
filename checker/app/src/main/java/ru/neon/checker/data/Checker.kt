package ru.neon.checker.data

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

object Checker {
    const val LISTS_URL = "https://raw.githubusercontent.com/Yshi777-lang/lab/main/checker-lists.json"

    val DEFAULT_WHITE = listOf("https://yandex.ru", "https://gosuslugi.ru", "https://vk.com", "https://ozon.ru", "https://wildberries.ru", "https://sberbank.ru")
    val DEFAULT_BLACK = listOf("https://t.me", "https://discord.com", "https://x.com", "https://instagram.com", "https://facebook.com", "https://linkedin.com")

    data class ProbeResult(val url: String, val ok: Boolean, val code: Int, val ms: Long, val error: String?)

    fun probe(u0: String): ProbeResult {
        var u = u0
        if (!u.startsWith("http")) u = "https://$u"
        val t0 = System.currentTimeMillis()
        return try {
            val c = (URL(u).openConnection() as HttpURLConnection)
            c.connectTimeout = 8000
            c.readTimeout = 8000
            c.requestMethod = "GET"
            val code = c.responseCode
            ProbeResult(u, code in 200..399, code, System.currentTimeMillis() - t0, null)
        } catch (e: Exception) {
            ProbeResult(u, false, 0, System.currentTimeMillis() - t0, e.javaClass.simpleName)
        }
    }

    fun exitIp(): String = try {
        val c = (URL("https://api.ipify.org").openConnection() as HttpURLConnection)
        c.connectTimeout = 8000
        c.readTimeout = 8000
        c.inputStream.bufferedReader().readLine()?.trim() ?: "-"
    } catch (e: Exception) {
        "-"
    }

    private fun JSONArray.toStrList(): List<String> = (0 until length()).map { getString(it) }

    fun loadRemoteLists(): Pair<List<String>, List<String>>? = try {
        val c = (URL(LISTS_URL).openConnection() as HttpURLConnection)
        c.connectTimeout = 8000
        c.readTimeout = 8000
        if (c.responseCode == 200) {
            val o = JSONObject(c.inputStream.bufferedReader().readText())
            val w = o.optJSONArray("white")?.toStrList()
            val k = o.optJSONArray("black")?.toStrList()
            if (w != null && k != null && w.isNotEmpty() && k.isNotEmpty()) Pair(w, k) else null
        } else null
    } catch (e: Exception) {
        null
    }
}
