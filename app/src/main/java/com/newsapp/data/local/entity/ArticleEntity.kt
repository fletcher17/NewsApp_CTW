package com.newsapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val url: String,
    @Embedded
    val source: SourceEntity,
    val author: String?,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?,
    val sourceId: String,
    val cachedAt: Long = System.currentTimeMillis()
)

data class SourceEntity(
    val sourceIdValue: String?,
    val sourceName: String
)
