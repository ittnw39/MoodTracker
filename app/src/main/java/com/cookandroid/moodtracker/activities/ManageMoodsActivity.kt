package com.cookandroid.moodtracker

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import android.content.Context

class ManageMoodsActivity : AppCompatActivity(), ColorPickerDialogListener {

    private lateinit var toolbarManageMoods: Toolbar
    private lateinit var rvManageMoods: RecyclerView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var customMoodRepository: CustomMoodRepository
    private lateinit var manageMoodsAdapter: ManageMoodsAdapter

    private val DIALOG_ID_ADD_MOOD = 0
    private var currentEtMoodColor: EditText? = null
    private val moodDataFilename = "mood_data.txt" // MainActivity와 파일 이름 공유

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
                editMood(customMood)
            },
            onItemDelete = { customMood ->
                confirmDeleteMood(customMood)
            }
        )
        rvManageMoods.adapter = manageMoodsAdapter

        fabAddMood = findViewById(R.id.fabAddMood)
        fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }

        loadAndDisplayMoods()
    }

    private fun editMood(customMood: CustomMood) {
        showAddMoodDialog(customMoodToEdit = customMood)
    }

    private fun confirmDeleteMood(customMood: CustomMood) {
        val colorHexToDelete = customMood.colorHex // 삭제 전 색상 코드 저장
        AlertDialog.Builder(this)
            .setTitle("감정 삭제")
            .setMessage("'${customMood.name}' 감정을 정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("삭제") { _, _ ->
                val success = customMoodRepository.deleteCustomMood(customMood.id)
                if (success) {
                    Toast.makeText(this, "'${customMood.name}' 감정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    deleteMoodDataFromFile(colorHexToDelete) // mood_data.txt에서 해당 감정 기록 삭제
                    loadAndDisplayMoods()
                } else {
                    Toast.makeText(this, "감정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showAddMoodDialog(customMoodToEdit: CustomMood? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_mood, null)
        val etMoodName = dialogView.findViewById<EditText>(R.id.etMoodName)
        val etMoodColorLocal = dialogView.findViewById<EditText>(R.id.etMoodColor)
        val btnSelectColor = dialogView.findViewById<Button>(R.id.btnSelectColor)
        val tvColorPreview = dialogView.findViewById<TextView>(R.id.tvColorPreview)

        currentEtMoodColor = etMoodColorLocal

        val dialogTitle = if (customMoodToEdit != null) "감정 수정" else "새 감정 추가"
        var oldColorHex: String? = null // 수정 전 색상 코드 저장 변수

        if (customMoodToEdit != null) {
            etMoodName.setText(customMoodToEdit.name)
            etMoodColorLocal.setText(customMoodToEdit.colorHex)
            oldColorHex = customMoodToEdit.colorHex // 수정 전 색상 코드 저장
            try {
                tvColorPreview.setBackgroundColor(Color.parseColor(customMoodToEdit.colorHex))
                tvColorPreview.text = customMoodToEdit.colorHex
            } catch (e: IllegalArgumentException) {
                tvColorPreview.setBackgroundColor(Color.WHITE)
                tvColorPreview.text = "유효하지 않은 색상"
            }
        }

        etMoodColorLocal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val colorHex = s.toString()
                try {
                    if (colorHex.isNotEmpty() && colorHex.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))) {
                        tvColorPreview.setBackgroundColor(Color.parseColor(colorHex))
                        tvColorPreview.text = colorHex
                    } else {
                        tvColorPreview.setBackgroundColor(Color.WHITE)
                        tvColorPreview.text = if (colorHex.isEmpty()) "색상 코드 입력" else "유효하지 않은 색상"
                    }
                } catch (e: IllegalArgumentException) {
                    tvColorPreview.setBackgroundColor(Color.WHITE)
                    tvColorPreview.text = "유효하지 않은 색상"
                }
            }
        })

        btnSelectColor.setOnClickListener {
            val colorString = etMoodColorLocal.text.toString()
            val initialColor = if (colorString.isNotEmpty()) {
                try {
                    Color.parseColor(colorString)
                } catch (e: IllegalArgumentException) {
                    Log.w("ManageMoodsActivity", "Invalid color string for initialColor: $colorString", e)
                    Color.WHITE // 유효하지 않은 형식일 경우 기본값
                }
            } else {
                Color.WHITE // 비어있을 경우 기본값
            }

            ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setDialogId(DIALOG_ID_ADD_MOOD)
                .setColor(initialColor)
                .setShowAlphaSlider(false)
                .show(this)
        }

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle(dialogTitle)
        builder.setPositiveButton("저장") { _, _ ->
            val moodName = etMoodName.text.toString().trim()
            val moodColor = etMoodColorLocal.text.toString().trim()

            if (moodName.isEmpty()) {
                Toast.makeText(this, "감정 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (!moodColor.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))) {
                Toast.makeText(this, "색상 코드는 #RRGGBB 또는 #AARRGGBB 형식이어야 합니다.", Toast.LENGTH_SHORT).show() // 에러 메시지 수정
                return@setPositiveButton
            }

            val finalMoodColor = if (moodColor.length == 9 && moodColor.startsWith("#")) {
                "#" + moodColor.substring(3) // #AARRGGBB -> #RRGGBB
            } else if (moodColor.length == 8 && !moodColor.startsWith("#")) {
                 "#" + moodColor.substring(2) // AARRGGBB -> #RRGGBB (이 경우는 거의 없지만 방어 코드)
            } else {
                moodColor
            }

            if (customMoodToEdit != null && oldColorHex != null) { // 수정 모드이고, 이전 색상 코드가 있을 때
                val updatedMood = CustomMood(id = customMoodToEdit.id, name = moodName, colorHex = finalMoodColor)
                val success = customMoodRepository.updateCustomMood(updatedMood)
                if (success) {
                    Toast.makeText(this, "감정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    updateMoodDataFile(oldColorHex, finalMoodColor) // mood_data.txt 업데이트
                } else {
                    Toast.makeText(this, "감정 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else { // 새 감정 추가 모드
                val addedMood = customMoodRepository.addCustomMood(moodName, finalMoodColor)
                Toast.makeText(this, "'${addedMood.name}' 감정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }
            loadAndDisplayMoods()
        }
        builder.setNegativeButton("취소", null)
        builder.create().show()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        if (dialogId == DIALOG_ID_ADD_MOOD) {
            val colorHex = String.format("#%06X", (0xFFFFFF and color))
            currentEtMoodColor?.setText(colorHex)
        }
    }

    override fun onDialogDismissed(dialogId: Int) {
        // 다이얼로그가 닫힐 때 필요한 작업 (현재는 없음)
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
        finish()
        return true
    }

    // mood_data.txt에서 특정 색상 코드를 다른 색상 코드로 변경하는 함수
    private fun updateMoodDataFile(oldColorHex: String, newColorHex: String) {
        if (oldColorHex == newColorHex) return // 색상이 같다면 변경 필요 없음

        val file = File(filesDir, moodDataFilename)
        if (!file.exists()) return

        val newLines = mutableListOf<String>()
        try {
            openFileInput(moodDataFilename).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2 && parts[1] == oldColorHex) {
                        newLines.add("${parts[0]}:$newColorHex")
                    } else {
                        newLines.add(line)
                    }
                }
            }

            // 새 내용으로 파일 덮어쓰기
            openFileOutput(moodDataFilename, MODE_PRIVATE).use {
                it.write(newLines.joinToString("\n").toByteArray())
                if (newLines.isNotEmpty()) { // 파일 끝에 불필요한 개행문자 추가 방지
                    it.write("\n".toByteArray())
                }
            }
            Log.d("ManageMoodsActivity", "mood_data.txt 업데이트: $oldColorHex -> $newColorHex")
        } catch (e: Exception) {
            Log.e("ManageMoodsActivity", "mood_data.txt 업데이트 실패", e)
            Toast.makeText(this, "기록된 기분 데이터 업데이트 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // mood_data.txt에서 특정 색상 코드를 포함하는 라인을 삭제하는 함수
    private fun deleteMoodDataFromFile(colorHexToDelete: String) {
        val file = File(filesDir, moodDataFilename)
        if (!file.exists()) return

        val newLines = mutableListOf<String>()
        var linesChanged = false
        try {
            openFileInput(moodDataFilename).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2 && parts[1] == colorHexToDelete) {
                        linesChanged = true // 삭제될 라인이 있었음
                    } else {
                        newLines.add(line)
                    }
                }
            }

            if (linesChanged) {
                // 새 내용으로 파일 덮어쓰기
                openFileOutput(moodDataFilename, MODE_PRIVATE).use {
                    it.write(newLines.joinToString("\n").toByteArray())
                    if (newLines.isNotEmpty()) { // 파일 끝에 불필요한 개행문자 추가 방지
                         it.write("\n".toByteArray())
                    }
                }
                Log.d("ManageMoodsActivity", "mood_data.txt 에서 $colorHexToDelete 관련 기록 삭제됨")
            }
        } catch (e: Exception) {
            Log.e("ManageMoodsActivity", "mood_data.txt 기록 삭제 실패", e)
            Toast.makeText(this, "기록된 기분 데이터 삭제 실패", Toast.LENGTH_SHORT).show()
        }
    }
} 