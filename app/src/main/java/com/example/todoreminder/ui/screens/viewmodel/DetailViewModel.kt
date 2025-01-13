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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale

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
    val selectedDueDate = _selectedDueDate.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _isCreatingNew = MutableStateFlow(todo == null)
    val isCreatingNew = _isCreatingNew.asStateFlow()

    val formattedDueDate = selectedDueDate.map { timestamp ->
        HelperUtils.formatDateTime(timestamp)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "No date set"
    )

    init {
        todo?.let {
            _title.value = it.title
            _description.value = it.description
            _selectedDueDate.value = it.dueDate
            Log.d("DetailViewModel", "Setting todo: ${it.title}, ${it.description}, ${it.dueDate}")
        }

        // Initialize selectedTime if todo has a reminder
        viewModelScope.launch {
            val item = todoRepository.getTodoById(todoId)
            Log.d("DetailViewModel", "Fetched todo from database: $item")
            if (item?.isReminderSet == true) {
                item.dueDate?.let { timestamp ->
                    Log.d("DetailViewModel", "Setting initial selectedTime from dueDate: $timestamp")
                    val localDateTime = HelperUtils.toLocalDateTime(timestamp)
                    _selectedTime.value = localDateTime.toLocalTime()
                }
            } else {
                _selectedTime.value = null
            }
        }
    }

    fun updateTitle (newTitle: String) {
        _title.value = newTitle
    }

    fun updateDescription (newDescription: String) {
        _description.value = newDescription
    }

    fun onDueDateSelected(milliseconds: Long) {
        _selectedDueDate.value = milliseconds
    }

    fun createTodo() {
        if (!isValid()) {
            _uiState.value = DetailUiState.Error("Title cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                val newTodo = Item(
                    id = 0,
                    title = _title.value,
                    description = _description.value,
                    isCompleted = false,
                    isReminderSet = false,
                    dueDate = _selectedDueDate.value
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

    fun updateTodo() {
        if (!isValid()) {
            _uiState.value = DetailUiState.Error("Title cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                val updatedTodo = Item(
                    id = todoId,
                    title = _title.value,
                    description = _description.value,
                    isCompleted = false,
                    isReminderSet = false,
                    dueDate = _selectedDueDate.value
                )
                Log.d("DetailViewModel", "Updating todo with dueDate: ${_selectedDueDate.value}")
                todoRepository.updateItem(updatedTodo)
                _uiState.value = DetailUiState.Success
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error updating todo", e)
                _uiState.value = DetailUiState.Error("Failed to update todo")
            }
        }
    }

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

                // Create LocalDateTime and convert to timestamp
                val reminderDateTime = LocalDate.now().atTime(selectedTime)
                val timestamp = HelperUtils.fromLocalDateTime(reminderDateTime)

                Log.d("DetailViewModel", "Setting reminder for: $reminderDateTime")

                viewModelScope.launch {
                    val updatedToDo = item.copy(
                        isReminderSet = true,
                        dueDate = timestamp  // Now using Long timestamp
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