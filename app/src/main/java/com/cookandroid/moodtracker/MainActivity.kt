package com.cookandroid.moodtracker

import android.content.Context // Context for file IO
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log // Log for debugging
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader // For reading file
import java.io.File // For file checking
import java.io.FileOutputStream // For writing file
import java.io.InputStreamReader // For reading file
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var btnPrevMonth: Button
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnNextMonth: Button
    private lateinit var gridCalendar: GridLayout
    private lateinit var btnLogMood: Button

    private lateinit var currentCalendar: Calendar

    // 기분 데이터 정의 (이름 to 색상 코드)
    private val moods = mapOf(
        "행복" to "#FFEB3B", // Yellow
        "좋음" to "#AED581", // Light Green
        "보통" to "#FFF59D", // Pale Yellow
        "나쁨" to "#FFAB91", // Light Orange
        "슬픔" to "#B0BEC5"  // Blue Grey
    )
    private val moodNames = moods.keys.toTypedArray()
    private val moodFileName = "mood_data.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        gridCalendar = findViewById(R.id.gridCalendar)
        btnLogMood = findViewById(R.id.btnLogMood)

        currentCalendar = Calendar.getInstance()

        updateCurrentMonthText()
        drawCalendar()

        // 이전 달 버튼 클릭 이벤트
        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCurrentMonthText()
            drawCalendar()
        }

        // 다음 달 버튼 클릭 이벤트
        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCurrentMonthText()
            drawCalendar()
        }

        // '기분 기록하기' 버튼 클릭 이벤트
        btnLogMood.setOnClickListener {
            showMoodLogDialog()
        }
    }

    private fun updateCurrentMonthText() {
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())
        tvCurrentMonth.text = sdf.format(currentCalendar.time)
    }

    private fun showMoodLogDialog() {
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val currentDay = dayFormat.format(currentCalendar.time) // 현재 선택된 '일'을 가져옴
        
        val titleDate = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault()).format(currentCalendar.time)

        val selectedMoodIndex = intArrayOf(0) 

        val builder = AlertDialog.Builder(this)
        builder.setTitle("${titleDate} 기분 선택") // 제목에 '일'까지 포함
        builder.setSingleChoiceItems(moodNames, selectedMoodIndex[0]) { _, which ->
            selectedMoodIndex[0] = which
        }
        builder.setPositiveButton("확인") { _, _ ->
            val selectedMoodName = moodNames[selectedMoodIndex[0]]
            val selectedMoodColor = moods[selectedMoodName]
            // 날짜 형식을 YYYY-MM-DD로 일관되게 사용
            val selectedDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentCalendar.time)

            if (selectedMoodColor != null) {
                saveMood(selectedDateKey, selectedMoodColor)
                Toast.makeText(this, "${selectedDateKey} : $selectedMoodName 기분이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                drawCalendar() 
            } else {
                Toast.makeText(this, "오류: 기분 색상을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("취소", null)
        builder.create().show()
    }

    private fun saveMood(date: String, colorHexCode: String) {
        val moodEntry = "$date:$colorHexCode"
        val file = File(filesDir, moodFileName)
        val newMoodData = mutableListOf<String>()
        var entryUpdated = false

        try {
            if (file.exists()) {
                openFileInput(moodFileName).bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        if (line.startsWith("$date:")) {
                            newMoodData.add(moodEntry) // 기존 날짜면 새로운 기분으로 교체
                            entryUpdated = true
                        } else {
                            newMoodData.add(line) // 다른 날짜는 그대로 유지
                        }
                    }
                }
            }
            if (!entryUpdated) { // 기존 날짜에 대한 항목이 없었으면 새로 추가
                newMoodData.add(moodEntry)
            }

            // 파일에 전체 데이터 다시 쓰기
            openFileOutput(moodFileName, Context.MODE_PRIVATE).use {
                it.write(newMoodData.joinToString("\n").toByteArray())
            }
            Log.d("saveMood", "기분 저장 완료: $moodEntry")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("saveMood", "기분 저장 실패", e)
            Toast.makeText(this, "기분 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawCalendar() {
        gridCalendar.removeAllViews()

        val calendar = currentCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val daysOfWeek = arrayOf("일", "월", "화", "수", "목", "금", "토")
        for (dayName in daysOfWeek) {
            val tvDayName = TextView(this)
            tvDayName.text = dayName
            tvDayName.gravity = Gravity.CENTER
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            tvDayName.layoutParams = params
            gridCalendar.addView(tvDayName)
        }

        for (i in 1 until firstDayOfWeek) {
            val emptyView = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            emptyView.layoutParams = params
            gridCalendar.addView(emptyView)
        }

        for (day in 1..lastDayOfMonth) {
            val tvDay = TextView(this)
            tvDay.text = day.toString()
            tvDay.gravity = Gravity.CENTER
            tvDay.setPadding(8, 16, 8, 16)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            tvDay.layoutParams = params

            val todayCalendar = Calendar.getInstance()
            if (calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
                day == todayCalendar.get(Calendar.DAY_OF_MONTH)) {
                tvDay.setTextColor(Color.BLUE)
                tvDay.setBackgroundColor(Color.parseColor("#FFFFE0")) // 밝은 노란색 (LightYellow)
            }

            gridCalendar.addView(tvDay)
        }

        val totalCells = 42 // 7열 * 6행, 요일 표시 행 제외하고 35 또는 42
        val currentCellsInDateGrid = (firstDayOfWeek - 1) + lastDayOfMonth
        // 요일 표시를 위한 한 줄을 제외하고 5줄 또는 6줄을 만들기 위한 로직으로 변경 필요
        // 현재는 요일 표시 포함해서 6줄 (42칸)을 강제로 채움. UI에 따라 조절 필요.
        val cellsInDayGrid = daysOfWeek.size + (firstDayOfWeek - 1) + lastDayOfMonth
        for(i in 0 until (totalCells - cellsInDayGrid)){
            val emptyView = TextView(this)
             val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            emptyView.layoutParams = params
            gridCalendar.addView(emptyView)
        }
    }
}