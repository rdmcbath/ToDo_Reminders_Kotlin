package com.example.todoreminder.ui.screens.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todoreminder.State.DetailUiState
import com.example.todoreminder.data.model.Item
import com.example.todoreminder.data.repository.ToDoRepository
import com.example.todoreminder.notifications.NotificationHelper
import com.example.todoreminder.utils.HelperUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class DetailViewModel(
    private val todoId: Long,
    todo: Item?,
    private val todoRepository: ToDoRepository,
    private val notificationHelper: NotificationHelper,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _selectedTime = MutableStateFlow<LocalTime?>(null)
    val selectedTime = _selectedTime.asStateFlow()

    private val _selectedDueDate = MutableStateFlow<Long?>(null)
    val selectedDueDate: MutableStateFlow<Long?> = _selectedDueDate

    private val _databaseTodo = MutableStateFlow<Item?>(null)
    val databaseTodo = _databaseTodo.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _isCreatingNew = MutableStateFlow(todo == null)
    val isCreatingNew = _isCreatingNew.asStateFlow()


    init {
        Log.d("DetailViewModel", "Initializing with todo: $todoId")
        todo?.let {
            _title.value = it.title
            _description.value = it.description
        }

        // Initialize selectedTime if todo has a reminder
        viewModelScope.launch {
            val item = todoRepository.getTodoById(todoId)
            Log.d("DetailViewModel", "Fetched todo from database: $item")
            if (item?.isReminderSet == true) {  // Only set time if reminder is actually set
                item.dueDate?.let { dueDate ->
                    Log.d("DetailViewModel", "Setting initial selectedTime from dueDate: $dueDate")
                    val localDateTime = HelperUtils.toLocalDateTime(dueDate)
                    _selectedTime.value = localDateTime.toLocalTime()
                }
            } else {
                _selectedTime.value = null  // Set selectedTime to null if no reminder is set
            }
        }
    }

    fun updateTitle (newTitle: String) {
        _title.value = newTitle
    }

    fun updateDescription (newDescription: String) {
        _description.value = newDescription
    }

    fun updateDueDate(date: Long) {
        _selectedDueDate.value = date
    }

    // Function to save new todo
    fun createTodo() {
        if (!isValid()) {
            _uiState.value = DetailUiState.Error("Title cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                val newTodo = Item(
                    id = 0, // Room will generate ID
                    title = _title.value,
                    description = _description.value,
                    isCompleted = false,
                    isReminderSet = false,
                    dueDate = null
                )
                Log.d("DetailViewModel", "Saving new todo: $newTodo")
                todoRepository.createItem(newTodo)
                _uiState.value = DetailUiState.Success
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error saving todo", e)
                _uiState.value = DetailUiState.Error("Failed to save todo")
            }
        }
    }

    // Validate before saving
    private fun isValid(): Boolean {
        return _title.value.isNotBlank()
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        Log.d("DetailViewModel", "onTimeSelected called with $hour:$minute")
        _selectedTime.value = LocalTime.of(hour, minute)
        Log.d("DetailViewModel", "selectedTime updated to: ${_selectedTime.value}")
    }

    fun setReminder(item: Item) {
        val selectedTime = _selectedTime.value
        Log.d("DetailViewModel", "setReminder called. Selected time: $selectedTime")

        if (selectedTime == null) {
            Log.d("DetailViewModel", "No time selected, returning")
            return
        }

        try {
            Log.d("DetailViewModel", "Starting setReminder for todo: ${item.title}")
            if (hasNotificationPermission()) {
                Log.d("DetailViewModel", "Has notification permission")

                val reminderDateTime = LocalDate.now().atTime(selectedTime)
                val newDateTime = HelperUtils.fromLocalDateTime(reminderDateTime)

                Log.d("DetailViewModel", "Setting reminder for: $reminderDateTime")

                viewModelScope.launch {
                    val updatedToDo = item.copy(
                        isReminderSet = true,
                        dueDate = newDateTime
                    )
                    todoRepository.updateItem(updatedToDo)
                    notificationHelper.scheduleNotification(updatedToDo)
                    Log.d("DetailViewModel", "Reminder set successfully")
                }
                _uiState.value = DetailUiState.Success
            } else {
                Log.d("DetailViewModel", "No notification permission")
                _uiState.value = DetailUiState.RequiresPermission
            }
        } catch (e: Exception) {
            Log.e("DetailViewModel", "Error in setReminder", e)
            _uiState.value = DetailUiState.Error(e.message ?: "Failed to set reminder")
        }
    }

    private fun hasNotificationPermission(): Boolean {
        // Check if the app has notification permission
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            permission == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

class DetailViewModelFactory(
    private val todoId: Long,
    private val todo: Item?,
    private val todoRepository: ToDoRepository,
    private val notificationHelper: NotificationHelper,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(todoId, todo, todoRepository, notificationHelper, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}