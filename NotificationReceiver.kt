package com.example.task_tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_TITLE = "notification_title"
        const val DUE_DATE = "due_date"
        const val ACTION_TASK_COMPLETED = "com.example.task_tracker.ACTION_TASK_COMPLETED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(NOTIFICATION_TITLE)
        val dueDateMillis = intent.getLongExtra(DUE_DATE, 0L)

        // Check if title is not null, if it is, use a default title
        val notificationTitle = title ?: "Task Reminder"

        // Format the due date for display in the notification
        val formattedDueDate = getFormattedDate(dueDateMillis)

        // Build and display the notification
        val notificationBuilder = NotificationCompat.Builder(context, "task_notification_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationTitle) // Use the title here
            .setContentText("Due on $formattedDueDate")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)  // Automatically dismiss the notification when tapped

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use a unique notification ID, e.g., based on the current time
        val notificationId = System.currentTimeMillis().toInt()

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getFormattedDate(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timeInMillis))
    }
}
