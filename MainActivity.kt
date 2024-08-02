package com.example.task_tracker

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sharedPreferences: SharedPreferences

    // SharedPreferences keys
    private val tasksKey = "tasks"
    private val nightModeKey = "nightMode"

    private val channelId = "task_notification_channel"
    private val alarmChannelId = "alarm_notification_channel"


    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add fade-in animation to main activity
        findViewById<LinearLayout>(R.id.mainActivity).startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.fade_in
            )
        )

        // Show greeting message after a delay
        findViewById<TextView>(R.id.greetingMessage).postDelayed({
            findViewById<TextView>(R.id.greetingMessage).visibility = View.VISIBLE
        }, 2000) // 2000 ms = 2 seconds

        // Initialize sharedPreferences
        sharedPreferences = getSharedPreferences("TaskTrackerPrefs", MODE_PRIVATE)

        // Apply dark mode theme
        if (isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        // Add this code for animation
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        findViewById<View>(android.R.id.content).startAnimation(fadeIn)

        createNotificationChannel()
        createAlarmNotificationChannel()

        sharedPreferences = getSharedPreferences("TaskTrackerPrefs", MODE_PRIVATE)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setupRecyclerView()

        val btnAddTask: Button = findViewById(R.id.btnAddTask)
        btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // Load tasks from SharedPreferences
        val tasks = loadTasks()
        if (tasks.isEmpty()) {
            // If no tasks are stored in SharedPreferences, generate sample tasks
            val sampleTasks = generateSampleTasks()
            taskAdapter = TaskAdapter(this, sampleTasks)
            recyclerView.adapter = taskAdapter
            // Save sample tasks to SharedPreferences
            saveTasks(sampleTasks)
        } else {
            // If tasks are already stored in SharedPreferences, use them
            taskAdapter = TaskAdapter(this, tasks)
            recyclerView.adapter = taskAdapter
        }

        // Initialize bottom navigation view
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle Home navigation
                    true
                }
                R.id.navigation_dashboard -> {
                    // Handle Dashboard navigation
                    true
                }
                R.id.navigation_notifications -> {
                    // Handle Notifications navigation
                    true
                }
                else -> false
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuSettings -> {
                showSettingsDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Method to load tasks from SharedPreferences
    private fun loadTasks(): MutableList<Task> {
        val tasksJson = sharedPreferences.getString(tasksKey, null)
        val type: Type = object : TypeToken<MutableList<Task>>() {}.type
        return Gson().fromJson(tasksJson, type) ?: mutableListOf()
    }

    // Method to save tasks to SharedPreferences
    private fun saveTasks(tasks: MutableList<Task>) {
        val editor = sharedPreferences.edit()
        val tasksJson = Gson().toJson(tasks)
        editor.putString(tasksKey, tasksJson)
        editor.apply()
    }

    private fun getDateTimeMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notification Channel"
            val descriptionText = "Channel for task notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createAlarmNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Notification Channel"
            val descriptionText = "Channel for task alarms"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(alarmChannelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("LaunchActivityFromNotification", "MissingPermission")
    private fun scheduleNotification(task: Task) {
        val formattedDate = getFormattedDate(task.dueDate)

        val notificationId = task.id

        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        alarmIntent.putExtra(NotificationReceiver.NOTIFICATION_TITLE, task.title)
        alarmIntent.putExtra("formattedDueDate", formattedDate)
        alarmIntent.action = NotificationReceiver.ACTION_TASK_COMPLETED

        val pendingIntent = PendingIntent.getBroadcast(
            this, notificationId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Handle the case where exact alarms cannot be scheduled
                    // You may choose to use setAlarmClock or setAndAllowWhileIdle instead
                    // based on your application requirements.
                    return
                }
            } catch (e: SecurityException) {
                // Handle SecurityException
                // This exception may occur if the app doesn't have the necessary permissions
                e.printStackTrace()
                return
            }
        }

        // Calculate alarm time based on alarm type
        val alarmTime = when (task.alarmType) {
            AlarmType.NONE -> task.dueDate
            AlarmType.NOTIFICATION -> task.dueDate - 60000 // Schedule notification 1 minute before due
        }

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
    }

    private fun getFormattedDate(dueDate: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date(dueDate))
    }

    private fun setupRecyclerView() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                // Add visual feedback when swiping
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val removedTask = taskAdapter.tasks[position]
                taskAdapter.removeTask(position)

                // Save updated tasks to SharedPreferences after removing a task
                saveTasks(taskAdapter.tasks)

                // Add custom aesthetic animation when undoing the task
                val animator =
                    ObjectAnimator.ofFloat(viewHolder.itemView, "translationX", -200f, 0f)
                animator.apply {
                    duration = 500
                    start()
                }

                Snackbar.make(recyclerView, "Task removed", Snackbar.LENGTH_LONG)
                    .setAction("Undo Task") {
                        taskAdapter.addTask(removedTask)
                        // Save tasks again if task is undone
                        saveTasks(taskAdapter.tasks)
                    }
                    .show()
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun generateSampleTasks(): MutableList<Task> {
        return listOf(
            Task(1, "Due today", System.currentTimeMillis() - 86400000, false, AlarmType.NONE),
            Task(
                2,
                "Warning",
                System.currentTimeMillis() + 86400000,
                false,
                AlarmType.NOTIFICATION
            ),
            Task(3, "Free time", System.currentTimeMillis(), true, AlarmType.NONE)
        ).toMutableList()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun () {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Task")
            .setPositiveButton("Add") { _, _ ->
                val title = dialogView.findViewById<EditText>(R.id.editTitle).text.toString()
                val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
                val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
                val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupAlarm)

                val alarmType = when (radioGroup.checkedRadioButtonId) {
                    R.id.radioButtonNone -> AlarmType.NONE
                    R.id.radioButtonNotification -> AlarmType.NOTIFICATION
                    else -> AlarmType.NONE
                }

                val dueDateTime = if (timePicker != null) {
                    getDateTimeMillis(
                        datePicker.year, datePicker.month, datePicker.dayOfMonth,
                        timePicker.hour, timePicker.minute
                    )
                } else {
                    getDateTimeMillis(
                        datePicker.year, datePicker.month, datePicker.dayOfMonth,
                        0, 0
                    )
                }

                val newTask = Task(taskAdapter.itemCount + 1, title, dueDateTime, false, alarmType)
                taskAdapter.addTask(newTask)

                // Save tasks to SharedPreferences
                saveTasks(taskAdapter.tasks)

                // Schedule notification for the new task
                scheduleNotification(newTask)

                // Add animation
                val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                recyclerView.startAnimation(fadeIn)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(nightModeKey, false)
    }

    private fun setNightModeEnabled(isNightModeEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(nightModeKey, isNightModeEnabled).apply()

        // Set the night mode based on the new setting
        val nightMode =
            if (isNightModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(nightMode)

        // Recreate the activity to apply the new night mode setting
        recreate()
    }

    override fun recreate() {
        window.setWindowAnimations(R.style.ActivityAnimation)
        super.recreate()
    }


    @SuppressLint("MissingInflatedId", "UseSwitchCompatOrMaterialCode")
    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val switchNightMode = dialogView.findViewById<Switch>(R.id.switchNightMode)
        switchNightMode.isChecked = isDarkModeEnabled()

        // Find the buttons for Help Us, Contact, About Us, and Feedback
        val btnHelpUs = dialogView.findViewById<Button>(R.id.btnHelpUs)
        val btnContact = dialogView.findViewById<Button>(R.id.btnContact)
        val btnAboutUs = dialogView.findViewById<Button>(R.id.btnAboutUs)
        val btnFeedback = dialogView.findViewById<Button>(R.id.btnFeedback)

        // Set click listeners for each button
        btnHelpUs.setOnClickListener {
            // Implement the functionality for Help Us
            // For example, show a dialog or open a webpage with help information
        }

        btnContact.setOnClickListener {
            // Implement the functionality for Contact
            // For example, open a contact form or email client
        }

        btnAboutUs.setOnClickListener {
            // Implement the functionality for About Us
            // For example, show a dialog with information about the app/company
        }

        btnFeedback.setOnClickListener {
            // Implement the functionality for Feedback
            // For example, open a feedback form or email client
        }

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Settings")
            .setPositiveButton("Save") { _, _ ->
                setNightModeEnabled(switchNightMode.isChecked)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}

