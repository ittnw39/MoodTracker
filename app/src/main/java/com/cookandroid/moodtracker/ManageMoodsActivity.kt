package com.cookandroid.moodtracker

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageMoodsActivity : AppCompatActivity() {

    private lateinit var toolbarManageMoods: Toolbar
    private lateinit var rvManageMoods: RecyclerView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var customMoodRepository: CustomMoodRepository
    private lateinit var manageMoodsAdapter: ManageMoodsAdapter

    // 기본 감정 목록 (MainActivity의 것과 동일하게 우선 정의, 추후 통합 관리 고려)
    private val defaultMoods = mapOf(
        "행복" to "#FFEB3B",
        "좋음" to "#AED581",
        "보통" to "#FFF59D",
        "나쁨" to "#FFAB91",
        "슬픔" to "#B0BEC5"
    )

    // RecyclerView에 표시될 통합 감정 목록 (기본 + 사용자 정의)
    private val combinedMoodsList = mutableListOf<MoodListItem>()

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
        manageMoodsAdapter = ManageMoodsAdapter(combinedMoodsList,
            onItemEdit = { customMood ->
                // TODO: 수정 다이얼로그 표시 및 로직 구현
                Toast.makeText(this, "수정: ${customMood.name}", Toast.LENGTH_SHORT).show()
            },
            onItemDelete = { customMood ->
                // TODO: 삭제 확인 다이얼로그 표시 및 로직 구현
                Toast.makeText(this, "삭제: ${customMood.name}", Toast.LENGTH_SHORT).show()
            }
        )
        rvManageMoods.adapter = manageMoodsAdapter

        fabAddMood = findViewById(R.id.fabAddMood)
        fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }

        loadAndDisplayMoods()
    }

    private fun showAddMoodDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_mood, null)
        val etMoodName = dialogView.findViewById<EditText>(R.id.etMoodName)
        val etMoodColorHex = dialogView.findViewById<EditText>(R.id.etMoodColorHex)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("저장", null) // 리스너는 아래에서 오버라이드
            .setNegativeButton("취소") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener { 
                val moodName = etMoodName.text.toString().trim()
                val moodColorHex = etMoodColorHex.text.toString().trim()

                if (moodName.isEmpty()) {
                    Toast.makeText(this, "감정 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!isValidColorHex(moodColorHex)) {
                    Toast.makeText(this, "올바른 색상 코드 형식이 아닙니다. (예: #RRGGBB)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                try {
                    Color.parseColor(moodColorHex) // 실제 색상 유효성 검사
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this, "유효하지 않은 색상 코드입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 이름 중복 검사 (선택 사항, 현재 Repository는 중복 허용)
                val existingMoods = customMoodRepository.loadCustomMoods()
                if (existingMoods.any { it.name.equals(moodName, ignoreCase = true) }) {
                    Toast.makeText(this, "이미 존재하는 감정 이름입니다.", Toast.LENGTH_SHORT).show()
                    // return@setOnClickListener // 필요에 따라 중복 저장 방지
                }

                customMoodRepository.addCustomMood(moodName, moodColorHex)
                Toast.makeText(this, "'${moodName}' 감정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                loadAndDisplayMoods() // 목록 새로고침
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun isValidColorHex(colorHex: String): Boolean {
        return colorHex.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }

    private fun loadAndDisplayMoods() {
        combinedMoodsList.clear()
        defaultMoods.forEach {
            combinedMoodsList.add(MoodListItem.DefaultMoodItem(it.key, it.value))
        }
        val customMoods = customMoodRepository.loadCustomMoods()
        customMoods.forEach {
            combinedMoodsList.add(MoodListItem.CustomMoodItem(it))
        }
        manageMoodsAdapter.updateData(combinedMoodsList)
        
        if (customMoods.isEmpty()) {
            Log.i("ManageMoodsActivity", "저장된 사용자 정의 감정이 없습니다.")
        } else {
            Log.i("ManageMoodsActivity", "총 ${customMoods.size}개의 사용자 정의 감정이 로드되었습니다.")
        }
        Log.i("ManageMoodsActivity", "RecyclerView에 총 ${combinedMoodsList.size}개의 아이템 표시.")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish() // 현재 액티비티 종료하여 이전 화면으로 돌아감
        return true
    }
} 