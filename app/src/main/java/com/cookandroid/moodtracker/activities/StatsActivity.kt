package com.cookandroid.moodtracker

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
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

    // 기간 선택 UI 요소 추가
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var tvSelectedDateRange: TextView
    private lateinit var spinnerPredefinedRanges: Spinner

    private lateinit var currentStatsCalendar: Calendar
    private var selectedStartDate: Calendar? = null
    private var selectedEndDate: Calendar? = null

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

        // 월별 네비게이션
        btnPrevStatsMonth = findViewById(R.id.btnPrevStatsMonth)
        tvCurrentStatsMonth = findViewById(R.id.tvCurrentStatsMonth)
        btnNextStatsMonth = findViewById(R.id.btnNextStatsMonth)

        // 기간 선택 UI 초기화
        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate = findViewById(R.id.btnEndDate)
        tvSelectedDateRange = findViewById(R.id.tvSelectedDateRange)
        spinnerPredefinedRanges = findViewById(R.id.spinnerPredefinedRanges)

        currentStatsCalendar = Calendar.getInstance()

        pieChart = findViewById(R.id.pieChartStats)
        setupPieChart()

        // Spinner 설정
        ArrayAdapter.createFromResource(
            this,
            R.array.predefined_date_ranges,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPredefinedRanges.adapter = adapter
        }

        spinnerPredefinedRanges.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)

                when (position) {
                    1 -> { // 최근 7일
                        selectedEndDate = today.clone() as Calendar
                        selectedStartDate = today.clone() as Calendar
                        selectedStartDate!!.add(Calendar.DAY_OF_YEAR, -6)
                        updateDateRangeText()
                        loadPieChartData()
                    }
                    2 -> { // 최근 30일
                        selectedEndDate = today.clone() as Calendar
                        selectedStartDate = today.clone() as Calendar
                        selectedStartDate!!.add(Calendar.DAY_OF_YEAR, -29)
                        updateDateRangeText()
                        loadPieChartData()
                    }
                    3 -> { // 이번 주
                        selectedStartDate = today.clone() as Calendar
                        selectedStartDate!!.firstDayOfWeek = Calendar.MONDAY // 주의 시작을 월요일로 설정 (선택적)
                        selectedStartDate!!.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        
                        selectedEndDate = selectedStartDate!!.clone() as Calendar
                        selectedEndDate!!.add(Calendar.DAY_OF_YEAR, 6)

                        // 만약 오늘이 이번 주의 일요일 이후라면 (예: 다음 주로 넘어갔지만 아직 월요일이 안된 경우)
                        // 또는 현재 주의 계산이 미래를 포함한다면, 종료일을 오늘로 조정
                        val todayForWeekComparison = Calendar.getInstance()
                        todayForWeekComparison.set(Calendar.HOUR_OF_DAY, 0)
                        todayForWeekComparison.set(Calendar.MINUTE, 0)
                        todayForWeekComparison.set(Calendar.SECOND, 0)
                        todayForWeekComparison.set(Calendar.MILLISECOND, 0)
                        if (selectedEndDate!!.after(todayForWeekComparison)) {
                           selectedEndDate = todayForWeekComparison
                        }
                        // 시작일이 종료일보다 늦는 경우 (예: 월요일에 "이번 주" 선택 시)
                        if (selectedStartDate!!.after(selectedEndDate!!)) {
                            selectedStartDate = selectedEndDate!!.clone() as Calendar
                            selectedStartDate!!.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                             // 만약 월요일이 오늘보다 미래면, 시작일을 오늘로.
                            if(selectedStartDate!!.after(todayForWeekComparison)){
                                selectedStartDate = todayForWeekComparison
                            }
                        }


                        updateDateRangeText()
                        loadPieChartData()
                    }
                    // 0번 인덱스 ("기간 선택...")는 아무것도 하지 않음
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 동작
            }
        }

        // 초기 텍스트 설정 및 데이터 로드
        updateDateRangeText()
        updateCurrentStatsMonthText()
        loadPieChartData()

        btnPrevStatsMonth.setOnClickListener {
            selectedStartDate = null
            selectedEndDate = null
            spinnerPredefinedRanges.setSelection(0, false) // Spinner 초기화
            currentStatsCalendar.add(Calendar.MONTH, -1)
            updateCurrentStatsMonthText()
            updateDateRangeText()
            loadPieChartData()
        }

        btnNextStatsMonth.setOnClickListener {
            selectedStartDate = null
            selectedEndDate = null
            spinnerPredefinedRanges.setSelection(0, false) // Spinner 초기화
            currentStatsCalendar.add(Calendar.MONTH, 1)
            updateCurrentStatsMonthText()
            updateDateRangeText()
            loadPieChartData()
        }

        btnStartDate.setOnClickListener {
            showDatePickerDialog(isStartDate = true)
            spinnerPredefinedRanges.setSelection(0, false) // Spinner 초기화
        }

        btnEndDate.setOnClickListener {
            showDatePickerDialog(isStartDate = false)
            spinnerPredefinedRanges.setSelection(0, false) // Spinner 초기화
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        // val calendar = Calendar.getInstance()
        val initialCalendar = if (isStartDate && selectedStartDate != null) {
            selectedStartDate!!
        } else if (!isStartDate && selectedEndDate != null) {
            selectedEndDate!!
        } else {
            currentStatsCalendar
        }

        val year = initialCalendar.get(Calendar.YEAR)
        val month = initialCalendar.get(Calendar.MONTH)
        val day = initialCalendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            val selectedCal = Calendar.getInstance()
            selectedCal.set(selectedYear, selectedMonth, selectedDayOfMonth)
            selectedCal.set(Calendar.HOUR_OF_DAY, 0)
            selectedCal.set(Calendar.MINUTE, 0)
            selectedCal.set(Calendar.SECOND, 0)
            selectedCal.set(Calendar.MILLISECOND, 0)

            if (isStartDate) {
                selectedStartDate = selectedCal
                if (selectedEndDate != null && selectedStartDate!!.after(selectedEndDate)) {
                    selectedEndDate = selectedStartDate!!.clone() as Calendar
                    // Toast.makeText(this, "종료 날짜가 시작 날짜와 같거나 이후로 조정되었습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                selectedEndDate = selectedCal
                if (selectedStartDate != null && selectedEndDate!!.before(selectedStartDate)) {
                    selectedStartDate = selectedEndDate!!.clone() as Calendar
                   // Toast.makeText(this, "시작 날짜가 종료 날짜와 같거나 이전으로 조정되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            updateDateRangeText()
            loadPieChartData()
            // spinnerPredefinedRanges.setSelection(0, false) // 날짜 직접 선택 시 Spinner 초기화 - 각 버튼 클릭 리스너로 이동
        }, year, month, day)
        
        if (!isStartDate && selectedStartDate != null) {
            datePickerDialog.datePicker.minDate = selectedStartDate!!.timeInMillis
        }
        if (isStartDate && selectedEndDate != null) {
            datePickerDialog.datePicker.maxDate = selectedEndDate!!.timeInMillis
        }

        datePickerDialog.show()
    }

    private fun updateDateRangeText() {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        if (selectedStartDate != null && selectedEndDate != null) {
            tvSelectedDateRange.text = "기간: ${sdf.format(selectedStartDate!!.time)} ~ ${sdf.format(selectedEndDate!!.time)}"
            tvCurrentStatsMonth.text = "사용자 지정 기간"
        } else if (selectedStartDate != null) {
            tvSelectedDateRange.text = "시작: ${sdf.format(selectedStartDate!!.time)} ~ (종료일 선택)"
            tvCurrentStatsMonth.text = "기간 설정 중"
        } else if (selectedEndDate != null) {
            tvSelectedDateRange.text = "(시작일 선택) ~ 종료: ${sdf.format(selectedEndDate!!.time)}"
            tvCurrentStatsMonth.text = "기간 설정 중"
        } else {
            tvSelectedDateRange.text = "표시: 월별 보기"
            updateCurrentStatsMonthText()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun updateCurrentStatsMonthText() {
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())
        if (selectedStartDate == null && selectedEndDate == null) {
            tvCurrentStatsMonth.text = sdf.format(currentStatsCalendar.time)
        }
    }

    private fun setupPieChart() {
        pieChart.isDrawHoleEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setCenterTextSize(16f)
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
        val moodCounts = mutableMapOf<String, Int>()
        val sdfFileParse = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        var chartTitleDateString: String

        if (selectedStartDate != null && selectedEndDate != null) {
            if (selectedStartDate!!.after(selectedEndDate!!)) {
                Toast.makeText(this, "시작 날짜는 종료 날짜보다 이전이거나 같아야 합니다.", Toast.LENGTH_LONG).show()
                pieChart.data = null
                pieChart.centerText = "기간 설정 오류"
                pieChart.invalidate()
                return
            }
            val sdfDisplay = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            chartTitleDateString = "${sdfDisplay.format(selectedStartDate!!.time)} ~ ${sdfDisplay.format(selectedEndDate!!.time)}"
            pieChart.centerText = chartTitleDateString

            for ((dateKey, recordedColorHex) in recordedMoodData) {
                try {
                    val recordDate = sdfFileParse.parse(dateKey) ?: continue
                    val recordCalendar = Calendar.getInstance()
                    recordCalendar.time = recordDate
                    recordCalendar.set(Calendar.HOUR_OF_DAY, 0); recordCalendar.set(Calendar.MINUTE, 0); recordCalendar.set(Calendar.SECOND, 0); recordCalendar.set(Calendar.MILLISECOND, 0)
                    
                    val startCal = selectedStartDate!!.clone() as Calendar
                    startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
                    
                    val endCal = selectedEndDate!!.clone() as Calendar
                    endCal.set(Calendar.HOUR_OF_DAY, 0); endCal.set(Calendar.MINUTE, 0); endCal.set(Calendar.SECOND, 0); endCal.set(Calendar.MILLISECOND, 0)

                    if (!recordCalendar.before(startCal) && !recordCalendar.after(endCal)) {
                        val moodName = currentValidMoodsMap.entries.find { it.value.equals(recordedColorHex, ignoreCase = true) }?.key
                        if (moodName != null) {
                            moodCounts[moodName] = (moodCounts[moodName] ?: 0) + 1
                        } else {
                            Log.d("StatsActivity", "Ignoring record (range) with color $recordedColorHex for date $dateKey")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("StatsActivity", "Error processing mood data (range) for $dateKey: ${e.message}", e)
                }
            }

        } else {
            val targetYear = currentStatsCalendar.get(Calendar.YEAR)
            val targetMonth = currentStatsCalendar.get(Calendar.MONTH)
            chartTitleDateString = "${targetYear}년 ${targetMonth + 1}월"
            pieChart.centerText = chartTitleDateString

            for ((dateKey, recordedColorHex) in recordedMoodData) {
                try {
                    val dateParts = dateKey.split("-")
                    if (dateParts.size == 3) {
                        val year = dateParts[0].toInt()
                        val month = dateParts[1].toInt() - 1

                        if (year == targetYear && month == targetMonth) {
                            val moodName = currentValidMoodsMap.entries.find { it.value.equals(recordedColorHex, ignoreCase = true) }?.key
                            if (moodName != null) {
                                moodCounts[moodName] = (moodCounts[moodName] ?: 0) + 1
                            } else {
                                Log.d("StatsActivity", "Ignoring record (month) with color $recordedColorHex for date $dateKey")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("StatsActivity", "Error processing mood data (month) for $dateKey: ${e.message}", e)
                }
            }
        }

        val entries = ArrayList<PieEntry>()
        val sliceColors = ArrayList<Int>()

        if (moodCounts.isEmpty()) {
            pieChart.centerText = "$chartTitleDateString 기록 없음"
            pieChart.data = null
        } else {
            pieChart.centerText = chartTitleDateString
            moodCounts.forEach { (name, count) ->
                entries.add(PieEntry(count.toFloat(), name))
                val colorHex = currentValidMoodsMap[name]
                try {
                    sliceColors.add(Color.parseColor(colorHex ?: "#CCCCCC"))
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
        }
        
        pieChart.invalidate()
    }
} 