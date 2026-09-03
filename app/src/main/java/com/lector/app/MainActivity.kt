package com.lector.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lector.app.ui.navigation.LectorNavHost
import com.lector.app.ui.theme.LectorTheme
import com.lector.app.worker.ImportWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        handleIncomingIntent(intent) // Обработка при запуске
        
        setContent {
            LectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LectorNavHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent) // Обработка, если приложение уже открыто
    }

    private fun handleIncomingIntent(intent: Intent?) {
        intent ?: return
        
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                uri?.let { enqueueImport(it) }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                uris?.forEach { enqueueImport(it) }
            }
        }
    }

    private fun enqueueImport(uri: Uri) {
        val workRequest = OneTimeWorkRequestBuilder<ImportWorker>()
            .setInputData(Data.Builder().putString(ImportWorker.KEY_URI, uri.toString()).build())
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "${ImportWorker.WORK_NAME_PREFIX}${System.currentTimeMillis()}",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
        
        Toast.makeText(this, "Импорт книги запущен...", Toast.LENGTH_SHORT).show()
    }
}