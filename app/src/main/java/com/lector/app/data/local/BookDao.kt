package com.lector.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lector.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE sha256 = :hash LIMIT 1")
    suspend fun getBookByHash(hash: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity): Long
    
    @Query("SELECT * FROM books WHERE isDeleted = 0 ORDER BY addedAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>
}