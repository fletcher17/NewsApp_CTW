package com.newsapp.data.repository

import com.newsapp.core.util.Resource
import com.newsapp.data.local.dao.ArticleDao
import com.newsapp.data.mapper.toDomain
import com.newsapp.data.mapper.toEntity
import com.newsapp.data.remote.NewsApiService
import com.newsapp.domain.model.Article
import com.newsapp.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApiService,
    private val dao: ArticleDao,
    private val apiKey: String,
    private val sourceId: String
) : NewsRepository {

    companion object {
        private val CACHE_DURATION = TimeUnit.HOURS.toMillis(1)
    }

    override fun getTopHeadlines(
        source: String,
        forceRefresh: Boolean
    ): Flow<Resource<List<Article>>> = flow {
        
        // Emit loading with cached data
        emit(Resource.Loading())
        
        // Try to get cached data first
        val cachedArticles = dao.getArticlesBySource(source).first()
        val shouldFetchFromNetwork = forceRefresh || 
            cachedArticles.isEmpty() || 
            isCacheExpired(cachedArticles.firstOrNull()?.cachedAt)

        if (!shouldFetchFromNetwork) {
            emit(Resource.Success(cachedArticles.map { it.toDomain() }))
            return@flow
        }

        // Emit cached data while loading from network
        if (cachedArticles.isNotEmpty()) {
            emit(Resource.Loading(cachedArticles.map { it.toDomain() }))
        }

        try {
            val response = api.getTopHeadlines(
                sources = source,
                apiKey = apiKey
            )
            
            // Sort by date
            val sortedArticles = response.articles.sortedByDescending { it.publishedAt }
            
            // Cache the new data
            dao.deleteArticlesBySource(source)
            dao.insertArticles(sortedArticles.map { it.toEntity(source) })
            
            // Clean old cache
            dao.deleteOldArticles(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))
            
            // Emit success
            emit(Resource.Success(sortedArticles.map { it.toDomain() }))
            
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = "Network error: ${e.message()}",
                data = cachedArticles.map { it.toDomain() }.takeIf { it.isNotEmpty() }
            ))
        } catch (e: IOException) {
            emit(Resource.Error(
                message = "No internet connection. Showing cached data.",
                data = cachedArticles.map { it.toDomain() }.takeIf { it.isNotEmpty() }
            ))
        } catch (e: Exception) {
            emit(Resource.Error(
                message = "An unexpected error occurred: ${e.localizedMessage}",
                data = cachedArticles.map { it.toDomain() }.takeIf { it.isNotEmpty() }
            ))
        }
    }

    private fun isCacheExpired(cachedAt: Long?): Boolean {
        if (cachedAt == null) return true
        return System.currentTimeMillis() - cachedAt > CACHE_DURATION
    }
}
