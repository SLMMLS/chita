package com.lector.app.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.lector.app.data.local.BookDao
import com.lector.app.data.local.entity.BookEntity
import com.lector.app.parser.BookParserFactory
import com.lector.app.utils.FileImporter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class ImportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileImporter: FileImporter,
    private val bookDao: BookDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        val uriString = inputData.getString(KEY_URI)

        if (uriString.isNullOrBlank()) {
            return failure("Не передан URI файла")
        }

        val uri = runCatching { Uri.parse(uriString) }
            .getOrNull()
            ?: return failure("Некорректный URI файла")

        return runCatching {
            importFile(uri)
        }.getOrElse { error ->
            failure("Ошибка импорта: ${error.message}")
        }
    }

    private suspend fun importFile(uri: Uri): ListenableWorker.Result {
        val fileName = withContext(Dispatchers.IO) {
            fileImporter.getFileName(uri).orEmpty()
        }.ifBlank {
            DEFAULT_FILE_NAME
        }

        val fileType = fileImporter.getFileType(fileName).lowercase()

        if (fileType.isBlank()) {
            return failure("Не удалось определить расширение файла")
        }

        val parser = BookParserFactory.getParser(fileType)

        if (parser == null) {
            return failure("Формат .$fileType пока не поддерживается")
        }

        val copiedFileAndHash = withContext(Dispatchers.IO) {
            fileImporter.copyAndHash(uri, fileName)
        }

        if (copiedFileAndHash == null) {
            return failure("Не удалось скопировать файл в приватное хранилище")
        }

        val (file, sha256) = copiedFileAndHash

        if (!file.exists() || file.length() == 0L) {
            deleteQuietly(file)
            return failure("Файл пуст или не был корректно скопирован")
        }

        val existingBook = bookDao.getBookByHash(sha256)

        if (existingBook != null) {
            deleteQuietly(file)
            return success("Книга уже есть в библиотеке")
        }

        val parseResult = try {
            parser.parse(file)
        } catch (e: Exception) {
            deleteQuietly(file)
            return failure("Не удалось разобрать файл: ${e.message}")
        }

        val title = parseResult.metadata.title
            .trim()
            .ifBlank {
                fileName.substringBeforeLast('.').ifBlank { fileName }
            }

        val author = parseResult.metadata.author
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val bookEntity = BookEntity(
            title = title,
            author = author,
            filePath = file.absolutePath,
            fileType = fileType,
            sizeBytes = file.length(),
            sha256 = sha256,
            coverPath = null,
            addedAt = System.currentTimeMillis(),
            lastOpenedAt = null,
            isHidden = false,
            isDeleted = false
        )

        bookDao.insert(bookEntity)

        return success("Книга добавлена")
    }

    private fun deleteQuietly(file: File) {
        runCatching {
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun success(message: String): ListenableWorker.Result {
        return ListenableWorker.Result.success(
            Data.Builder()
                .putString(KEY_RESULT_MESSAGE, message)
                .build()
        )
    }

    private fun failure(message: String): ListenableWorker.Result {
        return ListenableWorker.Result.failure(
            Data.Builder()
                .putString(KEY_RESULT_MESSAGE, message)
                .build()
        )
    }

    companion object {
        const val KEY_URI = "key_uri"
        const val KEY_RESULT_MESSAGE = "key_result_message"
        const val WORK_NAME_PREFIX = "import_book_"

        private const val DEFAULT_FILE_NAME = "unknown"
    }
}