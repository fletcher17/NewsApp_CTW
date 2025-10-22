package com.newsapp.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.newsapp.core.util.Resource
import com.newsapp.domain.model.Article
import com.newsapp.domain.model.Source
import com.newsapp.domain.repository.NewsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.ZonedDateTime

class GetTopHeadlinesUseCaseTest {

    private lateinit var useCase: GetTopHeadlinesUseCase
    private lateinit var repository: NewsRepository

    private val testSource = "bbc-news"
    private val testArticles = listOf(
        Article(
            source = Source("bbc-news", "BBC News"),
            author = "John Doe",
            title = "Test Article",
            description = "Test Description",
            url = "https://test.com",
            urlToImage = "https://test.com/image.jpg",
            publishedAt = ZonedDateTime.now(),
            content = "Test content"
        )
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetTopHeadlinesUseCase(repository)
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        // Given
        every { repository.getTopHeadlines(testSource, false) } returns flowOf(
            Resource.Success(testArticles)
        )

        // When
        useCase(testSource, forceRefresh = false).test {
            val result = awaitItem()

            // Then
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat(result.data).hasSize(1)

            awaitComplete()
        }

        verify { repository.getTopHeadlines(testSource, false) }
    }

    @Test
    fun `invoke with forceRefresh true calls repository correctly`() = runTest {
        // Given
        every { repository.getTopHeadlines(testSource, true) } returns flowOf(
            Resource.Success(testArticles)
        )

        // When
        useCase(testSource, forceRefresh = true).test {
            val result = awaitItem()

            // Then
            assertThat(result).isInstanceOf(Resource.Success::class.java)

            awaitComplete()
        }

        verify { repository.getTopHeadlines(testSource, true) }
    }

    @Test
    fun `invoke propagates loading state from repository`() = runTest {
        // Given
        every { repository.getTopHeadlines(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Success(testArticles)
        )

        // When & Then
        useCase(testSource).test {
            val loading = awaitItem()
            assertThat(loading).isInstanceOf(Resource.Loading::class.java)

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)

            awaitComplete()
        }
    }

    @Test
    fun `invoke propagates error state from repository`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { repository.getTopHeadlines(any(), any()) } returns flowOf(
            Resource.Error(errorMessage)
        )

        // When & Then
        useCase(testSource).test {
            val error = awaitItem()
            assertThat(error).isInstanceOf(Resource.Error::class.java)
            assertThat(error.message).isEqualTo(errorMessage)

            awaitComplete()
        }
    }

    @Test
    fun `invoke returns flow that emits multiple states`() = runTest {
        // Given
        every { repository.getTopHeadlines(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Loading(testArticles),
            Resource.Success(testArticles)
        )

        // When & Then
        useCase(testSource).test {
            val loading1 = awaitItem()
            assertThat(loading1).isInstanceOf(Resource.Loading::class.java)
            assertThat(loading1.data).isNull()

            val loading2 = awaitItem()
            assertThat(loading2).isInstanceOf(Resource.Loading::class.java)
            assertThat(loading2.data).isNotNull()

            val success = awaitItem()
            assertThat(success).isInstanceOf(Resource.Success::class.java)
            assertThat(success.data).hasSize(1)

            awaitComplete()
        }
    }
}
