package com.lector.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String?,
    val filePath: String,       // Путь во внутренней памяти приложения
    val fileType: String,       // epub, fb2, pdf и т.д.
    val sizeBytes: Long,
    val sha256: String,         // Уникальный хэш файла для поиска дублей
    val coverPath: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long? = null,
    val isHidden: Boolean = false,
    val isDeleted: Boolean = false
)