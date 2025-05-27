package com.cookandroid.moodtracker

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var btnPrevMonth: Button
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnNextMonth: Button
    private lateinit var gridCalendar: GridLayout
    private lateinit var btnLogMood: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        gridCalendar = findViewById(R.id.gridCalendar)
        btnLogMood = findViewById(R.id.btnLogMood)

        // 현재 날짜로 tvCurrentMonth 초기화
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())
        tvCurrentMonth.text = sdf.format(calendar.time)

        // TODO: 월 이동 및 기분 기록 로직 구현
    }
}