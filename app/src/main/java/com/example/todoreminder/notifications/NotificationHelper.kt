package com.example.todoreminder.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.todoreminder.data.model.Item
import com.example.todoreminder.utils.HelperUtils
import java.time.ZoneId

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "com.example.todoreminder.notifications"
        private const val CHANNEL_NAME = "Todo Reminders"
        private const val DESCRIPTION = "Channel for todo reminders"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = DESCRIPTION
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleNotification(todo: Item) {
        val intent: Intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.todoreminder.ALARM_TRIGGER"
            putExtra("todoId", todo.id)
            putExtra("todoTitle", todo.title)
            putExtra("todoDesc", todo.description)
        }

        val pendingIntent: PendingIntent? = todo.id.let {
            PendingIntent.getBroadcast(
                context,
                it.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        Log.d("NotificationHelper", "Scheduled notification for ${todo.title} at ${todo.dueDate}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Use setAlarmClock for more reliable delivery
        todo.dueDate?.let { HelperUtils.toLocalDateTime(it).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }
            ?.let {
                AlarmManager.AlarmClockInfo(
                    it,
                    pendingIntent
                )
            }?.let {
                if (pendingIntent != null) {
                    alarmManager.setAlarmClock(
                        it,
                        pendingIntent
                    )
                }
            }
    }
}