package ru.neon.checker

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.neon.checker.vm.CheckerViewModel
import java.io.File

class MainActivity : ComponentActivity() {
    private val vm: CheckerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installCrashLogger()
        super.onCreate(savedInstanceState)
        setContent { NeonApp(vm) }
    }

    private fun installCrashLogger() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            runCatching {
                val text = e.stackTraceToString()
                if (Build.VERSION.SDK_INT >= 29) {
                    val cv = ContentValues().apply { put(MediaStore.Downloads.DISPLAY_NAME, "neon_crash.txt") }
                    val uri = contentResolver.insert(MediaStore.Downloads.CONTENT_URI, cv)
                    uri?.let { u -> contentResolver.openOutputStream(u)?.use { o -> o.write(text.toByteArray()) } }
                } else {
                    File(Environment.getExternalStorageDirectory(), "Download/neon_crash.txt").writeText(text)
                }
            }
            Process.killProcess(Process.myPid())
        }
    }
}

@Composable
fun NeonApp(vm: CheckerViewModel) {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val report by vm.report.collectAsState()
        val busy by vm.busy.collectAsState()
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Button(onClick = { vm.check("https://github.com") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("GitHub") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.checkAll() }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Проверить все") }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.checkList(true) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Белый РФ") }
                Button(onClick = { vm.checkList(false) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Чёрный") }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.refreshLists() }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Обновить списки") }
                Button(onClick = { vm.export() }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Экспорт JSON") }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = report, modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()))
        }
    }
}
