package com.cookandroid.moodtracker

sealed class MoodListItem {
    data class DefaultMoodItem(val name: String, val colorHex: String) : MoodListItem()
    data class CustomMoodItem(val mood: CustomMood) : MoodListItem()
} 