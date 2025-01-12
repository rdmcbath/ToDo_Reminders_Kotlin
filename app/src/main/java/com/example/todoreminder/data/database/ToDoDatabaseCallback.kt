package com.example.todoreminder.data.database

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todoreminder.data.model.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ToDoDatabaseCallback(
    private val scope: CoroutineScope,
    private val context: Context
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("TodoDatabaseCallback", "onCreate triggered")
        scope.launch {
            try {
                val dao = ToDoDatabase.getDatabase(context).todoDao()
                val sampleItems = Item.SampleData.getTodoItems()
                Log.d("TodoDatabaseCallback", "Inserting ${sampleItems.size} sample items")
                sampleItems.forEach { item ->
                    dao.insertTodo(item)
                }
                Log.d("TodoDatabaseCallback", "Sample data inserted successfully")
            } catch (e: Exception) {
                Log.e("TodoDatabaseCallback", "Error inserting sample data", e)
            }
        }
    }

    // Add this to see if the database already exists
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        Log.d("TodoDatabaseCallback", "onOpen triggered")
    }
}