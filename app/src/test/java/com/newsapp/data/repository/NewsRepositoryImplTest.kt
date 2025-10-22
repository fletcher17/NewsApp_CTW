package com.newsapp.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.newsapp.core.util.Resource
import com.newsapp.data.local.dao.ArticleDao
import com.newsapp.data.local.entity.ArticleEntity
import com.newsapp.data.local.entity.SourceEntity
import com.newsapp.data.remote.NewsApiService
import com.newsapp.data.remote.dto.ArticleDto
import com.newsapp.data.remote.dto.NewsResponse
import com.newsapp.data.remote.dto.SourceDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

class NewsRepositoryImplTest {

    private lateinit var repository: NewsRepositoryImpl
    private lateinit var api: NewsApiService
    private lateinit var dao: ArticleDao

    private val testApiKey = "test_api_key"
    private val testSourceId = "bbc-news"

    @Before
    fun setup() {
        api = mockk()
        dao = mockk()
        repository = NewsRepositoryImpl(api, dao, testApiKey, testSourceId)
    }

    @Test
    fun `getTopHeadlines returns success when API call succeeds with empty cache`() = runTest {
        // Given
        val articles = listOf(
            ArticleDto(
                source = SourceDto("bbc-news", "BBC News"),
                author = "John Doe",
                title = "Test Title",
                description = "Test Description",
                url = "https://test.com",
                urlToImage = "https://test.com/image.jpg",
                publishedAt = "2024-01-01T12:00:00Z",
                content = "Test content"
            )
        )
        val response = NewsResponse("ok", 1, articles)

        every { dao.getArticlesBySource(testSourceId) } returns flowOf(emptyList())
        coEvery { api.getTopHeadlines(testSourceId, testApiKey) } returns response
        coEvery { dao.deleteArticlesBySource(any()) } returns Unit
        coEvery { dao.insertArticles(any()) } returns Unit
        coEvery { dao.deleteOldArticles(any()) } returns Unit

        // When & Then
        repository.getTopHeadlines(testSourceId).test {
            // Loading state
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            // Success state
            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).hasSize(1)
            assertThat(success.data?.first()?.title).isEqualTo("Test Title")

            awaitComplete()
        }

        coVerify { dao.insertArticles(any()) }
        coVerify { dao.deleteArticlesBySource(testSourceId) }
    }

    @Test
    fun `getTopHeadlines returns cached data when network fails`() = runTest {
        // Given
        val cachedEntities = listOf(
            ArticleEntity(
                url = "https://test.com",
                source = SourceEntity("bbc-news", "BBC News"),
                author = "John Doe",
                title = "Cached Title",
                description = "Cached Description",
                urlToImage = "https://test.com/image.jpg",
                publishedAt = "2024-01-01T12:00:00Z",
                content = "Cached content",
                sourceId = testSourceId,
                cachedAt = System.currentTimeMillis()
            )
        )

        every { dao.getArticlesBySource(testSourceId) } returns flowOf(cachedEntities)
        coEvery { api.getTopHeadlines(testSourceId, testApiKey) } throws IOException("Network error")

        // When & Then
        repository.getTopHeadlines(testSourceId, forceRefresh = true).test {
            // Loading state
            val loading1 = awaitItem()
            assertThat(loading1).isInstanceOf(Resource.Loading::class.java)

            // Loading with cached data
            val loading2 = awaitItem()
            assertThat(loading2).isInstanceOf(Resource.Loading::class.java)
            assertThat(loading2.data).hasSize(1)

            // Error with cached data
            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.data).hasSize(1)
            assertThat(error.data?.first()?.title).isEqualTo("Cached Title")

            awaitComplete()
        }
    }

    @Test
    fun `getTopHeadlines handles HTTP exception`() = runTest {
        // Given
        val httpException = mockk<HttpException>()
        every { httpException.message() } returns "HTTP 404"

        every { dao.getArticlesBySource(testSourceId) } returns flowOf(emptyList())
        coEvery { api.getTopHeadlines(testSourceId, testApiKey) } throws httpException

        // When & Then
        repository.getTopHeadlines(testSourceId, forceRefresh = true).test {
            // Loading state
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            // Error state
            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).contains("Network error")

            awaitComplete()
        }
    }

    @Test
    fun `getTopHeadlines returns cached data when not expired and forceRefresh is false`() = runTest {
        // Given
        val recentCachedEntities = listOf(
            ArticleEntity(
                url = "https://test.com",
                source = SourceEntity("bbc-news", "BBC News"),
                author = "John Doe",
                title = "Recent Cached Title",
                description = "Recent Cached Description",
                urlToImage = "https://test.com/image.jpg",
                publishedAt = "2024-01-01T12:00:00Z",
                content = "Recent cached content",
                sourceId = testSourceId,
                cachedAt = System.currentTimeMillis() // Recent cache
            )
        )

        every { dao.getArticlesBySource(testSourceId) } returns flowOf(recentCachedEntities)

        // When & Then
        repository.getTopHeadlines(testSourceId, forceRefresh = false).test {
            // Loading state
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            // Success with cached data (no network call)
            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).hasSize(1)
            assertThat(success.data?.first()?.title).isEqualTo("Recent Cached Title")

            awaitComplete()
        }

        // Verify no network call was made
        coVerify(exactly = 0) { api.getTopHeadlines(any(), any()) }
    }

    @Test
    fun `getTopHeadlines sorts articles by date in descending order`() = runTest {
        // Given
        val articles = listOf(
            ArticleDto(
                source = SourceDto("bbc-news", "BBC News"),
                author = "John Doe",
                title = "Older Article",
                description = "Test Description",
                url = "https://test.com/1",
                urlToImage = null,
                publishedAt = "2024-01-01T12:00:00Z",
                content = "Test content"
            ),
            ArticleDto(
                source = SourceDto("bbc-news", "BBC News"),
                author = "Jane Doe",
                title = "Newer Article",
                description = "Test Description",
                url = "https://test.com/2",
                urlToImage = null,
                publishedAt = "2024-01-02T12:00:00Z",
                content = "Test content"
            )
        )
        val response = NewsResponse("ok", 2, articles)

        every { dao.getArticlesBySource(testSourceId) } returns flowOf(emptyList())
        coEvery { api.getTopHeadlines(testSourceId, testApiKey) } returns response
        coEvery { dao.deleteArticlesBySource(any()) } returns Unit
        coEvery { dao.insertArticles(any()) } returns Unit
        coEvery { dao.deleteOldArticles(any()) } returns Unit

        // When & Then
        repository.getTopHeadlines(testSourceId).test {
            awaitItem() // Loading

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).hasSize(2)
            // First article should be the newer one
            assertThat(success.data?.first()?.title).isEqualTo("Newer Article")

            awaitComplete()
        }
    }
}
