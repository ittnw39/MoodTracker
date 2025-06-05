# 🌖MoodTracker🌒 애플리케이션 발표 자료

## 1. 프로젝트 소개

### 개발 목적
일상의 기분을 간편하게 기록하고, 기록된 데이터를 시각적으로 분석하여 사용자가 자신의 감정 변화 패턴을 이해하고 관리하는 데 도움을 주기 위해 개발되었습니다.

### 앱의 주요 특징
- **직관적인 달력 UI**: 월별로 기분을 한눈에 파악할 수 있는 색상 기반 달력 제공.
- **간편한 기분 기록**: 몇 번의 터치로 쉽고 빠르게 오늘의 기분 선택 및 저장.
- **사용자 정의 감정**: 기본 제공 감정 외에 사용자만의 감정을 직접 추가, 수정, 삭제하여 개인 맞춤형 기록 가능.
- **상세한 기분 통계**: 기간별(월별, 사용자 지정, 최근 N일 등) 기분 데이터를 파이 차트로 시각화하여 제공.
- **데이터 영속성**: 기록된 기분 및 사용자 정의 감정은 앱 내부에 안전하게 저장.

---

## 2. 주요 기능 및 핵심 코드

###  기능 1: 월별 기분 달력

#### 설명
- 현재 월의 달력을 표시하며, 각 날짜에 기록된 기분을 해당 감정의 색상으로 표시합니다.
- '이전 달', '다음 달' 버튼으로 쉽게 월을 탐색할 수 있습니다.
- 오늘 날짜는 특별한 스타일(예: 파란색 텍스트, 연노랑 배경)로 강조되며, 사용자가 선택한 날짜는 테두리로 표시됩니다.
- 사용자는 날짜를 클릭하여 해당 날짜의 기분 정보를 확인하거나, 기분 기록/수정 다이얼로그를 호출할 수 있습니다. (미래 날짜는 기록 불가)

#### 화면 예시
(월별 달력 화면 GIF 또는 스크린샷 삽입 위치)

#### 핵심 코드 (`MainActivity.kt`)

**달력 UI 동적 생성 (`drawCalendar` 함수 일부)**
```kotlin
// ... (생략) ...
private fun drawCalendar() {
    gridCalendar.removeAllViews()
    val loadedMoods = moodsMap // 현재 로드된 기분 데이터

    val displayCalendar = currentCalendar.clone() as Calendar
    displayCalendar.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = displayCalendar.get(Calendar.DAY_OF_WEEK)
    val lastDayOfMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    // ... (요일 이름 표시 로직 생략) ...

    // 날짜 채우기
    for (day in 1..lastDayOfMonth) {
        val dayCellCalendar = displayCalendar.clone() as Calendar
        dayCellCalendar.set(Calendar.DAY_OF_MONTH, day)
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayCellCalendar.time)

        val tvDay = TextView(this)
        tvDay.text = day.toString()
        // ... (스타일 설정) ...

        tvDay.setOnClickListener {
            // 미래 날짜 클릭 방지 및 날짜 선택 로직
            val today = Calendar.getInstance()
            if (dayCellCalendar.after(today) && !isSameDay(dayCellCalendar, today)) {
                Toast.makeText(this, "미래 날짜의 기분은 기록할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                selectedDateCalendar = dayCellCalendar.clone() as Calendar
                updateSelectedMoodInfo(selectedDateCalendar!!) // 선택된 날짜 정보 업데이트
                drawCalendar() // 선택 시 달력 다시 그려서 테두리 등 반영
            }
        }

        val moodColor = loadedMoods[dateKey] // 해당 날짜의 기분 색상 가져오기
        setTodayHighlightOrSelection(tvDay, dayCellCalendar, moodColor) // 강조 및 배경색 설정
        gridCalendar.addView(tvDay)
    }
    // ... (나머지 빈 칸 채우기 로직 생략) ...
}
```
- `currentCalendar`를 기준으로 해당 월의 날짜들을 `TextView`로 동적 생성하여 `GridLayout`에 추가합니다.
- 각 날짜 `TextView`에 클릭 리스너를 설정하여 날짜 선택 기능을 구현하고, `selectedDateCalendar`를 업데이트합니다.
- `moodsMap`에서 해당 날짜의 기분 데이터를 가져와 `setTodayHighlightOrSelection` 함수를 통해 배경색 및 강조 효과를 적용합니다.

**오늘 날짜 및 선택 날짜 강조 (`setTodayHighlightOrSelection` 함수 일부)**
```kotlin
private fun setTodayHighlightOrSelection(textView: TextView, calendarForDay: Calendar, moodColorHex: String?) {
    val today = Calendar.getInstance()
    val finalDrawable = GradientDrawable()
    finalDrawable.shape = GradientDrawable.RECTANGLE
    textView.setTextColor(Color.BLACK) // 기본 텍스트 색상

    var currentBgColor: Int
    if (moodColorHex != null) { // 기분 기록이 있는 경우
        currentBgColor = Color.parseColor(moodColorHex)
    } else { // 기분 기록이 없는 경우
        if (isSameDay(calendarForDay, today) &&
            (selectedDateCalendar == null || !isSameDay(calendarForDay, selectedDateCalendar!!))) {
            currentBgColor = Color.parseColor("#FFFFE0") // 오늘 날짜 기본 배경
        } else {
            currentBgColor = Color.TRANSPARENT
        }
    }
    finalDrawable.setColor(currentBgColor)

    // 선택된 날짜 테두리 적용
    if (selectedDateCalendar != null && isSameDay(calendarForDay, selectedDateCalendar!!)) {
        finalDrawable.setStroke(3, Color.BLACK) // 두꺼운 검은색 테두리
    }

    // 오늘 날짜 텍스트 색상 적용
    if (isSameDay(calendarForDay, today)) {
        textView.setTextColor(Color.BLUE)
    }
    textView.background = finalDrawable
}
```
- `GradientDrawable`을 사용하여 날짜 셀의 배경 및 테두리를 설정합니다.
- 기록된 기분이 있으면 해당 기분 색상을 배경으로, 없으면 투명 또는 오늘 날짜 특별 배경색을 적용합니다.
- `selectedDateCalendar`와 현재 셀의 날짜가 동일하면 검은색 테두리를 추가합니다.
- 오늘 날짜는 텍스트 색상을 파란색으로 변경합니다.

---

### 기능 2: 기분 기록 및 사용자 정의 감정 관리

#### 설명
- 사용자는 달력에서 날짜를 선택 후 '기분 기록하기' 버튼을 통해 해당 날짜의 기분을 기록할 수 있습니다.
- 기분 선택 다이얼로그에는 기본 감정(행복, 좋음, 보통, 나쁨, 슬픔)과 사용자가 추가한 감정이 함께 표시됩니다.
- '감정 관리' 메뉴를 통해 사용자 정의 감정을 추가, 기존 감정의 이름이나 색상을 수정, 또는 삭제할 수 있습니다.
    - 감정 추가/수정 시 이름과 색상(HEX 코드 직접 입력 또는 색상 선택기 사용)을 지정합니다.
    - 감정 수정 시, 해당 감정으로 이미 기록된 내역의 색상도 함께 업데이트됩니다.
    - 감정 삭제 시, 해당 감정으로 기록된 내역도 함께 삭제됩니다.

#### 화면 예시
(기분 기록 다이얼로그, 감정 관리 화면, 감정 추가/수정 다이얼로그 GIF 또는 스크린샷 삽입 위치)

#### 핵심 코드

**기분 선택 다이얼로그 표시 (`MainActivity.kt` - `showMoodLogDialog` 일부)**
```kotlin
private fun showMoodLogDialog(dateToLog: Calendar) {
    val titleDate = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault()).format(dateToLog.time)
    val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateToLog.time)

    val displayableMoods = getAllDisplayableMoods() // 기본 + 사용자 정의 감정 모두 가져오기
    val moodNamesForDialog = displayableMoods.map { it.first }.toTypedArray()

    val existingMoodColor = moodsMap[dateKey]
    var currentSelectedPosition = displayableMoods.indexOfFirst { it.second == existingMoodColor }.takeIf { it != -1 } ?: 0

    val adapter = MoodListAdapter(this, displayableMoods.associate { it.first to it.second }, moodNamesForDialog, currentSelectedPosition)

    val builder = AlertDialog.Builder(this)
    builder.setTitle("${titleDate} 기분 선택")
    builder.setSingleChoiceItems(adapter, currentSelectedPosition) { _, which ->
        adapter.setSelectedPosition(which)
    }
    builder.setPositiveButton("확인") { _, _ ->
        val selectedPosition = adapter.getSelectedPosition()
        // ... (유효성 검사) ...
        val selectedMoodColor = displayableMoods[selectedPosition].second
        saveMoodToFile(dateKey, selectedMoodColor) // 선택된 기분 저장
        moodsMap = loadMoodsFromFile() // 기분 맵 다시 로드
        updateSelectedMoodInfo(dateToLog)
        drawCalendar()
        // ... (Toast 메시지) ...
    }
    // ... (취소, 삭제 버튼 설정) ...
    builder.show()
}
```
- `getAllDisplayableMoods()`를 통해 기본 감정과 사용자 정의 감정을 합쳐 목록을 만듭니다.
- `MoodListAdapter` (커스텀 어댑터)를 사용하여 각 감정 항목에 이름과 색상 원을 표시합니다.
- '확인' 버튼 클릭 시 선택된 감정의 색상 코드를 `saveMoodToFile` 함수를 통해 저장합니다.

**기분 데이터 저장/삭제 (`MainActivity.kt`)**
```kotlin
private fun saveMoodToFile(date: String, moodColor: String) {
    try {
        val currentMoods = moodsMap.toMutableMap()
        currentMoods[date] = moodColor // 해당 날짜에 기분 색상 맵핑
        
        val fileContents = StringBuilder()
        currentMoods.forEach { (d, mc) -> // 전체 기분 데이터를 문자열로 변환
            fileContents.append("$d:$mc\n")
        }
        // 내부 저장소 파일에 쓰기
        openFileOutput(moodFileName, Context.MODE_PRIVATE).use {
            it.write(fileContents.toString().toByteArray())
        }
    } catch (e: Exception) { /* ... (오류 처리) ... */ }
}

private fun deleteMoodFromFile(date: String) {
    try {
        val currentMoods = moodsMap.toMutableMap()
        if (currentMoods.containsKey(date)) {
            currentMoods.remove(date) // 해당 날짜의 기분 데이터 삭제
            // ... (이하 saveMoodToFile과 유사하게 파일 다시 쓰기) ...
        }
    } catch (e: Exception) { /* ... (오류 처리) ... */ }
}
```
- `mood_data.txt` 파일에 "날짜:색상코드" 형식으로 기분 데이터를 저장하고 관리합니다.
- 저장 시에는 전체 `moodsMap`을 파일에 덮어쓰는 방식입니다.

**사용자 정의 감정 추가/수정 (`ManageMoodsActivity.kt` - `showAddMoodDialog` 일부)**
```kotlin
private fun showAddMoodDialog(customMoodToEdit: CustomMood? = null) {
    // ... (다이얼로그 UI 설정 생략) ...
    val etMoodName = dialogView.findViewById<EditText>(R.id.etMoodName)
    val etMoodColorLocal = dialogView.findViewById<EditText>(R.id.etMoodColor) // Hex 색상 코드 입력 필드
    val btnSelectColor = dialogView.findViewById<Button>(R.id.btnSelectColor) // 색상 선택기 버튼
    // ...
    btnSelectColor.setOnClickListener {
        // ColorPickerDialog 라이브러리를 사용하여 색상 선택기 호출
        ColorPickerDialog.newBuilder()
            .setDialogId(DIALOG_ID_ADD_MOOD) // 콜백 구분을 위한 ID
            .setColor(initialColor) // EditText의 현재 색상 또는 기본 색상
            .show(this) // 'this'는 ColorPickerDialogListener를 구현한 Activity
    }

    builder.setPositiveButton("저장") { _, _ ->
        val moodName = etMoodName.text.toString().trim()
        val moodColor = etMoodColorLocal.text.toString().trim()
        // ... (입력값 유효성 검사) ...

        if (customMoodToEdit != null) { // 수정 모드
            val updatedMood = CustomMood(id = customMoodToEdit.id, name = moodName, colorHex = moodColor)
            val success = customMoodRepository.updateCustomMood(updatedMood)
            if (success) {
                updateMoodDataFile(oldColorHex, moodColor) // 기존 기록된 기분 색상 업데이트
                // ...
            }
        } else { // 추가 모드
            customMoodRepository.addCustomMood(moodName, moodColor)
            // ...
        }
        loadAndDisplayMoods() // 목록 새로고침
    }
    // ...
}

// ColorPickerDialogListener 인터페이스 구현
override fun onColorSelected(dialogId: Int, color: Int) {
    if (dialogId == DIALOG_ID_ADD_MOOD) {
        val hexColor = String.format("#%06X", (0xFFFFFF and color))
        currentEtMoodColor?.setText(hexColor) // 선택된 색상을 EditText에 반영
    }
}
```
- 감정 추가/수정 시 `AlertDialog`와 커스텀 레이아웃(`dialog_add_mood.xml`)을 사용합니다.
- `jaredrummler:colorpicker` 라이브러리를 연동하여 사용자 친화적인 색상 선택 기능을 제공합니다.
- `CustomMoodRepository`를 통해 사용자 정의 감정 데이터를 JSON 파일로 저장/관리합니다.
- 감정 수정/삭제 시 `mood_data.txt`에 기록된 해당 감정의 색상 코드도 동기화하여 업데이트/삭제 처리합니다. (`updateMoodDataFile`, `deleteMoodDataFromFile` 함수)

**사용자 정의 감정 데이터 관리 (`CustomMoodRepository.kt`)**
```kotlin
class CustomMoodRepository(private val context: Context) {
    private val customMoodsFilename = "custom_moods.json"
    private val gson = Gson() // JSON 직렬화/역직렬화를 위한 Gson 라이브러리 사용

    fun loadCustomMoods(): MutableList<CustomMood> {
        // ... (파일에서 JSON 읽어와 CustomMood 리스트로 변환) ...
        try {
            val jsonString = file.readText()
            val type = object : TypeToken<MutableList<CustomMood>>() {}.type
            return gson.fromJson(jsonString, type) ?: mutableListOf()
        } // ... (예외 처리) ...
    }

    fun saveCustomMoods(moods: List<CustomMood>) {
        // ... (CustomMood 리스트를 JSON으로 변환하여 파일에 저장) ...
        val jsonString = gson.toJson(moods)
        context.openFileOutput(customMoodsFilename, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        } // ... (예외 처리) ...
    }

    fun addCustomMood(name: String, colorHex: String): CustomMood {
        val moods = loadCustomMoods()
        val newMood = CustomMood(id = UUID.randomUUID().toString(), name = name, colorHex = colorHex)
        moods.add(newMood)
        saveCustomMoods(moods)
        return newMood
    }
    // ... (updateCustomMood, deleteCustomMood 함수 구현) ...
}
```
- `CustomMood` 데이터 클래스(id, name, colorHex)를 정의하여 사용합니다.
- `Gson` 라이브러리를 사용하여 `CustomMood` 객체 리스트를 JSON 형태로 직렬화하여 `custom_moods.json` 파일에 저장하고, 필요시 역직렬화하여 로드합니다.

---

### 기능 3: 기분 통계

#### 설명
- 기록된 기분 데이터를 바탕으로 기간별 통계를 파이 차트 형태로 제공합니다.
- 사용자는 월별 이동, 특정 기간 직접 선택(시작일/종료일), 또는 사전 정의된 기간(최근 7일, 최근 30일, 이번 주) 선택을 통해 통계 범위를 지정할 수 있습니다.
- 파이 차트에는 각 감정이 해당 기간 동안 기록된 횟수에 따라 비율로 표시되며, 감정의 이름과 백분율이 함께 나타납니다.
- 기본 감정과 사용자 정의 감정이 모두 통계에 포함되어 분석됩니다.

#### 화면 예시
(통계 화면 - 파이 차트 및 기간 선택 UI GIF 또는 스크린샷 삽입 위치)

#### 핵심 코드 (`StatsActivity.kt`)

**통계 데이터 로드 및 파이 차트 업데이트 (`loadPieChartData` 함수 일부)**
```kotlin
private fun loadPieChartData() {
    val allMoodsData = loadMoodsFromFile() // MainActivity의 mood_data.txt 로드
    val allDisplayableMoods = getAllDisplayableMoods() // 기본 + 사용자 정의 감정 정보

    val moodCounts = mutableMapOf<String, Int>() // 색상 코드별 빈도수
    val moodColors = mutableMapOf<String, Int>() // 색상 코드별 실제 Color 값
    val moodNamesMap = mutableMapOf<String, String>() // 색상 코드별 감정 이름

    allDisplayableMoods.forEach { (name, colorHex) ->
        try {
            moodNamesMap[colorHex] = name // 색상 코드에 이름 맵핑
            moodColors[colorHex] = Color.parseColor(colorHex) // 색상 코드에 Color 값 맵핑
        } catch (e: IllegalArgumentException) { /* ... */ }
    }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    for ((dateString, colorHex) in allMoodsData) {
        try {
            val recordDate = Calendar.getInstance().apply { time = sdf.parse(dateString)!! }
            
            // 기간 필터링 로직
            val isInSelectedRange = when {
                selectedStartDate != null && selectedEndDate != null -> // 직접 기간 선택
                    !recordDate.before(selectedStartDate) && !recordDate.after(selectedEndDate)
                else -> // 월별 보기 (selectedStartDate/EndDate가 null일 때)
                    recordDate.get(Calendar.YEAR) == currentStatsCalendar.get(Calendar.YEAR) &&
                    recordDate.get(Calendar.MONTH) == currentStatsCalendar.get(Calendar.MONTH)
            }

            if (isInSelectedRange && moodNamesMap.containsKey(colorHex)) { // 기간 내 & 유효한 감정
                moodCounts[colorHex] = moodCounts.getOrDefault(colorHex, 0) + 1
            }
        } catch (e: Exception) { /* ... (날짜 파싱 오류 등) ... */ }
    }

    val entries = ArrayList<PieEntry>()
    val colors = ArrayList<Int>()

    for ((colorHex, count) in moodCounts) {
        if (count > 0) {
            entries.add(PieEntry(count.toFloat(), moodNamesMap[colorHex] ?: "알 수 없음"))
            colors.add(moodColors[colorHex] ?: Color.GRAY)
        }
    }

    if (entries.isEmpty()) {
        // ... (데이터 없음 처리) ...
        pieChart.clear()
        return
    }

    val dataSet = PieDataSet(entries, "") // 라벨 제거
    dataSet.colors = colors
    // ... (dataSet 스타일 설정) ...

    val data = PieData(dataSet)
    // ... (data 스타일 설정) ...
    pieChart.data = data
    pieChart.invalidate() // 차트 새로고침
}
```
- `mood_data.txt`에서 모든 기분 기록을 로드합니다.
- `getAllDisplayableMoods()`로 기본 및 사용자 정의 감정 목록(이름, 색상)을 가져옵니다.
- 선택된 기간(월별, 직접 지정, 사전 정의)에 따라 기분 기록을 필터링합니다.
- 필터링된 데이터를 바탕으로 각 감정(색상 코드 기준)의 빈도수를 계산합니다.
- `MPAndroidChart` 라이브러리의 `PieEntry`를 생성하여 파이 차트 데이터를 구성하고, 각 조각의 색상을 설정합니다.
- 데이터가 없을 경우 차트를 비우고, 데이터가 있으면 차트를 업데이트합니다.

**기간 선택 로직 (Spinner 및 DatePickerDialog 연동)**
```kotlin
// Spinner 아이템 선택 리스너 (onCreate 내)
spinnerPredefinedRanges.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        // ... (선택된 항목에 따라 selectedStartDate, selectedEndDate 설정) ...
        // 예: "최근 7일"
        // selectedEndDate = today.clone() as Calendar
        // selectedStartDate = today.clone() as Calendar
        // selectedStartDate!!.add(Calendar.DAY_OF_YEAR, -6)
        updateDateRangeText()
        loadPieChartData()
    }
    // ...
}

// 날짜 선택 버튼 클릭 리스너 (onCreate 내)
btnStartDate.setOnClickListener {
    showDatePickerDialog(isStartDate = true)
    spinnerPredefinedRanges.setSelection(0, false) // Spinner 선택 해제
}

// DatePickerDialog 표시 함수
private fun showDatePickerDialog(isStartDate: Boolean) {
    val calendar = if (isStartDate && selectedStartDate != null) selectedStartDate!!
                   else if (!isStartDate && selectedEndDate != null) selectedEndDate!!
                   else Calendar.getInstance()
    
    DatePickerDialog(this, { _, year, month, dayOfMonth ->
        val selectedCal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        if (isStartDate) {
            selectedStartDate = selectedCal
            // 시작일이 종료일보다 늦으면 종료일도 시작일로 설정 (또는 오류 처리)
            if (selectedEndDate != null && selectedStartDate!!.after(selectedEndDate!!)) {
                selectedEndDate = selectedStartDate!!.clone() as Calendar
            }
        } else {
            selectedEndDate = selectedCal
            // 종료일이 시작일보다 이르면 시작일도 종료일로 설정 (또는 오류 처리)
             if (selectedStartDate != null && selectedEndDate!!.before(selectedStartDate!!)) {
                selectedStartDate = selectedEndDate!!.clone() as Calendar
            }
        }
        updateDateRangeText()
        loadPieChartData()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}
```
- `Spinner`를 사용하여 "최근 7일", "최근 30일", "이번 주"와 같은 사전 정의된 기간 선택 옵션을 제공합니다.
- `DatePickerDialog`를 사용하여 사용자가 직접 시작 날짜와 종료 날짜를 선택할 수 있게 합니다.
- 날짜/기간이 변경될 때마다 `selectedStartDate`, `selectedEndDate`를 업데이트하고 `loadPieChartData()`를 호출하여 통계를 갱신합니다.

---

## 3. 개발 환경

- **언어**: Kotlin
- **최소 API 레벨**: API 21 (Android 5.0 Lollipop)
- **아키텍처**: 기본적인 MVC 패턴 지향 (Activity가 Controller 역할, XML이 View, 데이터 파일 및 Repository가 Model 역할 일부 수행)
- **주요 라이브러리**:
    - `com.github.PhilJay:MPAndroidChart` (통계 파이 차트 시각화)
    - `com.jaredrummler:colorpicker` (사용자 정의 감정 색상 선택기)
    - `com.google.code.gson:gson` (사용자 정의 감정 데이터 JSON 직렬화/역직렬화)
- **데이터 저장**:
    - 일일 기분 기록: 내부 저장소 텍스트 파일 (`mood_data.txt`)
    - 사용자 정의 감정: 내부 저장소 JSON 파일 (`custom_moods.json`)

---

## 4. 결론 및 향후 개선 방향 (선택 사항)

### 결론
MoodTracker 앱은 사용자가 간편하게 자신의 기분을 기록하고, 시각적인 통계를 통해 감정 패턴을 파악할 수 있도록 지원합니다. 사용자 정의 기능을 통해 개인에게 더욱 최적화된 경험을 제공하고자 했습니다.

### 향후 개선 방향
- 데이터 백업 및 복원 기능 (Google Drive 연동 등)
- 다양한 통계 분석 기능 추가 (예: 특정 기간 감정 변화 추이 라인 그래프)
- 알림 기능 (일일 기분 기록 독려)
- 태그 또는 메모 기능 추가 (기분 기록 시 상세 내용 작성)

---

이 Markdown 문서를 PPT 제작의 기초 자료로 활용하시면 됩니다. 각 "화면 예시" 부분에 실제 앱 화면 GIF나 스크린샷을 추가하고, 코드 설명 부분을 발표 흐름에 맞게 조절하여 사용하세요.