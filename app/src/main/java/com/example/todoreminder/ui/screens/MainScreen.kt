package com.example.todoreminder.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoreminder.R
import com.example.todoreminder.data.model.Item
import com.example.todoreminder.data.repository.RepositoryProvider
import com.example.todoreminder.ui.components.ToDoItem
import com.example.todoreminder.ui.screens.viewmodel.MainViewModel
import com.example.todoreminder.ui.screens.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (Item?) -> Unit,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            todoRepository = RepositoryProvider.getRepository(LocalContext.current),
            application = LocalContext.current.applicationContext as Application
        )
    )
) {
    val todos by viewModel.todos.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)  // Apply the padding here
        ) {
            Text(
                text = stringResource(id = R.string.main_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                text = stringResource(id = R.string.textView_no_reminder),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(todos) { todo ->
                        ToDoItem(
                            todo = todo,
                            onItemClick = { onNavigateToDetail(it) },
                            onCheckboxClick = { updatedItem ->
                                viewModel.updateToDo(updatedItem)
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Column {
                        Button(
                            onClick = {
                                viewModel.deleteLastToDo(todos.first())
                                scope.launch {
                                    snackbarHostState.showSnackbar("Reminder deleted!")
                                }
                            },
                            modifier = Modifier.padding( horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(text = stringResource(id = R.string.button_text_delete_last_reminder))
                        }

                        Button(
                            onClick = {
                                viewModel.clearAllReminders()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Reminders Cleared!")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(text = stringResource(id = R.string.button_text_clear_all_reminders))
                        }

                        Button(
                            onClick = {
                                val number = viewModel.checkExpiredReminders(todos)
                                viewModel.checkExpiredReminders(todos)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Found $number expired Reminders and updated Todos!")
                                }
                            },
                            modifier = Modifier.padding( horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(text = stringResource(id = R.string.button_text_check_expired_reminders))
                        }

                    }
                }


                FloatingActionButton(
                    onClick = { onNavigateToDetail(null) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.content_desc_add_button)
                    )
                }
            }
        }
    }
}