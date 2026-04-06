package com.aether.connect.data.db

import androidx.room.*
import com.aether.connect.data.model.ClipboardEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEntries(): Flow<List<ClipboardEntry>>

    @Query("SELECT * FROM clipboard_history WHERE contentHash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): ClipboardEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ClipboardEntry)

    @Query("DELETE FROM clipboard_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM clipboard_history")
    suspend fun clearAll()

    @Query("DELETE FROM clipboard_history WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("SELECT COUNT(*) FROM clipboard_history")
    suspend fun getCount(): Int

    // Keep only the latest N entries
    @Query("DELETE FROM clipboard_history WHERE id NOT IN (SELECT id FROM clipboard_history ORDER BY timestamp DESC LIMIT :keep)")
    suspend fun trimToSize(keep: Int = 50)
}
