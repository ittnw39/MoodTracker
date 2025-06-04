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

    private lateinit var customMoodRepository: CustomMoodRepository // Repository 추가
    private var moodsMap: Map<String, String> = emptyMap() // 기분 데이터를 저장할 멤버 변수

    // 기본 감정 데이터 정의 (이름 to 색상 코드)
    private val defaultMoods = mapOf(
        "행복" to "#FFEB3B", // Yellow
        "좋음" to "#AED581", // Light Green
        "보통" to "#FFF59D", // Pale Yellow
        "나쁨" to "#FFAB91", // Light Orange
        "슬픔" to "#B0BEC5"  // Blue Grey
    )
    private val moodNames = defaultMoods.keys.toTypedArray()
    private val moodFileName = "mood_data.txt"

    // Helper function to check if two Calendar instances represent the same day
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    // 사용자 이전 코드 버전의 setTodayHighlightOrSelection 함수로 대체
    private fun setTodayHighlightOrSelection(textView: TextView, calendarForDay: Calendar, moodColorHex: String?) {
        val today = Calendar.getInstance()
        val finalDrawable = GradientDrawable()
        finalDrawable.shape = GradientDrawable.RECTANGLE

        // 기본 텍스트 색상 설정
        textView.setTextColor(Color.BLACK)

        // 1. 배경색 결정 로직
        var currentBgColor: Int
        if (moodColorHex != null) { // 기분 기록이 있는 경우
            try {
                currentBgColor = Color.parseColor(moodColorHex)
            } catch (e: IllegalArgumentException) {
                Log.e("HighlightLogic", "잘못된 기분 색상 코드: $moodColorHex for date ${calendarForDay.time}", e)
                currentBgColor = Color.TRANSPARENT // 오류 시 투명
            }
        } else { // 기분 기록이 없는 경우 (moodColorHex == null)
            // 오늘 날짜이면서, (선택된 날짜가 없거나 또는 선택된 날짜가 오늘이 아닐 때) 오늘 날짜 배경 강조
            if (isSameDay(calendarForDay, today) && 
                (selectedDateCalendar == null || !isSameDay(calendarForDay, selectedDateCalendar!!))) {
                currentBgColor = Color.parseColor("#FFFFE0") 
            } else {
                currentBgColor = Color.TRANSPARENT
            }
        }
        finalDrawable.setColor(currentBgColor)

        // 2. 선택된 날짜 테두리 적용 로직 수정
        // selectedDateCalendar가 null이 아니고, 현재 그리는 셀(calendarForDay)이 selectedDateCalendar와 같은 날짜일 때 테두리 적용
        if (selectedDateCalendar != null && isSameDay(calendarForDay, selectedDateCalendar!!)) {
            finalDrawable.setStroke(3, Color.BLACK) // 두꺼운 검은색 테두리
        } else if (isSameDay(calendarForDay, today)) { // 오늘 날짜이지만 선택되지 않은 경우 (위의 if 조건에서 이미 처리되지 않았다면)
             // 오늘 날짜에 대한 기본 테두리 (예: 파란색 얇은 테두리). 만약 기분 색상이 있다면 그 위에 덧씌워질 수 있음.
             // 또는, 오늘 날짜는 텍스트 색상으로만 구분하고 테두리는 선택 시에만 줄 수도 있음.
             // 이전 제공 코드의 정확한 "오늘 날짜 테두리" 로직이 불분명하여, 선택된 날짜 테두리 우선으로 둠.
             // 만약 moodColorHex가 없고, 선택도 안된 '오늘' 이면 위 currentBgColor가 #FFFFE0가 됨.
             // 여기서 추가적인 오늘 날짜 테두리를 원하면 추가 가능. 현재는 선택된 날짜 테두리만 명확히 적용.
        }


        // 3. 오늘 날짜 텍스트 색상 적용 (이전과 동일하게)
        if (isSameDay(calendarForDay, today)) {
            textView.setTextColor(Color.BLUE)
        }

        textView.background = finalDrawable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbarMain = findViewById(R.id.toolbarMain) // Toolbar 참조 연결
        setSupportActionBar(toolbarMain) // Toolbar를 ActionBar로 설정
        supportActionBar?.title = "월별 기분 달력" // ActionBar 타이틀 설정 (선택 사항)

        customMoodRepository = CustomMoodRepository(this) // Repository 초기화

        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        gridCalendar = findViewById(R.id.gridCalendar)
        btnLogMood = findViewById(R.id.btnLogMood)
        tvSelectedMoodInfo = findViewById(R.id.tvSelectedMoodInfo) // 참조 연결

        currentCalendar = Calendar.getInstance()
        selectedDateCalendar = currentCalendar.clone() as Calendar // 초기 선택된 날짜는 오늘

        moodsMap = loadMoodsFromFile() // onCreate에서 기분 데이터 로드

        updateCurrentMonthText()
        updateSelectedMoodInfo(selectedDateCalendar!!) // 로드된 moodsMap 사용
        drawCalendar() // 로드된 moodsMap 사용

        // 이전 달 버튼 클릭 이벤트
        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCurrentMonthText()
            // moodsMap은 변경되지 않았으므로 다시 로드할 필요 없음
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

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume 호출됨 - 데이터 및 달력 새로고침")
        moodsMap = loadMoodsFromFile() // onResume에서 기분 데이터 다시 로드
        updateCurrentMonthText() // 달 표시 업데이트
        drawCalendar() // 달력 새로고침 (변경된 moodsMap 사용)
        selectedDateCalendar?.let {
            updateSelectedMoodInfo(it) // 선택된 날짜 정보 업데이트 (변경된 moodsMap 사용)
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
            R.id.action_manage_moods -> { // "감정 관리" 메뉴 선택 시
                val intent = Intent(this, ManageMoodsActivity::class.java)
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

    // 모든 감정(기본+사용자) 목록을 반환하는 헬퍼 함수
    private fun getAllDisplayableMoods(): List<Pair<String, String>> {
        val allMoods = mutableListOf<Pair<String, String>>()
        // 기본 감정 추가
        defaultMoods.forEach { (name, color) ->
            allMoods.add(Pair(name, color))
        }
        // 사용자 정의 감정 추가
        val customMoods = customMoodRepository.loadCustomMoods()
        customMoods.forEach { mood ->
            allMoods.add(Pair(mood.name, mood.colorHex))
        }
        return allMoods
    }

    private fun updateSelectedMoodInfo(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateKey = sdf.format(calendar.time)
        // 이제 멤버 변수 moodsMap 사용
        val moodColor = moodsMap[dateKey] 
        
        val allDisplayableMoods = getAllDisplayableMoods()
        val moodName = allDisplayableMoods.find { it.second == moodColor }?.first

        if (moodName != null && moodColor != null) {
            tvSelectedMoodInfo.text = "${sdf.format(calendar.time)}: $moodName ($moodColor)"
        } else {
            tvSelectedMoodInfo.text = "${sdf.format(calendar.time)}: 기록된 기분 없음"
        }
    }

    private fun showMoodLogDialog(dateToLog: Calendar) {
        val titleDate = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault()).format(dateToLog.time)
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateToLog.time)

        val displayableMoods = getAllDisplayableMoods()
        val moodNamesForDialog = displayableMoods.map { it.first }.toTypedArray()
        // val moodColorsForDialog = displayableMoods.map { it.second }.toTypedArray() // 직접 사용되지 않음

        // 멤버 변수 moodsMap 사용
        val existingMoodColor = moodsMap[dateKey] 
        var currentSelectedPosition = 0
        if (existingMoodColor != null) {
            // displayableMoods에서 올바른 인덱스를 찾아야 함
            val index = displayableMoods.indexOfFirst { it.second == existingMoodColor }
            if (index != -1) {
                currentSelectedPosition = index
            }
        }

        val adapter = MoodListAdapter(this, displayableMoods.associate { it.first to it.second }, moodNamesForDialog, currentSelectedPosition)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("${titleDate} 기분 선택")
        builder.setSingleChoiceItems(adapter, currentSelectedPosition) { _, which ->
            adapter.setSelectedPosition(which)
        }
        builder.setPositiveButton("확인") { _, _ ->
            val selectedPosition = adapter.getSelectedPosition()
            if (selectedPosition < 0 || selectedPosition >= displayableMoods.size) {
                Toast.makeText(this, "오류: 유효하지 않은 선택입니다.", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val selectedMoodName = displayableMoods[selectedPosition].first
            val selectedMoodColor = displayableMoods[selectedPosition].second
            saveMoodToFile(dateKey, selectedMoodColor)
            moodsMap = loadMoodsFromFile() // 기분 저장 후 moodsMap 갱신
            updateSelectedMoodInfo(dateToLog)
            drawCalendar()
            Toast.makeText(this, "${selectedMoodName}이(가) 기록되었습니다.", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("취소", null)
        builder.setNeutralButton("삭제") { _, _ ->
            deleteMoodFromFile(dateKey)
            moodsMap = loadMoodsFromFile() // 기분 삭제 후 moodsMap 갱신
            updateSelectedMoodInfo(dateToLog)
            drawCalendar()
            Toast.makeText(this, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun saveMoodToFile(date: String, moodColor: String) {
        try {
            val currentMoods = moodsMap.toMutableMap()
            currentMoods[date] = moodColor
            
            val fileContents = StringBuilder()
            currentMoods.forEach { (d, mc) ->
                fileContents.append("$d:$mc\n")
            }

            openFileOutput(moodFileName, Context.MODE_PRIVATE).use {
                it.write(fileContents.toString().toByteArray())
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "파일 저장 실패", e)
            Toast.makeText(this, "기분 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteMoodFromFile(date: String) {
        try {
            val currentMoods = moodsMap.toMutableMap()
            if (currentMoods.containsKey(date)) {
                currentMoods.remove(date)
                
                val fileContents = StringBuilder()
                currentMoods.forEach { (d, mc) ->
                    fileContents.append("$d:$mc\n")
                }

                openFileOutput(moodFileName, Context.MODE_PRIVATE).use {
                    it.write(fileContents.toString().toByteArray())
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "파일에서 기분 삭제 실패", e)
            Toast.makeText(this, "기록 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMoodsFromFile(): Map<String, String> {
        val moods = mutableMapOf<String, String>()
        try {
            val file = File(filesDir, moodFileName)
            if (file.exists()) {
                openFileInput(moodFileName).bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split(":", limit = 2)
                        if (parts.size == 2) {
                            moods[parts[0]] = parts[1]
                        }
                    }
                }
            } else {
                Log.i("MainActivity", "$moodFileName 파일이 존재하지 않습니다. 새로 생성될 수 있습니다.")
            }
        } catch (e: FileNotFoundException) {
            Log.w("MainActivity", "$moodFileName 파일을 찾을 수 없습니다.", e)
        } catch (e: Exception) {
            Log.e("MainActivity", "$moodFileName 파일 로드 중 오류 발생", e)
        }
        return moods
    }

    private fun drawCalendar() {
        gridCalendar.removeAllViews()
        val loadedMoods = moodsMap

        // currentCalendar를 복제하여 displayCalendar로 사용 (원본 currentCalendar 변경 방지)
        val displayCalendar = currentCalendar.clone() as Calendar
        displayCalendar.set(Calendar.DAY_OF_MONTH, 1) // 해당 월의 1일로 설정

        // DAY_OF_WEEK는 일요일(1) ~ 토요일(7)을 반환
        val firstDayOfWeek = displayCalendar.get(Calendar.DAY_OF_WEEK)
        val lastDayOfMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance() // 오늘 날짜 가져오기

        // 요일 이름 (일, 월, 화...) 표시
        val daysOfWeekArray = arrayOf("일", "월", "화", "수", "목", "금", "토")
        for (dayName in daysOfWeekArray) {
            val tvDayName = TextView(this)
            tvDayName.text = dayName
            tvDayName.gravity = Gravity.CENTER
            // tvDayName.textSize = 14f // 이전 코드에는 명시적 textSize 없음
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                // rowSpec은 UNDEFINED 사용 시 명시하지 않음
            }
            // params.setMargins(4,4,4,4) // 이전 코드에는 명시적 margins 없음
            tvDayName.layoutParams = params
            gridCalendar.addView(tvDayName)
        }

        // 해당 월의 첫 날짜 앞에 빈 칸 채우기
        // firstDayOfWeek는 1(일요일)부터 시작하므로, (i in 1 until firstDayOfWeek) 루프 사용
        for (i in 1 until firstDayOfWeek) {
            val emptyView = TextView(this)
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT // 이전 코드대로 WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            // params.setMargins(4,4,4,4) // 이전 코드에는 명시적 margins 없음
            emptyView.layoutParams = params
            gridCalendar.addView(emptyView)
        }

        // 날짜 채우기
        for (day in 1..lastDayOfMonth) {
            val dayCellCalendar = displayCalendar.clone() as Calendar
            dayCellCalendar.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayCellCalendar.time)

            val tvDay = TextView(this)
            tvDay.text = day.toString()
            tvDay.gravity = Gravity.CENTER
            tvDay.setPadding(8, 16, 8, 16) // 이전 코드와 동일한 패딩
            // tvDay.textSize = 16f // 이전 코드에는 명시적 textSize 없음

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            // params.setMargins(4,4,4,4) // 이전 코드에는 명시적 margins 없음
            tvDay.layoutParams = params

            tvDay.setOnClickListener {
                if (dayCellCalendar.after(today) && !isSameDay(dayCellCalendar, today)) {
                    Toast.makeText(this, "미래 날짜의 기분은 기록할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    selectedDateCalendar = dayCellCalendar.clone() as Calendar
                    updateSelectedMoodInfo(selectedDateCalendar!!)
                    drawCalendar() // 선택 변경 시 달력 전체 다시 그리기
                }
            }

            val moodColor = loadedMoods[dateKey]
            // 이전 버전의 setTodayHighlightOrSelection 호출
            setTodayHighlightOrSelection(tvDay, dayCellCalendar, moodColor)
            gridCalendar.addView(tvDay)
        }

        // 달력의 나머지 빈 칸 채우기 (총 6주 * 7일 = 42칸 기준)
        val totalCells = 42 // 일반적인 6주 달력
        // 현재까지 채워진 셀 수: 요일 헤더(7) + 시작 전 빈칸(firstDayOfWeek-1) + 실제 날짜(lastDayOfMonth)
        val currentCellCount = daysOfWeekArray.size + (firstDayOfWeek - 1) + lastDayOfMonth
        
        for (i in 0 until (totalCells - currentCellCount)) {
            val emptyView = TextView(this)
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            // params.setMargins(4,4,4,4) // 이전 코드에는 명시적 margins 없음
            emptyView.layoutParams = params
            gridCalendar.addView(emptyView)
        }
    }
}