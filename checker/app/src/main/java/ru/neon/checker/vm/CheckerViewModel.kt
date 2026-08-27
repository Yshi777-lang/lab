package ru.neon.checker.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.neon.checker.R
import ru.neon.checker.data.AppDatabase
import ru.neon.checker.data.CheckRecord
import ru.neon.checker.data.Checker
import ru.neon.checker.data.ListsStore
import java.io.File

class CheckerViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).checkDao()
    private val store = ListsStore(app)
    val history: Flow<List<CheckRecord>> = dao.getAll()
    private val _report = MutableStateFlow(app.getString(R.string.wait))
    val report: StateFlow<String> = _report
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy
    private fun runJob(block: suspend () -> Unit) = viewModelScope.launch { block() }
    private fun line(r: Checker.ProbeResult): String {
        val app = getApplication<Application>()
        return if (r.ok) "${app.getString(R.string.ok)} ${r.url} · HTTP ${r.code} · ${r.ms} ${app.getString(R.string.ms)}"
        else "${app.getString(R.string.fail)} ${r.url} · ${r.error}"
    }
    private suspend fun probeSave(url: String, listType: String, ip: String?): Checker.ProbeResult {
        val r = withContext(Dispatchers.IO) { Checker.probe(url) }
        dao.insert(CheckRecord(url = r.url, listType = listType, status = r.ok, latencyMs = r.ms.toInt(), exitIp = ip))
        return r
    }
    fun check(url: String) = runJob {
        val app = getApplication<Application>()
        _busy.value = true
        _report.value = app.getString(R.string.checking)
        _report.value = line(probeSave(url, "manual", null))
        _busy.value = false
    }
    fun checkAll() = runJob {
        val app = getApplication<Application>()
        _busy.value = true
        _report.value = app.getString(R.string.checking)
        val urls = listOf("https://github.com", "https://google.com", "https://chat.qwen.ai", "https://appteka.store", "https://t.me")
        val sb = StringBuilder()
        for (u in urls) {
            if (sb.isNotEmpty()) sb.append('\n')
            sb.append(line(probeSave(u, "all", null)))
        }
        _report.value = sb.toString()
        _busy.value = false
    }
    fun checkList(white: Boolean) = runJob {
        val app = getApplication<Application>()
        _busy.value = true
        _report.value = app.getString(R.string.checking)
        val ip = withContext(Dispatchers.IO) { Checker.exitIp() }
        val list = if (white) store.white() else store.black()
        val name = if (white) app.getString(R.string.white_rf) else app.getString(R.string.black)
        val sb = StringBuilder("$name · ${app.getString(R.string.exit_ip)} $ip")
        for (u in list) sb.append('\n').append(line(probeSave(u, if (white) "white" else "black", ip)))
        _report.value = sb.toString()
        _busy.value = false
    }
    fun refreshLists() = runJob {
        _busy.value = true
        val ok = withContext(Dispatchers.IO) { store.refreshFromRemote() }
        _report.value = if (ok) "✅ Списки обновлены с GitHub" else "⚠ GitHub недоступен — остались локальные"
        _busy.value = false
    }
    fun export() = runJob {
        val app = getApplication<Application>()
        val records = history.first()
        val arr = JSONArray()
        for (r in records) {
            arr.put(JSONObject().put("url", r.url).put("list", r.listType).put("ok", r.status).put("ms", r.latencyMs).put("ip", r.exitIp ?: "").put("at", r.timestamp))
        }
        val dir = app.getExternalFilesDir(null)!!
        val file = File(dir, "neon_history.json")
        withContext(Dispatchers.IO) { file.writeText(arr.toString(2)) }
        _report.value = "💾 ${file.absolutePath}"
    }
}
