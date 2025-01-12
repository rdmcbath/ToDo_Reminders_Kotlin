package com.example.todoreminder.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.todoreminder.data.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {
    @Query("SELECT * FROM items")
    fun getAllTodos(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :todoId")
    suspend fun getTodoById(todoId: Long): Item?

    @Update
    suspend fun updateTodo(item: Item)

    @Insert
    suspend fun insertTodo(item: Item)

    @Query("DELETE FROM items WHERE id = :todoId")
    suspend fun deleteTodoById(todoId: Long)
}