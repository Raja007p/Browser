package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Bookmark
import com.example.data.model.BrowserExtension
import com.example.data.model.HistoryItem
import com.example.data.model.WebApp
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): Bookmark?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAll()
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem): Long

    @Delete
    suspend fun deleteHistory(item: HistoryItem)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}

@Dao
interface WebAppDao {
    @Query("SELECT * FROM web_apps ORDER BY installedAt DESC")
    fun getAllWebApps(): Flow<List<WebApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebApp(webApp: WebApp): Long

    @Delete
    suspend fun deleteWebApp(webApp: WebApp)

    @Query("DELETE FROM web_apps WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM web_apps")
    suspend fun clearAll()
}

@Dao
interface ExtensionDao {
    @Query("SELECT * FROM extensions")
    fun getAllExtensions(): Flow<List<BrowserExtension>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtension(extension: BrowserExtension)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(extensions: List<BrowserExtension>)

    @Update
    suspend fun updateExtension(extension: BrowserExtension)

    @Delete
    suspend fun deleteExtension(extension: BrowserExtension)

    @Query("DELETE FROM extensions")
    suspend fun clearAll()
}
