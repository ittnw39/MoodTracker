## MoodTracker 앱 개발 작업 목록

### Task 1: 프로젝트 설정 및 메인 화면 UI 설계 (완료)

주요 목표: 앱 개발 환경 설정 및 사용자가 상호작용할 기본 화면 구성.
세부 작업:
- [x] Android Studio 프로젝트 생성:
    - 새로운 Android Studio 프로젝트를 'MoodCalendar' 이름으로 생성합니다. (사용자 실제 프로젝트 이름: MoodTracker)
    - 적용 내용: Project4_1 생성 과정 과 같이 프로젝트 이름, 패키지 이름, Kotlin 언어, 최소 API 레벨 (예: API 16: Android 4.1)을 설정합니다.
- [x] activity_main.xml 레이아웃 설계:
    - 최상위 레이아웃으로 LinearLayout (수직 방향)을 사용합니다.
    - 상단에 LinearLayout (수평 방향)을 추가하여 월 이동 UI를 구성합니다.
        - Button 1개 ('이전 달', ID: btnPrevMonth)
        - TextView 1개 (현재 '년/월' 표시, ID: tvCurrentMonth)
        - Button 1개 ('다음 달', ID: btnNextMonth)
    - 중단에 GridLayout을 추가하여 달력 본체를 구성합니다 (7열, ID: gridCalendar).
    - 하단에 TextView 1개 (선택된 날짜의 기분 정보 표시, ID: tvSelectedMoodInfo) 추가.
    - 하단에 Button 1개를 추가하여 기분 기록 기능을 실행합니다 ('기분 기록하기', ID: btnLogMood).
    - 적용 내용: LinearLayout의 orientation 속성, GridLayout의 columnCount 속성 을 활용하여 UI 뼈대를 만듭니다. 각 위젯에 id를 부여합니다.
- [x] MainActivity.kt 위젯 참조:
    - MainActivity.kt에서 findViewById를 사용하여 activity_main.xml에 배치된 위젯들의 참조 변수를 선언하고 연결합니다.
    - 적용 내용: Kotlin 변수 선언, findViewById를 통한 위젯 접근 을 사용합니다.

### Task 2: 달력 UI 동적 생성 로직 구현 (완료)

주요 목표: 현재 날짜를 기준으로 GridLayout에 날짜를 표시하고, 월 이동 기능 구현.
세부 작업:
- [x] 날짜 계산 로직:
    - Kotlin 코드에서 Calendar 클래스를 사용하여 현재 연도, 월, 해당 월의 시작 요일, 마지막 날짜 등을 계산하는 로직을 구현합니다.
    - 적용 내용: Kotlin 기본 문법 (변수, 조건문, 연산)  및 Calendar 클래스 (Week 8-1 실습의 데이트피커 설정 부분 참고 )를 활용합니다.
- [x] drawCalendar 함수 구현:
    - GridLayout의 모든 자식 뷰를 제거합니다.
    - 계산된 날짜 정보를 바탕으로, for 반복문을 사용하여 날짜 수만큼 TextView 위젯을 동적으로 생성합니다.
    - 생성된 TextView에 날짜 텍스트를 설정하고 GridLayout에 추가합니다.
    - 적용 내용: Kotlin 코드로 뷰 생성 (Week 5 실습 5-1 참고 ) 및 GridLayout 활용.
- [x] 월 이동 버튼 이벤트 처리:
    - '이전 달', '다음 달' Button에 setOnClickListener를 설정합니다.
    - 버튼 클릭 시 현재 표시된 월/년을 변경하고 tvCurrentMonth 텍스트를 업데이트한 후, drawCalendar 함수를 다시 호출하여 달력을 새로 그립니다.
    - 적용 내용: 버튼 이벤트 처리, 람다식.

### Task 3: 기분 선택 다이얼로그 구현 (완료)

주요 목표: '기분 기록하기' 버튼 클릭 시, 기분을 선택할 수 있는 대화상자 표시.
세부 작업:
- [x] 기분 데이터 정의:
    - 앱에서 사용할 기분 목록과 각 기분에 해당하는 색상 코드(Hex)를 Kotlin 코드 내에 배열 또는 Map으로 정의합니다. (예: val moods = mapOf("행복" to "#FFFF00", "슬픔" to "#0000FF"))
    - 적용 내용: Kotlin 배열  또는 컬렉션.
- [x] '기분 기록하기' 버튼 이벤트 처리:
    - btnLogMood Button에 setOnClickListener를 설정합니다.
- [x] 대화상자 생성:
    - 버튼 클릭 리스너 내에서 AlertDialog.Builder를 사용하여 대화상자를 생성합니다.
    - 대화상자 제목 (setTitle), 기분 목록 (setItems 또는 setSingleChoiceItems)을 설정합니다. setSingleChoiceItems를 사용하여 RadioButton 형태로 표시합니다.
    - '확인'(setPositiveButton)과 '취소'(setNegativeButton) 버튼을 추가합니다.
    - 적용 내용: 기본 대화상자  및 목록 대화상자  생성 방법을 활용합니다.
- [x] '확인' 버튼 로직:
    - '확인' 버튼 클릭 시, 선택된 기분(색상)과 현재 날짜(또는 달력에서 선택된 날짜)를 가져와 saveMood 함수를 호출합니다.
    - Toast를 사용하여 "기분이 저장되었습니다."와 같은 피드백 메시지를 표시합니다.
    - drawCalendar 함수를 호출하여 달력을 갱신합니다.
    - 적용 내용: 대화상자 버튼 리스너, Toast 사용법.

### Task 4: 기분 데이터 저장 기능 구현 (완료)

주요 목표: 사용자가 선택한 기분 데이터를 내부 저장소에 영구적으로 저장.
세부 작업:
- [x] saveMood 함수 구현:
    - 날짜와 기분(색상) 데이터를 파라미터로 받습니다.
    - openFileOutput을 사용하여 'mood_data.txt'와 같은 이름의 파일을 엽니다. (날짜별 덮어쓰기 방식)
    - FileOutputStream을 사용하여 YYYY-MM-DD:ColorHexCode 형식의 문자열을 파일에 씁니다.
    - 파일 쓰기 후 스트림을 닫습니다.
    - 적용 내용: 내장 메모리 파일 쓰기, '간단 일기장' 앱의 파일 저장 방식 을 참조합니다.
- [x] 예외 처리:
    - 파일 입출력 시 발생할 수 있는 오류를 처리하기 위해 try~catch 문을 사용합니다.

### Task 5: 기분 데이터 로드 및 달력 색상 적용 (완료)

주요 목표: 저장된 기분 데이터를 불러와 달력 UI에 색상으로 반영.
세부 작업:
- [x] loadMoods 함수 구현:
    - openFileInput을 사용하여 'mood_data.txt' 파일을 엽니다.
    - FileInputStream과 BufferedReader 등을 사용하여 파일 내용을 줄 단위로 읽습니다.
    - 읽어온 각 줄을 파싱하여 날짜를 키로, 색상 코드를 값으로 하는 Map<String, String> 형태의 데이터 구조에 저장합니다.
    - 파일이 없을 경우를 대비한 예외 처리를 합니다.
    - 적용 내용: 내장 메모리 파일 읽기, '간단 일기장' 앱의 파일 읽기 방식 을 참조합니다.
- [x] drawCalendar 함수 수정:
    - drawCalendar 함수 시작 시 loadMoods 함수를 호출하여 기분 데이터를 로드합니다.
    - GridLayout에 날짜 TextView를 추가할 때, 해당 날짜가 로드된 Map에 있는지 확인합니다.
    - 데이터가 존재하면, 해당 TextView의 setBackgroundColor(Color.parseColor(colorHexCode)) 메소드를 호출하여 배경색을 설정합니다.
    - 적용 내용: Kotlin Map 사용, 뷰의 속성 변경.

### Task 6: UI 개선 및 기능 테스트 (일부 완료)

주요 목표: 앱의 시각적 완성도를 높이고, 핵심 기능의 오류를 점검.
세부 작업:
- [x] UI 디테일 조정:
    - [x] 달력의 날짜 클릭 기능 구현:
        - 과거 및 오늘 날짜 클릭 시 해당 날짜를 선택된 날짜(`selectedDateCalendar`)로 설정.
        - 미래 날짜 클릭 시 Toast 메시지로 알림.
        - 선택된 날짜의 기분 정보를 하단 `tvSelectedMoodInfo` TextView에 표시 (기록된 기분 또는 "기록 없음").
        - 기분 기록 다이얼로그에 선택된 날짜가 반영되고, 해당 날짜에 기분이 이미 기록되어 있으면 기본 선택값으로 표시.
    - [ ] GridLayout 내 TextView들의 padding, margin, gravity 속성을 조정하여 달력 형태를 보기 좋게 만듭니다.
    - [ ] 기분 선택 RadioButton 옆에 해당 색상을 작은 원이나 사각형으로 표시합니다.
    - 적용 내용: View 클래스의 XML 속성, Paint와 Canvas를 이용한 간단한 도형 그리기 (필요시 Week 10 ) 또는 단순히 View의 배경색 설정.
- [ ] 기능 테스트:
    - 월 이동이 정확히 동작하는지 확인합니다.
    - 기분 저장 및 로드가 정상적으로 이루어지는지 확인합니다.
    - 다양한 날짜에 기분을 기록하고 달력에 올바르게 표시되는지 확인합니다.
    - 기존 기분을 수정(덮어쓰기)하는 시나리오를 테스트합니다.
    - 날짜 클릭 및 선택된 날짜 관련 기능이 정확히 동작하는지 테스트합니다.
- [ ] (리팩토링) 파일 입출력 작업 백그라운드 스레드로 이전 (예: Coroutines 사용)

### Task 7: 최종 점검, 버그 수정 및 마무리

주요 목표: 앱의 안정성을 확보하고 최종 버전을 완성.
세부 작업:
- [ ] 버그 수정: 테스트 과정에서 발견된 모든 버그를 수정합니다.
- [ ] 코드 정리: 불필요한 코드를 제거하고, 가독성을 높이기 위해 주석을 추가합니다.
- [ ] 아이콘 및 이름 설정: AndroidManifest.xml 파일을 수정하여 앱 아이콘과 이름을 설정합니다.
- [ ] 최종 테스트: 모든 기능을 처음부터 끝까지 다시 한번 테스트합니다. 