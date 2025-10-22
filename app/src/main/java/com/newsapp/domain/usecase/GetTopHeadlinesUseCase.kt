package com.newsapp.domain.usecase

import com.newsapp.core.util.Resource
import com.newsapp.domain.model.Article
import com.newsapp.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(source: String, forceRefresh: Boolean = false): Flow<Resource<List<Article>>> {
        return repository.getTopHeadlines(source, forceRefresh)
    }
}
