package com.example.data.model

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val maxProgress: Int
)
