package com.newsapp.presentation.headlines

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.newsapp.core.util.Resource
import com.newsapp.domain.model.Article
import com.newsapp.domain.model.Source
import com.newsapp.domain.usecase.GetTopHeadlinesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HeadlinesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HeadlinesViewModel
    private lateinit var getTopHeadlinesUseCase: GetTopHeadlinesUseCase

    private val testArticles = listOf(
        Article(
            source = Source("bbc-news", "BBC News"),
            author = "John Doe",
            title = "Test Article 1",
            description = "Description 1",
            url = "https://test.com/1",
            urlToImage = "https://test.com/image1.jpg",
            publishedAt = ZonedDateTime.now(),
            content = "Content 1"
        ),
        Article(
            source = Source("bbc-news", "BBC News"),
            author = "Jane Doe",
            title = "Test Article 2",
            description = "Description 2",
            url = "https://test.com/2",
            urlToImage = "https://test.com/image2.jpg",
            publishedAt = ZonedDateTime.now().minusHours(1),
            content = "Content 2"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTopHeadlinesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct default values`() = runTest {
        // Given
        every { getTopHeadlinesUseCase(any(), any()) } returns flowOf(Resource.Loading())

        // When
        viewModel = HeadlinesViewModel(getTopHeadlinesUseCase)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.articles).isEmpty()
            assertThat(state.isLoading).isTrue()
            assertThat(state.error).isNull()
            assertThat(state.sourceName).isNotEmpty()
        }
    }

    @Test
    fun `loadHeadlines updates state with success`() = runTest {
        // Given
        every { getTopHeadlinesUseCase(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Success(testArticles)
        )

        // When
        viewModel = HeadlinesViewModel(getTopHeadlinesUseCase)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.articles).hasSize(2)
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isNull()
            assertThat(state.articles.first().title).isEqualTo("Test Article 1")
        }
    }

    @Test
    fun `loadHeadlines updates state with error`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { getTopHeadlinesUseCase(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Error(errorMessage)
        )

        // When
        viewModel = HeadlinesViewModel(getTopHeadlinesUseCase)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.articles).isEmpty()
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isEqualTo(errorMessage)
        }
    }

    @Test
    fun `loadHeadlines with forceRefresh calls use case with correct parameter`() = runTest {
        // Given
        every { getTopHeadlinesUseCase(any(), false) } returns flowOf(Resource.Loading())
        every { getTopHeadlinesUseCase(any(), true) } returns flowOf(
            Resource.Loading(),
            Resource.Success(testArticles)
        )

        viewModel = HeadlinesViewModel(getTopHeadlinesUseCase)
        advanceUntilIdle()

        // When
        viewModel.loadHeadlines(forceRefresh = true)
        advanceUntilIdle()

        // Then
        verify { getTopHeadlinesUseCase(any(), true) }
    }

    @Test
    fun `clearError resets error state`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { getTopHeadlinesUseCase(any(), any()) } returns flowOf(
            Resource.Error(errorMessage)
        )

        viewModel = HeadlinesViewModel(getTopHeadlinesUseCase)
        advanceUntilIdle()

        // When
        viewModel.clearError()
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `loadHeadlines preserves articles on error with cached data`() = runTest {
        // Given
        every { getTopHeadlinesUseCase(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Error("Network error", testArticles)
        )

        // When
        viewModel = HeadlinesViewModel(getTopHeadlinesUseCase)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.articles).hasSize(2)
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isEqualTo("Network error")
        }
    }

    @Test
    fun `multiple loadHeadlines calls handle state correctly`() = runTest {
        // Given
        every { getTopHeadlinesUseCase(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Success(testArticles)
        )

        viewModel = HeadlinesViewModel(getTopHeadlinesUseCase)
        advanceUntilIdle()

        // When - Load again
        viewModel.loadHeadlines()
        advanceUntilIdle()

        // Then
        verify(atLeast = 2) { getTopHeadlinesUseCase(any(), any()) }
        
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.articles).hasSize(2)
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun `selectArticle updates selected article in state`() = runTest {
        // Given
        every { getTopHeadlinesUseCase(any(), any()) } returns flowOf(
            Resource.Success(testArticles)
        )

        viewModel = HeadlinesViewModel(getTopHeadlinesUseCase)
        advanceUntilIdle()

        // When
        val articleToSelect = testArticles.first()
        viewModel.selectArticle(articleToSelect)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.selectedArticle).isNotNull()
            assertThat(state.selectedArticle?.url).isEqualTo(articleToSelect.url)
            assertThat(state.selectedArticle?.title).isEqualTo(articleToSelect.title)
        }
    }
}
