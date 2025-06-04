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

    // MainActivity에서 정의한 기분 데이터를 여기에서도 활용할 수 있도록 가져오거나 유사하게 정의합니다.
    // 우선 간단하게 StatsActivity 내부에 정의합니다.
    private val moods = mapOf(
        "행복" to "#FFEB3B", // Yellow
        "좋음" to "#AED581", // Light Green
        "보통" to "#FFF59D", // Pale Yellow
        "나쁨" to "#FFAB91", // Light Orange
        "슬픔" to "#B0BEC5"  // Blue Grey
    )
    private val moodFileName = "mood_data.txt" // MainActivity와 동일한 파일 이름

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        toolbar = findViewById(R.id.toolbarStats)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "기분 통계"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnPrevStatsMonth = findViewById(R.id.btnPrevStatsMonth)
        tvCurrentStatsMonth = findViewById(R.id.tvCurrentStatsMonth)
        btnNextStatsMonth = findViewById(R.id.btnNextStatsMonth)

        currentStatsCalendar = Calendar.getInstance()

        pieChart = findViewById(R.id.pieChartStats)
        setupPieChart() // 기본 차트 설정

        updateCurrentStatsMonthText() // 현재 월 표시
        loadPieChartData() // 해당 월의 데이터로 차트 로드 (초기에는 더미/빈 데이터일 수 있음)

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
        onBackPressedDispatcher.onBackPressed()
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
        pieChart.centerText = "월별 기분" // 기본 센터 텍스트
        pieChart.setCenterTextSize(18f)
        pieChart.description.isEnabled = false
        // pieChart.setNoDataText("해당 월의 기분 기록이 없습니다.") // 데이터 없을 때 메시지

        val legend = pieChart.legend
        legend.isEnabled = true
    }

    // MainActivity의 loadMoods와 유사한 함수 (추후 리팩토링 고려)
    private fun loadMoodsFromFile(): Map<String, String> {
        val moodMap = mutableMapOf<String, String>()
        val file = File(filesDir, moodFileName)

        if (!file.exists()) {
            Log.d("loadMoodsFromFile", "${moodFileName} 파일 없음. 빈 맵 반환.")
            return moodMap
        }

        try {
            openFileInput(moodFileName).bufferedReader().useLines { lines ->
                lines.forEach {
                    val parts = it.split(":")
                    if (parts.size == 2) {
                        moodMap[parts[0]] = parts[1] // YYYY-MM-DD to ColorHex
                    }
                }
            }
            Log.d("loadMoodsFromFile", "기분 로드 완료: ${moodMap.size}개 항목.")
        } catch (e: FileNotFoundException) {
            Log.e("loadMoodsFromFile", "파일을 찾을 수 없음: $moodFileName", e)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("loadMoodsFromFile", "기분 로드 실패", e)
            Toast.makeText(this, "기분 로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
        return moodMap
    }

    private fun loadPieChartData() {
        val allMoodData = loadMoodsFromFile()
        val entries = ArrayList<PieEntry>()
        val moodCounts = mutableMapOf<String, Int>() // 기분 이름 -> 빈도수

        // 현재 선택된 연/월 (currentStatsCalendar 기준)
        val targetYear = currentStatsCalendar.get(Calendar.YEAR)
        val targetMonth = currentStatsCalendar.get(Calendar.MONTH) // 0-11

        Log.d("loadPieChartData", "선택된 연/월: $targetYear/${targetMonth + 1}")

        for ((dateKey, colorHex) in allMoodData) {
            try {
                val dateParts = dateKey.split("-") // YYYY-MM-DD
                if (dateParts.size == 3) {
                    val year = dateParts[0].toInt()
                    val month = dateParts[1].toInt() - 1 // 0-11
                    // val day = dateParts[2].toInt() // 일자는 현재 사용 안함

                    if (year == targetYear && month == targetMonth) {
                        // 해당 월의 데이터 -> colorHex로 기분 이름 찾기
                        val moodName = moods.entries.find { it.value == colorHex }?.key
                        if (moodName != null) {
                            moodCounts[moodName] = (moodCounts[moodName] ?: 0) + 1
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("loadPieChartData", "날짜 파싱 또는 데이터 처리 오류: $dateKey", e)
            }
        }
        
        Log.d("loadPieChartData", "계산된 기분 빈도: $moodCounts")

        if (moodCounts.isEmpty()) {
            pieChart.centerText = "${targetYear}년 ${targetMonth + 1}월 기록 없음"
            pieChart.data = null // 이전 데이터 클리어
        } else {
            pieChart.centerText = "${targetYear}년 ${targetMonth + 1}월 기분 통계" // 또는 그냥 "월별 기분"
            moods.keys.forEach { moodName -> // 정의된 모든 기분 종류에 대해 처리 (0인 경우도 포함)
                val count = moodCounts[moodName] ?: 0
                if (count > 0) { // 빈도가 0보다 큰 경우에만 차트에 추가 (선택 사항)
                    entries.add(PieEntry(count.toFloat(), moodName))
                }
            }
            // 만약 entries가 비어있다면 (즉, 모든 기분의 count가 0이었다면)
            if (entries.isEmpty() && moodCounts.isNotEmpty()) { // moodCounts는 있으나 모두 0인 경우
                 pieChart.centerText = "${targetYear}년 ${targetMonth + 1}월 기록 없음"
                 pieChart.data = null
            } else if (entries.isEmpty()){ // moodCounts도 비어있는 초기 상태
                 pieChart.centerText = "${targetYear}년 ${targetMonth + 1}월 기록 없음"
                 pieChart.data = null
            }
        }
        
        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "") // 범례 제목은 비워둠 (각 슬라이스 라벨로 충분)
            // dataSet.setDrawValues(true) // 값 표시 여부 (퍼센트 등) - PercentFormatter 사용 시 자동
            dataSet.valueFormatter = PercentFormatter(pieChart) // 퍼센트 값 사용
            pieChart.setUsePercentValues(true) // 차트 자체도 퍼센트 기반으로 설정

            val moodColorsList = ArrayList<Int>()
            entries.forEach { entry ->
                moods[entry.label]?.let { colorHex ->
                    try {
                        moodColorsList.add(Color.parseColor(colorHex))
                    } catch (e: IllegalArgumentException) {
                        moodColorsList.add(Color.GRAY) // 잘못된 색상 코드면 회색
                    }
                } ?: moodColorsList.add(Color.LTGRAY) // moods 맵에 없는 라벨이면 연한 회색 (이런 경우는 없어야 함)
            }
            dataSet.colors = moodColorsList
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = Color.BLACK
            // dataSet.setSliceSpace(2f) // 슬라이스 간 간격

            val data = PieData(dataSet)
            pieChart.data = data
        }
        // entries가 비어있으면 pieChart.data = null 이후 invalidate만 호출
        pieChart.invalidate() // 차트 새로고침
    }
} 