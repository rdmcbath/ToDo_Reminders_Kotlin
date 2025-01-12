package com.example.todoreminder

import android.app.Application
import com.example.todoreminder.data.database.ToDoDatabase
import com.example.todoreminder.data.repository.RepositoryProvider
import com.example.todoreminder.data.repository.ToDoRepository
import com.example.todoreminder.utils.ExpiredReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoApplication : Application() {
    // Initialize the database
    val repository: ToDoRepository
        get() {
            val database = ToDoDatabase.getDatabase(this)
            return ToDoRepository(database.todoDao())
        }

    override fun onCreate() {
        super.onCreate()
        // Insert sample data if needed
        CoroutineScope(Dispatchers.IO).launch {
            RepositoryProvider.getRepository(applicationContext)
                .insertSampleDataIfEmpty()
        }

        // Schedule the worker to check expired reminders
        ExpiredReminderWorker.schedule(this)
    }
}