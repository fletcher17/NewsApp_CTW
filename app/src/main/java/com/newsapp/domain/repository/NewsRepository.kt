package com.newsapp.domain.repository

import com.newsapp.core.util.Resource
import com.newsapp.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getTopHeadlines(source: String, forceRefresh: Boolean = false): Flow<Resource<List<Article>>>
}
