package com.newsapp.presentation.headlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.BuildConfig
import com.newsapp.core.util.Resource
import com.newsapp.domain.model.Article
import com.newsapp.domain.usecase.GetTopHeadlinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class HeadlinesState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sourceName: String = BuildConfig.NEWS_SOURCE_NAME,
    val selectedArticle: Article? = null
)

@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HeadlinesState())
    val state: StateFlow<HeadlinesState> = _state.asStateFlow()

    init {
        loadHeadlines()
    }

    fun loadHeadlines(forceRefresh: Boolean = false) {
        getTopHeadlinesUseCase(BuildConfig.NEWS_SOURCE, forceRefresh)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                articles = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                articles = result.data ?: it.articles,
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                articles = result.data ?: it.articles,
                                isLoading = true,
                                error = null
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectArticle(article: Article) {
        _state.update { it.copy(selectedArticle = article) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
