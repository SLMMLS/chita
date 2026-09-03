package com.lector.app.ui.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lector.app.worker.ImportWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLibrary: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val context = LocalContext.current
    
    // Системный пикер файлов (выбор нескольких документов)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            val workRequest = OneTimeWorkRequestBuilder<ImportWorker>()
                .setInputData(Data.Builder().putString(ImportWorker.KEY_URI, uri.toString()).build())
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${ImportWorker.WORK_NAME_PREFIX}${System.currentTimeMillis()}",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
        }
        if (uris.isNotEmpty()) {
            Toast.makeText(context, "Запущен импорт ${uris.size} файл(ов)", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lector") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Открываем пикер для всех типов файлов (или можно ограничить MIME-типами)
                    filePickerLauncher.launch(arrayOf("*/*")) 
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить книги")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Добро пожаловать в Lector!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Ваша приватная офлайн-библиотека",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = onNavigateToLibrary) {
                Text("Перейти к книгам")
            }
        }
    }
}