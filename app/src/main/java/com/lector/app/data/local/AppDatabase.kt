package com.lector.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lector.app.data.local.entity.BookEntity

@Database(entities = [BookEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}