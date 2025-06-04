package com.cookandroid.moodtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageMoodsActivity : AppCompatActivity() {

    private lateinit var toolbarManageMoods: Toolbar
    private lateinit var rvManageMoods: RecyclerView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var customMoodRepository: CustomMoodRepository

    // 기본 감정 목록 (MainActivity의 것과 동일하게 우선 정의, 추후 통합 관리 고려)
    private val defaultMoods = mapOf(
        "행복" to "#FFEB3B",
        "좋음" to "#AED581",
        "보통" to "#FFF59D",
        "나쁨" to "#FFAB91",
        "슬픔" to "#B0BEC5"
    )

    // RecyclerView에 표시될 통합 감정 목록 (기본 + 사용자 정의)
    // 실제로는 CustomMood 객체 또는 이를 감싸는 다른 형태의 객체 리스트가 될 것임
    private val combinedMoodsList = mutableListOf<Any>() // 임시로 Any 타입
    // private lateinit var manageMoodsAdapter: ManageMoodsAdapter // 추후 생성 예정

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_moods)

        toolbarManageMoods = findViewById(R.id.toolbarManageMoods)
        setSupportActionBar(toolbarManageMoods)
        supportActionBar?.title = "감정 관리"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        customMoodRepository = CustomMoodRepository(this)

        rvManageMoods = findViewById(R.id.rvManageMoods)
        rvManageMoods.layoutManager = LinearLayoutManager(this)
        // manageMoodsAdapter = ManageMoodsAdapter(combinedMoodsList, {/* onItemEdit */}, {/* onItemDelete */})
        // rvManageMoods.adapter = manageMoodsAdapter

        fabAddMood = findViewById(R.id.fabAddMood)
        fabAddMood.setOnClickListener {
            // TODO: 새 감정 추가 다이얼로그 표시 로직 구현
            Toast.makeText(this, "새 감정 추가 기능 (구현 예정)", Toast.LENGTH_SHORT).show()
        }

        loadAndDisplayMoods()
    }

    private fun loadAndDisplayMoods() {
        combinedMoodsList.clear()

        // 1. 기본 감정 추가 (수정/삭제 불가 표시 필요)
        defaultMoods.forEach { (name, color) ->
            // 어댑터에 맞게 기본 감정 객체 생성하여 추가 (지금은 로그만)
            Log.d("ManageMoodsActivity", "기본 감정: $name, $color")
            // combinedMoodsList.add(MoodListItem.DefaultMoodItem(name, color)) // 예시
        }

        // 2. 사용자 정의 감정 로드 및 추가
        val customMoods = customMoodRepository.loadCustomMoods()
        customMoods.forEach { customMood ->
            Log.d("ManageMoodsActivity", "사용자 감정: ${customMood.name}, ${customMood.colorHex}")
            // combinedMoodsList.add(MoodListItem.CustomMoodItem(customMood)) // 예시
        }

        // manageMoodsAdapter.notifyDataSetChanged() // 어댑터에 변경 알림
        Log.d("ManageMoodsActivity", "감정 목록 로드 완료. 총 ${combinedMoodsList.size}개 (어댑터 미적용)")

        if (customMoods.isEmpty()) {
            Log.i("ManageMoodsActivity", "사용자 정의 감정이 없습니다.")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish() // 현재 액티비티 종료하여 이전 화면으로 돌아감
        return true
    }
} 