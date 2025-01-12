package com.example.todoreminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val isReminderSet: Boolean,
    val dueDate: String? = null
) {

    object SampleData {
        fun getTodoItems(): MutableList<Item> {
            return mutableListOf(
                Item(
                    id = 1,
                    title = "Complete project presentation",
                    description = "Prepare slides for the quarterly review",
                    dueDate = null,
                    isCompleted = false,
                    isReminderSet = false
                ),
                Item(
                    id = 2,
                    title = "Buy groceries",
                    description = "Milk, eggs, bread, and vegetables",
                    dueDate = null,
                    isCompleted = false,
                    isReminderSet = false
                ),
                Item(
                    id = 3,
                    title = "Schedule dentist appointment",
                    description = "Call Dr. Smith's office for cleaning",
                    dueDate = null,
                    isCompleted = false,
                    isReminderSet = false
                ),
                Item(
                    id = 4,
                    title = "Pay utility bills",
                    description = "Electric and water bills due end of month",
                    dueDate = null,
                    isCompleted = false,
                    isReminderSet = false
                )
            )
        }
    }
}