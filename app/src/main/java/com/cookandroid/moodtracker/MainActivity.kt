package com.cookandroid.moodtracker

import android.content.Context // Context for file IO
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log // Log for debugging
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Toolbar 임포트 추가
import java.io.BufferedReader // For reading file
import java.io.File // For file checking
import java.io.FileOutputStream // For writing file
import java.io.InputStreamReader // For reading file
import java.io.FileNotFoundException // For file not found
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.view.Menu // 추가
import android.view.MenuItem // 추가
import android.content.Intent // 추가
// import com.cookandroid.moodtracker.MoodListAdapter // MoodListAdapter 임포트 추가 - 위치는 자동으로 정렬될 수 있음

class MainActivity : AppCompatActivity() {

    private lateinit var toolbarMain: Toolbar // Toolbar 참조 변수 추가
    private lateinit var btnPrevMonth: Button
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnNextMonth: Button
    private lateinit var gridCalendar: GridLayout
    private lateinit var btnLogMood: Button
    private lateinit var tvSelectedMoodInfo: TextView // 새로 추가된 TextView 참조

    private lateinit var currentCalendar: Calendar
    private var selectedDateCalendar: Calendar? = null // 사용자가 클릭한 날짜를 저장할 Calendar 인스턴스

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

        toolbarMain = findViewById(R.id.toolbarMain) // Toolbar 참조 연결
        setSupportActionBar(toolbarMain) // Toolbar를 ActionBar로 설정
        supportActionBar?.title = "월별 기분 달력" // ActionBar 타이틀 설정 (선택 사항)

        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        gridCalendar = findViewById(R.id.gridCalendar)
        btnLogMood = findViewById(R.id.btnLogMood)
        tvSelectedMoodInfo = findViewById(R.id.tvSelectedMoodInfo) // 참조 연결

        currentCalendar = Calendar.getInstance()
        selectedDateCalendar = currentCalendar.clone() as Calendar // 초기 선택된 날짜는 오늘

        updateCurrentMonthText()
        updateSelectedMoodInfo(selectedDateCalendar!!)
        drawCalendar()

        // 이전 달 버튼 클릭 이벤트
        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCurrentMonthText()
            updateSelectedMoodInfo(selectedDateCalendar!!)
            drawCalendar()
        }

        // 다음 달 버튼 클릭 이벤트
        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCurrentMonthText()
            updateSelectedMoodInfo(selectedDateCalendar!!)
            drawCalendar()
        }

        // '기분 기록하기' 버튼 클릭 이벤트
        btnLogMood.setOnClickListener {
            selectedDateCalendar?.let {
                showMoodLogDialog(it)
            } ?: run {
                // 기본적으로 오늘 날짜로 다이얼로그를 띄우거나, 날짜를 먼저 선택하라는 메시지 표시
                showMoodLogDialog(Calendar.getInstance())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_stats -> {
                val intent = Intent(this, StatsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateCurrentMonthText() {
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.getDefault())
        tvCurrentMonth.text = sdf.format(currentCalendar.time)
    }

    private fun updateSelectedMoodInfo(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateKey = sdf.format(calendar.time)
        val moodColor = loadMoods()[dateKey]
        val moodName = moods.entries.find { it.value == moodColor }?.key

        if (moodName != null && moodColor != null) {
            tvSelectedMoodInfo.text = "${sdf.format(calendar.time)}: $moodName ($moodColor)"
        } else {
            tvSelectedMoodInfo.text = "${sdf.format(calendar.time)}: 기록된 기분 없음"
        }
    }

    private fun showMoodLogDialog(dateToLog: Calendar) {
        val titleDate = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault()).format(dateToLog.time)
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateToLog.time)

        val loadedMoods = loadMoods()
        val existingMoodColor = loadedMoods[dateKey]
        var currentSelectedPosition = 0 // 기본 선택 위치
        if (existingMoodColor != null) {
            val existingMoodName = moods.entries.find { it.value == existingMoodColor }?.key
            if (existingMoodName != null) {
                val index = moodNames.indexOf(existingMoodName)
                if (index != -1) {
                    currentSelectedPosition = index
                }
            }
        }

        val adapter = MoodListAdapter(this, moods, moodNames, currentSelectedPosition)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("${titleDate} 기분 선택")
        // setSingleChoiceItems 대신 setAdapter 사용
        builder.setSingleChoiceItems(adapter, currentSelectedPosition) { dialog, which ->
            adapter.setSelectedPosition(which) // 어댑터에 선택된 위치 업데이트
            // selectedMoodIndex[0] = which -> 이제 어댑터가 선택 상태를 관리
        }
        builder.setPositiveButton("확인") { _, _ ->
            val selectedPosition = adapter.getSelectedPosition()
            val selectedMoodName = moodNames[selectedPosition]
            val selectedMoodColor = moods[selectedMoodName]

            if (selectedMoodColor != null) {
                saveMood(dateKey, selectedMoodColor)
                Toast.makeText(this, "${dateKey} : $selectedMoodName 기분이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                updateSelectedMoodInfo(dateToLog)
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
                            newMoodData.add(moodEntry)
                            entryUpdated = true
                        } else {
                            newMoodData.add(line)
                        }
                    }
                }
            }
            if (!entryUpdated) {
                newMoodData.add(moodEntry)
            }
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

    private fun loadMoods(): Map<String, String> {
        val moodMap = mutableMapOf<String, String>()
        val file = File(filesDir, moodFileName)

        if (!file.exists()) {
            Log.d("loadMoods", "${moodFileName} 파일 없음. 빈 맵 반환.")
            return moodMap
        }

        try {
            openFileInput(moodFileName).bufferedReader().useLines { lines ->
                lines.forEach {
                    val parts = it.split(":")
                    if (parts.size == 2) {
                        moodMap[parts[0]] = parts[1]
                    }
                }
            }
            Log.d("loadMoods", "기분 로드 완료: ${moodMap.size}개 항목.")
        } catch (e: FileNotFoundException) {
            Log.e("loadMoods", "파일을 찾을 수 없음: $moodFileName", e)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("loadMoods", "기분 로드 실패", e)
            Toast.makeText(this, "기분 로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
        return moodMap
    }

    private fun drawCalendar() {
        gridCalendar.removeAllViews()
        val loadedMoods = loadMoods()

        val displayCalendar = currentCalendar.clone() as Calendar // 현재 달력 표시용
        displayCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = displayCalendar.get(Calendar.DAY_OF_WEEK)
        val lastDayOfMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

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

        val today = Calendar.getInstance()

        for (day in 1..lastDayOfMonth) {
            val dayCellCalendar = displayCalendar.clone() as Calendar
            dayCellCalendar.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayCellCalendar.time)

            val tvDay = TextView(this)
            tvDay.text = day.toString()
            tvDay.gravity = Gravity.CENTER
            tvDay.setPadding(8, 16, 8, 16)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            tvDay.layoutParams = params

            // 날짜 클릭 리스너 설정
            tvDay.setOnClickListener {
                if (dayCellCalendar.after(today) && !isSameDay(dayCellCalendar, today)) {
                    Toast.makeText(this, "미래 날짜의 기분은 기록할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val previouslySelectedDate = selectedDateCalendar?.clone() as Calendar?
                    selectedDateCalendar = dayCellCalendar.clone() as Calendar
                    updateSelectedMoodInfo(selectedDateCalendar!!)
                    
                    // 최적화: 전체 달력을 다시 그리는 대신 이전 선택 셀과 새 선택 셀만 업데이트 할 수도 있지만,
                    // 현재 구조에서는 전체를 다시 그리는 것이 로직을 단순하게 유지하고 모든 상태를 정확히 반영합니다.
                    // 만약 성능 문제가 발생한다면 특정 셀만 업데이트 하는 방식으로 변경 고려 가능합니다.
                    // 만약 성능 문제가 발생한다면 특정 셀만 업데이트 하는 방식으로 변경 고려 가능합니다.
                    drawCalendar() // 선택 변경 시 달력 전체를 다시 그려서 테두리 및 하이라이트 업데이트
                }
            }

            // 기분 기록하기 버튼은 항상 마지막으로 선택된 날짜(selectedDateCalendar)로 동작.
            // 달력의 날짜 클릭 시 selectedDateCalendar가 업데이트 되고, tvSelectedMoodInfo도 업데이트 됨.
            // 그 후 기분 기록하기 버튼을 누르면 showMoodLogDialog(selectedDateCalendar)가 호출되어 해당 날짜의 기분을 기록.

            loadedMoods[dateKey]?.let {
                try {
                    setTodayHighlightOrSelection(tvDay, dayCellCalendar, it)
                } catch (e: IllegalArgumentException) {
                    Log.e("drawCalendar", "잘못된 색상 코드: $it for date $dateKey", e)
                    setTodayHighlightOrSelection(tvDay, dayCellCalendar, null)
                }
            } ?: run {
                setTodayHighlightOrSelection(tvDay, dayCellCalendar, null)
            }
            gridCalendar.addView(tvDay)
        }

        val totalCells = 42
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

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun setTodayHighlightOrSelection(textView: TextView, calendarForDay: Calendar, moodColorHex: String?) {
        val today = Calendar.getInstance()
        val finalDrawable = GradientDrawable()
        finalDrawable.shape = GradientDrawable.RECTANGLE

        // 기본 텍스트 색상 설정
        textView.setTextColor(Color.BLACK)

        // 1. 배경색 결정 로직 (이전과 동일)
        var currentBgColor: Int
        if (moodColorHex != null) { // 기분 기록이 있는 경우
            try {
                currentBgColor = Color.parseColor(moodColorHex)
            } catch (e: IllegalArgumentException) {
                Log.e("HighlightLogic", "잘못된 기분 색상 코드: $moodColorHex for date ${calendarForDay.time}", e)
                currentBgColor = Color.TRANSPARENT // 오류 시 투명
            }
        } else { // 기분 기록이 없는 경우 (moodColorHex == null)
            if (isSameDay(calendarForDay, today) && 
                (selectedDateCalendar == null || !isSameDay(calendarForDay, selectedDateCalendar!!))) {
                currentBgColor = Color.parseColor("#FFFFE0") 
            } else {
                currentBgColor = Color.TRANSPARENT
            }
        }
        finalDrawable.setColor(currentBgColor)

        // 2. 선택된 날짜 테두리 적용 로직 수정
        if (selectedDateCalendar != null) {
            val isSelectedDateActuallyToday = isSameDay(selectedDateCalendar!!, today) // selectedDateCalendar가 실제 '오늘'인지 확인

            if (isSelectedDateActuallyToday) {
                // 선택된 날짜가 '오늘'인 경우 -> 현재 그리는 셀(calendarForDay)도 '오늘'이어야 테두리 표시
                if (isSameDay(calendarForDay, today)) {
                    finalDrawable.setStroke(3, Color.BLACK)
                }
            } else {
                // 선택된 날짜가 '오늘'이 아닌 다른 날짜인 경우 -> 현재 그리는 셀이 그 '선택된 날짜'와 일치하면 테두리 표시
                if (isSameDay(calendarForDay, selectedDateCalendar!!)) {
                    finalDrawable.setStroke(3, Color.BLACK)
                }
            }
        }

        // 3. 오늘 날짜 텍스트 색상 적용 (이전과 동일)
        if (isSameDay(calendarForDay, today)) {
            textView.setTextColor(Color.BLUE)
        }

        textView.background = finalDrawable
    }
}