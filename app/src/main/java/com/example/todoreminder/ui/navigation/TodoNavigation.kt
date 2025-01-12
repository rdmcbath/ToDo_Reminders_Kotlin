package com.example.todoreminder.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todoreminder.data.model.Item
import com.example.todoreminder.ui.screens.DetailScreen
import com.example.todoreminder.ui.screens.MainScreen
import com.google.gson.Gson

sealed class Screen(val route: String) {
    data object TodoList : Screen("todoList")
    data object TodoDetail : Screen("todoDetail/{todoJson}") {
        fun createRoute(todo: Item?): String {
            val gson = Gson()
            return if (todo != null) {
                val todoJson = Uri.encode(gson.toJson(todo))
                "todoDetail/$todoJson"
            } else {
                "todoDetail/new"
            }
        }
    }
}

@Composable
fun ToDoNavigation() {
    val navController = rememberNavController()
    val gson = Gson() // Used to serialize/deserialize Item objects

    NavHost(
        navController = navController,
        startDestination = Screen.TodoList.route
    ) {
        composable(Screen.TodoList.route) {
            MainScreen(
                onNavigateToDetail = { item ->
                    navController.navigate(Screen.TodoDetail.createRoute(item))
                }
            )
        }
        composable(
            route = Screen.TodoDetail.route,
            arguments = listOf(
                navArgument("todoJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val todoJson = backStackEntry.arguments?.getString("todoJson") ?: return@composable

            val todo = if (todoJson == "new") {
                null
            } else {
                try {
                    gson.fromJson(Uri.decode(todoJson), Item::class.java)
                } catch (e: Exception) {
                    Log.e("Navigation", "Error parsing todo", e)
                    return@composable
                }
            }

            DetailScreen(
                todo = todo,  // Will be null for new items
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}