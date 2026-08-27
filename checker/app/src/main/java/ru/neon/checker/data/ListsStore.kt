package ru.neon.checker.data

import android.content.Context
import android.content.SharedPreferences

class ListsStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lists", Context.MODE_PRIVATE)

    fun white(): List<String> = read("white") ?: Checker.DEFAULT_WHITE
    fun black(): List<String> = read("black") ?: Checker.DEFAULT_BLACK

    fun saveWhite(list: List<String>) = write("white", list)
    fun saveBlack(list: List<String>) = write("black", list)

    fun refreshFromRemote(): Boolean {
        val remote = Checker.loadRemoteLists() ?: return false
        write("white", remote.first)
        write("black", remote.second)
        return true
    }

    private fun read(key: String): List<String>? =
        prefs.getString(key, null)?.split("\n")?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }

    private fun write(key: String, list: List<String>) =
        prefs.edit().putString(key, list.joinToString("\n")).apply()
}
