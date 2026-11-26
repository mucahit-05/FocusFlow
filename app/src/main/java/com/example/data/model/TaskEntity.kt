package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SubtaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

object SubtaskConverter {
    fun fromJson(json: String): List<SubtaskItem> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<SubtaskItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SubtaskItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun toJson(list: List<SubtaskItem>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("isCompleted", item.isCompleted)
            }
            array.put(obj)
        }
        return array.toString()
    }
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "", // Purely user-defined custom category, e.g. "Matematik", "Fizik", "Yazılım"
    val recurrence: String = "NONE", // "NONE", "DAILY", "WEEKLY", "MONTHLY"
    val targetType: String = "SESSIONS", // "SESSIONS" (seans) or "DURATION_HOURS" (saat)
    val targetValue: Int = 5, // e.g. 5 sessions or 5 hours per period
    val scheduledTime: String? = null, // e.g. "09:00", "15:30"
    val subtasksJson: String = "[]",
    val estimatedPomodoros: Int = 2,
    val completedPomodoros: Int = 0,
    val isCompleted: Boolean = false,
    val priority: String = "NORMAL", // "LOW", "NORMAL", "HIGH"
    val tags: String = "", // e.g. "Acil, Kodlama, Ders"
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getSubtasks(): List<SubtaskItem> = SubtaskConverter.fromJson(subtasksJson)
    fun getTagsList(): List<String> = tags.split(",").map { it.trim().removePrefix("#") }.filter { it.isNotEmpty() }
}
