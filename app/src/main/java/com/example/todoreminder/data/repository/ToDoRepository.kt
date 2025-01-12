package com.example.todoreminder.data.repository

import android.util.Log
import com.example.todoreminder.data.database.ToDoDao
import com.example.todoreminder.data.model.Item
import com.example.todoreminder.utils.HelperUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class ToDoRepository(private val dao: ToDoDao) {

    fun getAllTodos(): Flow<List<Item>> {
        Log.d("TodoRepository", "Getting todos flow")
        return dao.getAllTodos()
    }

    suspend fun getTodoById(todoId: Long): Item? {
        return dao.getTodoById(todoId)
    }

    suspend fun insertSampleDataIfEmpty() {
        val currentItems = dao.getAllTodos().first()
        if (currentItems.isEmpty()) {
            Log.d("TodoRepository", "Database empty, inserting sample data")
            Item.SampleData.getTodoItems().forEach { item ->
                dao.insertTodo(item)
            }
        }
    }

    suspend fun updateItem(item: Item) {
        dao.updateTodo(item)
    }

    suspend fun createItem(item: Item) {
        return dao.insertTodo(item)
    }

    suspend fun deleteItemById(itemId: Long) {
        dao.deleteTodoById(itemId)
    }

    suspend fun clearAllReminders(list: List<Item>) {
        for (item in list) {
            if (item.isReminderSet) {
                dao.updateTodo(item.copy(isReminderSet = false))
            }
        }
    }

    suspend fun checkAndUpdateExpiredReminders() {
        val now = LocalDateTime.now()

        val expiredTodos = dao.getAllTodos().first().filter { todo ->
            todo.isReminderSet &&
                    todo.dueDate?.let { HelperUtils.toLocalDateTime(it).isBefore(now) } == true
        }

        expiredTodos.forEach { todo ->
            val updatedTodo = todo.copy(isReminderSet = false)
            dao.updateTodo(updatedTodo)
        }
    }
}