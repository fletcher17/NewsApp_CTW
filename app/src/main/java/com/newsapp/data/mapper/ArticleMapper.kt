package com.newsapp.data.mapper

import com.newsapp.data.local.entity.ArticleEntity
import com.newsapp.data.local.entity.SourceEntity
import com.newsapp.data.remote.dto.ArticleDto
import com.newsapp.domain.model.Article
import com.newsapp.domain.model.Source
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun ArticleDto.toDomain(): Article {
    return Article(
        source = Source(
            id = source.id,
            name = source.name
        ),
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = ZonedDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME),
        content = content
    )
}

fun ArticleDto.toEntity(sourceId: String): ArticleEntity {
    return ArticleEntity(
        url = url,
        source = SourceEntity(
            sourceIdValue = source.id,
            sourceName = source.name
        ),
        author = author,
        title = title,
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content,
        sourceId = sourceId
    )
}

fun ArticleEntity.toDomain(): Article {
    return Article(
        source = Source(
            id = source.sourceIdValue,
            name = source.sourceName
        ),
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = ZonedDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME),
        content = content
    )
}
