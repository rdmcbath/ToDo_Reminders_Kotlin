package com.example.todoreminder.ui.screens

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoreminder.State.DetailUiState
import com.example.todoreminder.data.model.Item
import com.example.todoreminder.data.repository.RepositoryProvider
import com.example.todoreminder.notifications.NotificationHelper
import com.example.todoreminder.ui.components.DatePickerModal
import com.example.todoreminder.ui.components.ToDoTimePickerDialog
import com.example.todoreminder.ui.screens.viewmodel.DetailViewModel
import com.example.todoreminder.ui.screens.viewmodel.DetailViewModelFactory
import com.example.todoreminder.utils.HelperUtils
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun DetailScreen(
    todo: Item?,  // null when creating new todo
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = viewModel(
        factory = DetailViewModelFactory(
            todoId = todo?.id ?: 0L, // Pass 0 for new items
            todo = todo,
            todoRepository = RepositoryProvider.getRepository(LocalContext.current),
            notificationHelper = NotificationHelper(LocalContext.current),
            application = LocalContext.current.applicationContext as Application
        )
    ),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp
    val uiState by viewModel.uiState.collectAsState()
    val formattedExistingTime = todo?.dueDate?.let { dueDate ->
        HelperUtils.toLocalDateTime(dueDate).format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    val snackbarHostState = remember { SnackbarHostState() }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedTime by viewModel.selectedTime.collectAsState()
    val formattedDueDate by viewModel.formattedDueDate.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val isCreatingNew by viewModel.isCreatingNew.collectAsState()

    // Permission for sending notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            todo?.let { viewModel.setReminder(it) }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permission needed to send reminder notification")
            }
        }
    }

    val checkAndRequestNotificationPermission: (onPermissionGranted: () -> Unit) -> Unit =
        { onPermissionGranted ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        onPermissionGranted()
                    }

                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Permission needed to send reminder notification")
                        }
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    else -> {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                onPermissionGranted()
            }
        }

    // Handle time selected
    fun handleTimeSelected(hour: Int, minute: Int) {
        Log.d("DetailScreen", "Time selected in DetailScreen: $hour:$minute")
        viewModel.onTimeSelected(hour, minute)

        todo?.let { currentTodo ->
            checkAndRequestNotificationPermission {
                viewModel.setReminder(currentTodo)
            }
        }
        showTimePicker = false
    }

    if (showTimePicker) {
        ToDoTimePickerDialog(
            onTimeSelected = { hour, minute ->
                handleTimeSelected(hour, minute)
            },
            onDismiss = {
                Log.d("DetailScreen", "TimePicker dismissed")
                showTimePicker = false
            }
        )
    }

    // Handle date selected
    fun handleDateSelected(date: Long?) {
        if (date != null) {
            viewModel.onDueDateSelected(date)
        }
        showDatePicker = false
    }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { date ->
                date?.let {
                    handleDateSelected(date)
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Handle UI states
    LaunchedEffect(uiState) {
        when (uiState) {
            is DetailUiState.Success -> {
                scope.launch {
                    val message = when (isCreatingNew) {
                        true -> "ToDo Added!"
                        false -> "ToDo updated!"
                        else -> "Reminder set!"
                }
                    snackbarHostState.showSnackbar(message)
                }
            }

            is DetailUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar((uiState as DetailUiState.Error).message)
                }
            }

            else -> {} // Handle other states if needed
        }
    }

    // check and request permission
    fun checkAndRequestNotificationPermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted, proceed
                    onPermissionGranted()
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    // Show explanation why we need this permission
                    scope.launch {
                        snackbarHostState.showSnackbar("Permission needed to send reminder notification")
                    }
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    // First time asking or "Don't ask again" selected
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // No runtime permission needed for Android < 13
            onPermissionGranted()
        }
    }

    // Wrap your content in a Scaffold to properly show the snack bars
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)  // Apply scaffold padding
                .padding(top = statusBarHeight)
        ) {
            // Top section with dark background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = if (isCreatingNew) "Create New Task" else "Task Details",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        viewModel.updateTitle(it)
                    },
                    label = { Text("NAME") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isCreatingNew || todo?.isReminderSet == false,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        viewModel.updateDescription(it)
                    },
                    label = { Text("DESCRIPTION") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isCreatingNew || todo?.isReminderSet == false,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Due Date field
                OutlinedTextField(
                    value = formattedDueDate,
                    onValueChange = {},
                    label = { Text("DUE DATE") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Open Date Picker")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    readOnly = true
                )

                // Button section
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            when {
                                selectedTime != null -> {
                                    selectedTime?.let { time ->
                                        Log.d("DetailScreen", "Showing selectedTime: $selectedTime")
                                        Text(
                                            text = "Reminder set for: ${
                                                time.format(
                                                    DateTimeFormatter.ofPattern("HH:mm")
                                                )
                                            }",
                                            modifier = Modifier.padding(
                                                vertical = 24.dp,
                                                horizontal = 24.dp
                                            ),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                else -> {
                                    Log.d(
                                        "DetailScreen",
                                        "No reminder set. isReminderSet: ${todo?.isReminderSet}, todo: $todo"
                                    )
                                    // Don't show any text if no reminder is set
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isCreatingNew) {
                                Button(
                                    onClick = { viewModel.createTodo() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .padding(bottom = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = "Save ToDo",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.updateTodo() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .padding(bottom = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = "Update ToDo",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedTime == null) {
                                    showTimePicker = true
                                    Log.d("DetailScreen", "Showing time picker dialog")
                                } else {
                                    todo?.let { safeTodo ->
                                        checkAndRequestNotificationPermission {
                                            viewModel.setReminder(safeTodo)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (todo?.isReminderSet != true) "Set Reminder" else "Reminder is already set",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }


}
