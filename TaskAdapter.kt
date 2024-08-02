package com.example.task_tracker

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(private val context: Context, val tasks: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val dueDateTextView: TextView = itemView.findViewById(R.id.dueDateTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    // New method to mark a task as done
    private fun markTaskAsDone(title: String?) {
        val position = tasks.indexOfFirst { it.title == title }
        if (position != -1) {
            tasks[position].isCompleted = true
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.tasktracker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]

        holder.titleTextView.text = task.title
        holder.descriptionTextView.text = task.description  // Set the description
        val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        holder.dueDateTextView.text = dateFormat.format(Date(task.dueDate))

        // Set check symbol based on completion status
        holder.checkBox.isChecked = task.isCompleted

        // Color coding based on task status and due date
        val colorResId = when {
            task.isCompleted -> R.color.colorgreen
            task.dueDate < System.currentTimeMillis() -> R.color.colorRed
            task.dueDate - System.currentTimeMillis() > 86400000 * 2 -> R.color.colorgreen
            else -> R.color.colorYellow
        }

        // Get the color from the resource ID using ContextCompat
        val color = ContextCompat.getColor(context, colorResId)

        holder.itemView.setBackgroundColor(color)

        // Handle checkbox click event
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Update the completion status of the task
            task.isCompleted = isChecked

            // Post a runnable to update the item after the layout is computed
            holder.itemView.post {
                // Notify the adapter that only this item has changed
                notifyItemChanged(position)
            }
        }

        // Example usage of markTaskAsDone
        holder.itemView.setOnClickListener {
            markTaskAsDone(task.title)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    // Custom method to add a new task
    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    // Custom method to remove a task
    fun removeTask(position: Int) {
        if (position in 0 until tasks.size) {
            tasks.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}


