package com.example.task_tracker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ALARM_TITLE = "alarm_title"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra(ALARM_TITLE)

        // Log the received title for debugging
        Log.d("AlarmReceiver", "Received title: $title")

        // Display a notification when the alarm is triggered
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Set the content title of the notification

        val contentTitle = title ?: "Task Alarm"
        // Build the notification
        val notification = NotificationCompat.Builder(context, "alarm_notification_channel")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Alarm: $contentTitle")
            .setContentText("It's time for your task")
            .setColor(ContextCompat.getColor(context, R.color.colorgreen))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // Notify
        notificationManager.notify(2, notification)
    }
}
