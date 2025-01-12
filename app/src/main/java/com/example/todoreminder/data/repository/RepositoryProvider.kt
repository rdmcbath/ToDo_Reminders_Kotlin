package com.example.todoreminder.data.repository

import android.content.Context
import com.example.todoreminder.data.database.ToDoDatabase

object RepositoryProvider {
    private var repository: ToDoRepository? = null

    fun getRepository(context: Context): ToDoRepository {
        return repository ?: synchronized(this) {
            val database = ToDoDatabase.getDatabase(context)
            ToDoRepository(database.todoDao()).also { repository = it }
        }
    }
}