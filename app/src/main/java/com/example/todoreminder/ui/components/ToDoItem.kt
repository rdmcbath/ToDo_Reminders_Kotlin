package com.example.todoreminder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todoreminder.data.model.Item
import com.example.todoreminder.utils.HelperUtils

@Composable
fun ToDoItem(
    todo: Item,
    onItemClick: (Item) -> Unit,
    onCheckboxClick: (Item) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onItemClick(todo) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {

            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { isChecked ->
                    onCheckboxClick(todo.copy(isCompleted = isChecked))
                },
                modifier = Modifier
                    .padding(end = 24.dp)
                    .align(Alignment.CenterVertically)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (todo.isReminderSet) {
                    Text(
                        text = "Reminder set for ${HelperUtils.formatDateTime(todo.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else (
                        Text(
                            text = "No Reminder",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        )
            }

            IconButton(onClick = { onItemClick(todo) }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Set reminder"
                )
            }
        }
    }
}