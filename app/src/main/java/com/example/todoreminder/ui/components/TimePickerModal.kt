package com.example.todoreminder.ui.components

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoTimePickerDialog(
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState()
    Log.d("TimePickerDialog", "TimePickerDialog initialized with state: hour=${state.hour}, minute=${state.minute}")

    TimePickerDialog(
        onDismiss = {
            Log.d("TimePickerDialog", "Dialog dismissed")
            onDismiss()
        },
        onTimeSelected = {
            Log.d("TimePickerDialog", "Time selected in dialog. Hour: ${state.hour}, Minute: ${state.minute}")
            onTimeSelected(state.hour, state.minute)
        }
    ) {
        TimePicker(state = state)
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = { onTimeSelected() }) {
                Text("OK")
            }
        },
        text = { content() }
    )
}
