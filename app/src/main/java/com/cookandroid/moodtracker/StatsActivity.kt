package com.cookandroid.moodtracker

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var toolbar: Toolbar
    private lateinit var btnPrevStatsMonth: Button
    private lateinit var tvCurrentStatsMonth: TextView
    private lateinit var btnNextStatsMonth: Button

    private lateinit var currentStatsCalendar: Calendar
    private lateinit var customMoodRepository: CustomMoodRepository

    // 기본 감정 정의
    private val defaultMoods = mapOf(
        "행복" to "#FFEB3B",
        "좋음" to "#AED581",
        "보통" to "#FFF59D",
        "나쁨" to "#FFAB91",
        "슬픔" to "#B0BEC5"
    )
    private val moodFileName = "mood_data.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        toolbar = findViewById(R.id.toolbarStats)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "기분 통계"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        customMoodRepository = CustomMoodRepository(this)

        btnPrevStatsMonth = findViewById(R.id.btnPrevStatsMonth)
        tvCurrentStatsMonth = findViewById(R.id.tvCurrentStatsMonth)
        btnNextStatsMonth = findViewById(R.id.btnNextStatsMonth)

        currentStatsCalendar = Calendar.getInstance()

        pieChart = findViewById(R.id.pieChartStats)
        setupPieChart()

        updateCurrentStatsMonthText()
        loadPieChartData()

        btnPrevStatsMonth.setOnClickListener {
            currentStatsCalendar.add(Calendar.MONTH, -1)
            updateCurrentStatsMonthText()
            loadPieChartData()
        }

        btnNextStatsMonth.setOnClickListener {
            currentStatsCalendar.add(Calendar.MONTH, 1)
            updateCurrentStatsMonthText()
            loadPieChartData()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // onBackPressedDispatcher.onBackPressed() // API 33+ 에서 권장
        finish() // 단순 이전 화면 이동을 위해 finish() 사용
        return true
    }

    private fun updateCurrentStatsMonthText() {
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())
        tvCurrentStatsMonth.text = sdf.format(currentStatsCalendar.time)
    }

    private fun setupPieChart() {
        pieChart.isDrawHoleEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.centerText = "월별 기분"
        pieChart.setCenterTextSize(18f)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
    }

    private fun getCurrentDisplayableMoodsMap(): Map<String, String> {
        val allMoodsMap = mutableMapOf<String, String>()
        defaultMoods.forEach { (name, color) ->
            allMoodsMap[name] = color
        }
        val customMoods = customMoodRepository.loadCustomMoods()
        customMoods.forEach { mood ->
             // 사용자 정의 감정이 기본 감정과 이름이 같다면 사용자 정의 감정으로 덮어쓰도록 허용
            allMoodsMap[mood.name] = mood.colorHex
        }
        return allMoodsMap
    }

    private fun loadMoodsFromFile(): Map<String, String> {
        val moodMap = mutableMapOf<String, String>()
        val file = File(filesDir, moodFileName)
        if (!file.exists()) return moodMap
        try {
            openFileInput(moodFileName).bufferedReader().useLines { lines ->
                lines.forEach {
                    val parts = it.split(":", limit = 2)
                    if (parts.size == 2) moodMap[parts[0]] = parts[1]
                }
            }
        } catch (e: Exception) {
            Log.e("StatsActivity_LoadFile", "Error loading $moodFileName", e)
        }
        return moodMap
    }

    private fun loadPieChartData() {
        val recordedMoodData = loadMoodsFromFile()
        val currentValidMoodsMap = getCurrentDisplayableMoodsMap()
        val moodCounts = mutableMapOf<String, Int>() // 기분 이름 -> 빈도수

        val targetYear = currentStatsCalendar.get(Calendar.YEAR)
        val targetMonth = currentStatsCalendar.get(Calendar.MONTH)

        for ((dateKey, recordedColorHex) in recordedMoodData) {
            try {
                val dateParts = dateKey.split("-")
                if (dateParts.size == 3) {
                    val year = dateParts[0].toInt()
                    val month = dateParts[1].toInt() - 1

                    if (year == targetYear && month == targetMonth) {
                        // 기록된 colorHex가 현재 유효한 감정의 색상인지 확인
                        val moodName = currentValidMoodsMap.entries.find { it.value.equals(recordedColorHex, ignoreCase = true) }?.key
                        if (moodName != null) {
                            moodCounts[moodName] = (moodCounts[moodName] ?: 0) + 1
                        } else {
                            // 유효하지 않은 (예: 삭제된) 감정의 기록은 무시
                            Log.d("StatsActivity", "Ignoring record with color $recordedColorHex for date $dateKey as it's not a currently valid mood.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StatsActivity", "Error processing mood data for $dateKey", e)
            }
        }

        val entries = ArrayList<PieEntry>()
        val sliceColors = ArrayList<Int>()

        if (moodCounts.isEmpty()) {
            pieChart.centerText = "${targetYear}년 ${targetMonth + 1}월 기록 없음"
            pieChart.data = null
        } else {
            pieChart.centerText = "${targetYear}년 ${targetMonth + 1}월 기분"
            moodCounts.forEach { (name, count) ->
                entries.add(PieEntry(count.toFloat(), name))
                val colorHex = currentValidMoodsMap[name] // 이름으로 색상 다시 찾기
                try {
                    sliceColors.add(Color.parseColor(colorHex ?: "#CCCCCC")) // 색상 없으면 회색
                } catch (e: IllegalArgumentException) {
                    sliceColors.add(Color.GRAY)
                    Log.e("StatsActivity", "Invalid color hex for pie slice: $name - $colorHex")
                }
            }
        }

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "")
            dataSet.colors = sliceColors
            dataSet.valueFormatter = PercentFormatter(pieChart)
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = Color.BLACK
            
            val data = PieData(dataSet)
            pieChart.data = data
        } else {
             // entries가 비었지만 moodCounts는 비어있지 않은 경우는 위에서 처리됨 (모두 무시된 경우)
            // 여기서도 기록 없음을 한번 더 명시 가능
            pieChart.centerText = "${targetYear}년 ${targetMonth + 1}월 기록 없음"
            pieChart.data = null
        }
        pieChart.invalidate() // 차트 갱신
    }
} 