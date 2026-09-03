package com.lector.app.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject

class FileImporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Копирует файл из Uri в приватную папку приложения и одновременно считает SHA-256.
     * @return Pair(Скопированный файл, SHA-256 хэш)
     */
    fun copyAndHash(uri: Uri, fileName: String): Pair<File, String>? {
        val resolver = context.contentResolver
        val booksDir = File(context.filesDir, "books").apply { if (!exists()) mkdirs() }
        
        // Генерируем уникальное имя, чтобы избежать перезаписи при совпадении имен файлов
        val targetFile = File(booksDir, "${System.currentTimeMillis()}_$fileName")
        
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var read: Int
        
        try {
            resolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                    }
                }
            } ?: return null
            
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            return Pair(targetFile, hash)
        } catch (e: Exception) {
            if (targetFile.exists()) targetFile.delete()
            return null
        }
    }
    
    fun getFileName(uri: Uri): String {
        var result = "unknown_book"
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            }
        }
        if (result == "unknown_book") {
            result = uri.path?.let { File(it).name } ?: result
        }
        return result
    }
    
    fun getFileType(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }
}