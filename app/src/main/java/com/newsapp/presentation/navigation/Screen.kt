package com.newsapp.presentation.navigation

sealed class Screen(val route: String) {
    data object Headlines : Screen("headlines")
    data object ArticleDetail : Screen("article_detail/{articleIndex}") {
        fun createRoute(articleIndex: Int) = "article_detail/${articleIndex}"
    }
}