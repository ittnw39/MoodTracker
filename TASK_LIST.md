## MoodTracker 앱 개발 작업 목록

### Task 1: 프로젝트 설정 및 메인 화면 UI 설계 (완료)

주요 목표: 앱 개발 환경 설정 및 사용자가 상호작용할 기본 화면 구성.
세부 작업:
- [x] Android Studio 프로젝트 생성:
    - 새로운 Android Studio 프로젝트를 'MoodCalendar' 이름으로 생성.
    - 적용 내용: Project4_1 생성 과정 과 같이 프로젝트 이름, 패키지 이름, Kotlin 언어, 최소 API 레벨 (예: API 16: Android 4.1)을 설정.
- [x] `activity_main.xml` 레이아웃 설계:
    - 최상위 레이아웃으로 LinearLayout (수직 방향)을 사용.
    - 상단에 LinearLayout (수평 방향)을 추가하여 월 이동 UI를 구성.
        - Button 1개 ('이전 달', ID: btnPrevMonth)
        - TextView 1개 (현재 '년/월' 표시, ID: tvCurrentMonth)
        - Button 1개 ('다음 달', ID: btnNextMonth)
    - 중단에 GridLayout을 추가하여 달력 본체를 구성 (7열, ID: gridCalendar).
    - 하단에 TextView 1개 (선택된 날짜의 기분 정보 표시, ID: tvSelectedMoodInfo) 추가.
    - 하단에 Button 1개를 추가하여 기분 기록 기능을 실행 ('기분 기록하기', ID: btnLogMood).
    - 적용 내용: LinearLayout의 orientation 속성, GridLayout의 columnCount 속성 을 활용하여 UI 뼈대를 만들기. 각 위젯에 id를 부여.
- [x] `MainActivity.kt` 위젯 참조:
    - `MainActivity.kt`에서 findViewById를 사용하여 `activity_main.xml`에 배치된 위젯들의 참조 변수를 선언하고 연결.
    - 적용 내용: Kotlin 변수 선언, findViewById를 통한 위젯 접근.

### Task 2: 달력 UI 동적 생성 로직 구현 (`MainActivity.kt`) (완료)

주요 목표: 현재 날짜를 기준으로 GridLayout에 날짜를 표시하고, 월 이동 기능 구현.
세부 작업:
- [x] 날짜 계산 로직:
    - Kotlin 코드에서 Calendar 클래스를 사용하여 현재 연도, 월, 해당 월의 시작 요일, 마지막 날짜 등을 계산하는 로직을 구현.
    - 적용 내용: Kotlin 기본 문법 (변수, 조건문, 연산)  및 Calendar 클래스를 활용.
- [x] `drawCalendar` 함수 구현 (`MainActivity.kt`):
    - GridLayout의 모든 자식 뷰를 제거.
    - 계산된 날짜 정보를 바탕으로, for 반복문을 사용하여 날짜 수만큼 TextView 위젯을 동적으로 생성.
    - 생성된 TextView에 날짜 텍스트를 설정하고 GridLayout에 추가.
    - 적용 내용: Kotlin 코드로 뷰 생성 및 GridLayout 활용.
- [x] 월 이동 버튼 이벤트 처리 (`MainActivity.kt`):
    - '이전 달', '다음 달' Button에 setOnClickListener를 설정.
    - 버튼 클릭 시 현재 표시된 월/년을 변경하고 `tvCurrentMonth` 텍스트를 업데이트한 후, `drawCalendar` 함수를 다시 호출하여 달력을 새로 그림.
    - 적용 내용: 버튼 이벤트 처리, 람다식.

### Task 3: 기분 선택 다이얼로그 구현 (`MainActivity.kt`) (완료)

주요 목표: '기분 기록하기' 버튼 클릭 시, 기분을 선택할 수 있는 대화상자 표시.
세부 작업:
- [x] 기분 데이터 정의:
    - 앱에서 사용할 기분 목록과 각 기분에 해당하는 색상 코드(Hex)를 Kotlin 코드 내에 배열 또는 Map으로 정의. (예: val moods = mapOf("행복" to "#FFFF00", "슬픔" to "#0000FF"))
    - 적용 내용: Kotlin 배열  또는 컬렉션.
- [x] '기분 기록하기' 버튼 이벤트 처리 (`MainActivity.kt`):
    - `btnLogMood` Button에 setOnClickListener를 설정.
- [x] 대화상자 생성 (`MainActivity.kt`):
    - 버튼 클릭 리스너 내에서 AlertDialog.Builder를 사용하여 대화상자를 생성.
    - 대화상자 제목 (setTitle), 기분 목록 (setItems 또는 setSingleChoiceItems)을 설정. setSingleChoiceItems를 사용하여 RadioButton 형태로 표시.
    - '확인'(setPositiveButton)과 '취소'(setNegativeButton) 버튼을 추가.
    - 적용 내용: 기본 대화상자 및 목록 대화상자 생성 방법 활용.
- [x] '확인' 버튼 로직 (`MainActivity.kt`):
    - '확인' 버튼 클릭 시, 선택된 기분(색상)과 현재 날짜(또는 달력에서 선택된 날짜)를 가져와 `saveMood` 함수를 호출.
    - Toast를 사용하여 "기분이 저장되었습니다."와 같은 피드백 메시지를 표시.
    - `drawCalendar` 함수를 호출하여 달력을 갱신.
    - 적용 내용: 대화상자 버튼 리스너, Toast 사용법.

### Task 4: 기분 데이터 저장 기능 구현 (`MainActivity.kt`) (완료)

주요 목표: 사용자가 선택한 기분 데이터를 내부 저장소에 영구적으로 저장.
세부 작업:
- [x] `saveMood` 함수 구현 (`MainActivity.kt`):
    - 날짜와 기분(색상) 데이터를 파라미터로 받기.
    - openFileOutput을 사용하여 `mood_data.txt`와 같은 이름의 파일을 열기. (날짜별 덮어쓰기 방식)
    - FileOutputStream을 사용하여 YYYY-MM-DD:ColorHexCode 형식의 문자열을 파일에 쓰기.
    - 파일 쓰기 후 스트림 닫기.
    - 적용 내용: 내장 메모리 파일 쓰기, '간단 일기장' 앱의 파일 저장 방식을 참조.
- [x] 예외 처리 (`MainActivity.kt`):
    - 파일 입출력 시 발생할 수 있는 오류를 처리하기 위해 try~catch 문을 사용.

### Task 5: 기분 데이터 로드 및 달력 색상 적용 (`MainActivity.kt`) (완료)

주요 목표: 저장된 기분 데이터를 불러와 달력 UI에 색상으로 반영.
세부 작업:
- [x] `loadMoods` 함수 구현 (`MainActivity.kt`):
    - openFileInput을 사용하여 `mood_data.txt` 파일을 열기.
    - FileInputStream과 BufferedReader 등을 사용하여 파일 내용을 줄 단위로 읽기.
    - 읽어온 각 줄을 파싱하여 날짜를 키로, 색상 코드를 값으로 하는 Map<String, String> 형태의 데이터 구조에 저장.
    - 파일이 없을 경우를 대비한 예외 처리를 하기.
    - 적용 내용: 내장 메모리 파일 읽기, '간단 일기장' 앱의 파일 읽기 방식을 참조.
- [x] `drawCalendar` 함수 수정 (`MainActivity.kt`):
    - `drawCalendar` 함수 시작 시 `loadMoods` 함수를 호출하여 기분 데이터를 로드.
    - GridLayout에 날짜 TextView를 추가할 때, 해당 날짜가 로드된 Map에 있는지 확인.
    - 데이터가 존재하면, 해당 TextView의 setBackgroundColor(Color.parseColor(colorHexCode)) 메소드를 호출하여 배경색을 설정.
    - 적용 내용: Kotlin Map 사용, 뷰의 속성 변경.

### Task 6: UI 개선 및 기능 테스트 (완료)

주요 목표: 앱의 시각적 완성도를 높이고, 핵심 기능의 오류를 점검.
세부 작업:
- [x] UI 디테일 조정:
    - [x] 달력의 날짜 클릭 기능 구현 (`MainActivity.kt`):
        - 과거 및 오늘 날짜 클릭 시 해당 날짜를 선택된 날짜(`selectedDateCalendar`)로 설정.
        - 미래 날짜 클릭 시 Toast 메시지로 알림.
        - 선택된 날짜의 기분 정보를 하단 `tvSelectedMoodInfo` TextView에 표시 (기록된 기분 또는 "기록 없음").
        - 기분 기록 다이얼로그에 선택된 날짜가 반영되고, 해당 날짜에 기분이 이미 기록되어 있으면 기본 선택값으로 표시.
    - [x] 기분 선택 다이얼로그 아이템에 색상 원 표시 (`dialog_mood_item.xml`, `MoodListAdapter.kt`):
        - 커스텀 레이아웃(`dialog_mood_item.xml`) 및 커스텀 어댑터(`MoodListAdapter.kt`)를 사용하여 각 기분 항목 옆에 해당 색상을 원으로 표시.
    - [x] 오늘 날짜 강조 및 선택된 날짜 표시 로직 개선 (`MainActivity.kt`):
        - 오늘 날짜 강조(파란색 텍스트, 밝은 노란색 배경)는 현재 달력 화면이 실제 오늘 날짜가 포함된 달일 경우, 해당 일자에만 적용.
        - 사용자가 클릭하여 선택한 날짜는 검은색 테두리(예: 2dp)로 표시.
        - 기분 색상이 있는 경우, 해당 배경색 위에 테두리가 그려짐.
        - 월 이동 시 선택된 날짜의 테두리 표시는 유지되지만, 다른 달의 동일 '일'에는 적용되지 않음.
    - [x] `MainActivity.kt` 달력 UI 이전 스타일로 복원 (요일 표시, 날짜 칸 높이 및 패딩, 전체적인 그리드 레이아웃, `setTodayHighlightOrSelection` 함수 로직 복원)
    - [ ] `activity_main.xml` GridLayout 내 TextView들의 padding, margin, gravity 속성을 조정하여 달력 형태를 보기 좋게 만들기 (선택)
    - 적용 내용: View 클래스의 XML 속성, Paint와 Canvas를 이용한 간단한 도형 그리기 또는 단순히 View의 배경색 설정, `GradientDrawable` 사용.
- [x] 기능 테스트:
    - [x] 월 이동이 정확히 동작하는지 확인.
    - [x] 기분 저장 및 로드가 정상적으로 이루어지는지 확인.
    - [x] 다양한 날짜에 기분을 기록하고 달력에 올바르게 표시되는지 확인.
    - [x] 기존 기분을 수정(덮어쓰기)하는 시나리오를 테스트.
    - [x] 날짜 클릭 및 선택된 날짜 관련 기능(테두리, 오늘 날짜 강조, 정보 표시)이 정확히 동작하는지 테스트.

### Task 7: 통계 기능 구현 (완료)

주요 목표: 기록된 기분 데이터를 바탕으로 월별/기간별 통계 그래프를 제공하여 사용자가 자신의 감정 패턴을 시각적으로 분석할 수 있도록 함.
세부 작업:
- [x] 통계 화면 UI 설계 (`activity_stats.xml`) - 기본 PieChart 및 Toolbar 추가 완료.
- [x] 기간 선택 UI 구체화 (`activity_stats.xml`): 월 이동 UI (이전/다음 버튼, 현재 연/월 표시) 추가 완료.
- [x] 기분 데이터 로드 및 분석 로직 구현 (`StatsActivity.kt`): 선택된 월 데이터 필터링 및 기분별 빈도수 계산 완료.
- [x] 그래프 라이브러리 선정 및 연동 (MPAndroidChart) - 의존성 추가 및 기본 설정 완료.
- [x] 계산된 통계 데이터를 그래프로 시각화하여 표시 (`StatsActivity.kt`): PieChart 연동 및 월 변경 시 차트 업데이트 완료.
- [x] 통계 기능 (`StatsActivity.kt`)에 사용자 정의 감정 반영 및 삭제된 감정 제외 처리
적용 내용: UI 디자인, 데이터 분석 알고리즘, 외부 라이브러리 활용, 데이터 시각화.

### Task 8: 사용자 정의 감정 관리 기능 (신규)

주요 목표: 사용자가 자신만의 감정을 추가, 수정, 삭제할 수 있도록 하여 개인화된 기분 기록 경험을 제공 (기본 감정은 수정/삭제 불가).
세부 작업:
- [x] 사용자 정의 감정 추가/수정/삭제 UI 및 기본 로직 구현 (`ManageMoodsActivity.kt`, `activity_manage_moods.xml`)
- [x] 색상 선택기 라이브러리 변경 (`com.jaredrummler:colorpicker`) 및 적용 (`ManageMoodsActivity.kt`)
- [x] 감정 수정 시 `mood_data.txt`의 기존 기록된 색상 동기화 (`ManageMoodsActivity.kt`)
- [x] 감정 삭제 시 `mood_data.txt`의 기존 기록 삭제 동기화 (`ManageMoodsActivity.kt`)
- [x] 버그 수정: `ManageMoodsActivity.kt`에서 색상 코드 입력 없이 색상 선택기 버튼 클릭 시 발생하는 `StringIndexOutOfBoundsException` 해결
- [x] 기능 개선: `ManageMoodsActivity.kt`에서 감정 수정/삭제 후 `MainActivity.kt` 달력에 즉시 반영 (onResume 로직 추가)
- [x] 최적화: `MainActivity.kt`에서 `mood_data.txt` 파일 중복 로드 방지를 위한 `moodsMap` 멤버 변수 사용
- [x] 감정 데이터 저장 구조 변경 검토 및 설계 (`CustomMoodRepository.kt`).
- [x] 감정 관리 UI/UX 설계 (추가/수정/삭제 화면 또는 다이얼로그 - `dialog_add_mood.xml`, `item_custom_mood.xml`, `item_default_mood.xml`, `ManageMoodsAdapter.kt`).
- [x] 감정 추가/수정/삭제 CRUD 로직 구현 (`CustomMoodRepository.kt`, `ManageMoodsActivity.kt`).
- [x] 기분 기록 다이얼로그 (`MainActivity.kt`), 달력 (`MainActivity.kt`), 통계 기능 (`StatsActivity.kt`)에 사용자 정의 감정 연동.
적용 내용: 데이터 구조 설계, CRUD 구현, UI/UX 디자인, 기존 기능과의 통합.

### Task 9: 통계 기간 설정 고급화 (신규)

주요 목표: 사용자가 통계 확인할 기간을 보다 유연하고 직관적으로 설정할 수 있도록 다양한 옵션 제공.
세부 작업:
- [x] **기간 직접 선택 기능:** 시작 날짜와 종료 날짜를 달력 등으로 직접 선택하여 기간 설정 UI (`activity_stats.xml`) 및 로직 (`StatsActivity.kt`) 구현. (`DatePickerDialog` 연동, 선택된 기간에 따른 차트 데이터 필터링 로직 구현 완료, 차트 데이터셋 라벨 제거)
- [x] **사전 정의된 기간 선택 기능 (Spinner 사용):**
    - `activity_stats.xml`: `Spinner` (`spinnerPredefinedRanges`) 추가.
    - `strings.xml`: "최근 7일", "최근 30일", "이번 주" 옵션을 포함하는 `predefined_date_ranges` 문자열 배열 추가.
    - `StatsActivity.kt`:
        - `Spinner` 초기화 및 어댑터 설정.
        - `OnItemSelectedListener` 구현: 선택된 기간에 따라 `selectedStartDate`, `selectedEndDate` 업데이트 및 차트/텍스트 갱신.
        - "이번 주" 계산 로직 (월요일 시작, 일요일 종료, 오늘 이후 날짜 미포함).
        - 직접 날짜 선택 또는 월별 이동 시 `Spinner` 선택 초기화.
- [x] **통계 화면 UI/UX 개선 (`activity_stats.xml`, `StatsActivity.kt`):**
    - [x] **앱 전체 UI에 하늘색 테마 일관성 적용 및 가독성 확보:**
        - `themes.xml`: `colorPrimaryVariant`, `colorSecondary`, `colorOnSecondary`, `statusBarColor`, `windowLightStatusBar`, `navigationBarColor` 설정 추가 및 수정. `tools` 네임스페이스 추가.
        - `activity_main.xml`: `btnPrevMonth`, `btnNextMonth`, `btnLogMood` 버튼에 하늘색 배경 및 검은색 텍스트 적용.
        - `activity_stats.xml`: `btnPrevStatsMonth`, `btnNextStatsMonth`, `btnStartDate`, `btnEndDate` 버튼에 하늘색 배경 및 검은색 텍스트 적용.
        - `activity_manage_moods.xml`: FAB 버튼 색상 확인 (기존 설정 적절).
        - `dialog_add_mood.xml`: `btnSelectColor` 버튼에 하늘색 배경 및 검은색 텍스트 적용.
        - `item_custom_mood.xml`: 수정/삭제 `ImageButton`의 아이콘 `tint`를 검은색으로 설정.
        - `item_default_mood.xml`: "(기본)" `TextView`의 텍스트 색상을 검은색으로 변경.
        - `dialog_mood_item.xml`: 테마를 잘 따를 것으로 확인되어 별도 수정 없음.
    - [ ] Spinner 및 날짜 선택 영역의 배치, 간격 등 시각적 요소 개선.(선택)
    - [ ] 기타 사용자 경험 향상을 위한 조정.(선택)
적용 내용: UI 디자인, 사용자 경험 최적화.

### Task 10: 최종 점검, 버그 수정 및 마무리

주요 목표: 앱의 안정성을 확보하고 최종 버전을 완성.
세부 작업:
- [x] **코드 리팩토링:**
    - [x] Kotlin 파일들을 기능별 패키지(`activities`, `adapter`, `data`)로 구조화 (사용자 직접 수행).
    - [x] 사용하지 않는 Jetpack Compose 관련 `ui/theme` 디렉토리 및 파일 삭제 (사용자 직접 수행).
    - [x] `import` 경로 최적화 및 빌드 경고 수정 완료.
- [x] 버그 수정: 테스트 과정에서 발견된 모든 버그를 수정.
- [x] 코드 정리: 불필요한 코드를 제거하고, 가독성을 높이기 위해 주석을 추가.
- [x] 아이콘 및 이름 설정: `AndroidManifest.xml` 파일을 수정하여 앱 아이콘과 이름을 설정.
- [x] 최종 테스트: 모든 기능을 처음부터 끝까지 다시 한번 테스트.
- [ ] README.md 업데이트: 프로젝트 설명, 주요 기능, 개발 환경, GIF 삽입 위치 등을 포함하여 README 파일 작성. 