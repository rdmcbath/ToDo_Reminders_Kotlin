package com.example.todoreminder.ui.screens.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todoreminder.data.model.Item
import com.example.todoreminder.data.repository.ToDoRepository
import com.example.todoreminder.utils.HelperUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainViewModel(
    private val todoRepository: ToDoRepository,
    application: Application
) : AndroidViewModel(application) {

    // Single source of truth from Room database
    val todos: StateFlow<List<Item>> = todoRepository.getAllTodos()
        .onEach { list ->
            Log.d("MainViewModel", "Received ${list.size} todos from database")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateToDo(updatedItem: Item) {
        viewModelScope.launch {
            try {
                todoRepository.updateItem(updatedItem)
                Log.d("MainViewModel", "Todo updated successfully: ${updatedItem.id}")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error updating todo: ${e.message}")
            }
        }
    }

    fun deleteLastToDo(item: Item) {
        viewModelScope.launch {
            try {
                val id = item.id
                id.let { todoRepository.deleteItemById(it) }
                Log.d("MainViewModel", "Todo deleted successfully: $id}")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error deleted todo: ${e.message}")
            }
        }
    }

    fun clearAllReminders() {
        viewModelScope.launch {
            todos.value.let { currentTodos ->
                todoRepository.clearAllReminders(currentTodos)
                Log.d("MainViewModel", "Successfully cleared all reminders")
            }
        }
    }

    fun checkExpiredReminders(items: List<Item>): Int {
        val now = LocalDateTime.now()

        val expiredItems = items.filter { item ->
            item.isReminderSet &&
                    item.dueDate?.let { HelperUtils.toLocalDateTime(it).isBefore(now) } == true
        }

        viewModelScope.launch {
            todoRepository.checkAndUpdateExpiredReminders()
            Log.d("MainViewModel", "Checked for expired reminders")
        }

        Log.d("MainViewModel", "Checked for expired reminders and updated the Todos accordingly: ${expiredItems.size}")
        return expiredItems.size

    }
}

class MainViewModelFactory(
    private val todoRepository: ToDoRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(todoRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}