package com.example.todoreminder.State

import com.example.todoreminder.data.model.Item

sealed class DetailUiState {
    object Initial : DetailUiState()
    object Success : DetailUiState()
    object RequiresPermission : DetailUiState()
    data class EditingItem(val todo: Item, var new: String) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}