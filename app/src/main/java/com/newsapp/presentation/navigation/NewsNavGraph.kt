package com.newsapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.newsapp.domain.model.Article
import com.newsapp.presentation.detail.ArticleDetailScreen
import com.newsapp.presentation.headlines.HeadlinesScreen
import com.newsapp.presentation.headlines.HeadlinesViewModel

@Composable
fun NewsNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Headlines.route
) {

    val sharedViewModel: HeadlinesViewModel = hiltViewModel()


    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Headlines.route) {
            HeadlinesScreen(
                onArticleClick = { article ->
                    // Store selected article and navigate using index
                    val state = sharedViewModel.state.value
                    val articleIndex = state.articles.indexOf(article)
                    if (articleIndex != -1) {
                        sharedViewModel.selectArticle(article)
                        navController.navigate(Screen.ArticleDetail.createRoute(articleIndex))
                    }
                },
                viewModel = sharedViewModel
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument("articleIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val articleIndex = backStackEntry.arguments?.getInt("articleIndex") ?: -1
            val state by sharedViewModel.state.collectAsStateWithLifecycle()

            // Get article by index from the state
            val selectedArticle = state.articles.getOrNull(articleIndex)

            selectedArticle?.let { article ->
                ArticleDetailScreen(
                    article = article,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
