package com.example.todoreminder.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.todoreminder.R
import androidx.core.app.NotificationCompat

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != null && intent.action == "com.example.todoreminder.ALARM_TRIGGER") {
            Log.d("NotificationReceiver", "Received notification broadcast, intent.action = ${intent.action}")
            val todoId = intent.getLongExtra("todoId", -1)
            val todoTitle = intent.getStringExtra("todoTitle")
            val todoDesc = intent.getStringExtra("todoDesc")

            val notificationBuilder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setContentTitle(todoTitle)
                .setContentText(todoDesc)
                .setSmallIcon(R.drawable.ic_notification_foreground)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.notify(todoId.toInt(), notificationBuilder.build())
        }
    }
}