package com.cookandroid.moodtracker

data class CustomMood(
    val id: String,      // 고유 ID (예: UUID 또는 타임스탬프 기반)
    var name: String,    // 감정 이름 (수정 가능)
    var colorHex: String // 감정 색상 HEX 코드 (수정 가능)
) 