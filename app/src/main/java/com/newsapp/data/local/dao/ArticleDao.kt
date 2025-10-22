package com.newsapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newsapp.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    
    @Query("SELECT * FROM articles WHERE sourceId = :sourceId ORDER BY publishedAt DESC")
    fun getArticlesBySource(sourceId: String): Flow<List<ArticleEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)
    
    @Query("DELETE FROM articles WHERE sourceId = :sourceId")
    suspend fun deleteArticlesBySource(sourceId: String)
    
    @Query("DELETE FROM articles WHERE cachedAt < :timestamp")
    suspend fun deleteOldArticles(timestamp: Long)
}
