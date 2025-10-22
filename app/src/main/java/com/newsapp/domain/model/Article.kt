package com.newsapp.domain.model

import java.time.ZonedDateTime

data class Article(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: ZonedDateTime,
    val content: String?
)

data class Source(
    val id: String?,
    val name: String
)
