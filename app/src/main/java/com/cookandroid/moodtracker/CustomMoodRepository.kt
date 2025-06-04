package com.cookandroid.moodtracker

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.UUID

// Gson 같은 라이브러리를 사용하면 JSON 처리가 더 쉬워지지만, 우선은 기본 파일 입출력으로 가정합니다.
// 실제 구현 시에는 Gson 또는 Kotlinx.serialization 사용을 강력히 권장합니다.

class CustomMoodRepository(private val context: Context) {

    private val customMoodsFilename = "custom_moods.json"

    // 사용자 정의 감정 목록을 로드 (파일이 없거나 비었으면 빈 리스트 반환)
    fun loadCustomMoods(): MutableList<CustomMood> {
        val file = File(context.filesDir, customMoodsFilename)
        if (!file.exists()) {
            return mutableListOf()
        }
        try {
            val jsonString = file.readText()
            if (jsonString.isBlank()) {
                return mutableListOf()
            }
            // TODO: JSON 문자열을 List<CustomMood>로 파싱하는 로직 구현 (예: Gson 사용)
            // 예시 (실제로는 파싱 로직 필요):
            // val type = object : TypeToken<MutableList<CustomMood>>() {}.type
            // return Gson().fromJson(jsonString, type) ?: mutableListOf()
            Log.w("CustomMoodRepository", "loadCustomMoods: JSON 파싱 로직 미구현")
            // 임시 반환 (테스트용)
            // return mutableListOf(CustomMood(generateUniqueId(), "테스트 감정", "#FF00FF"))
        } catch (e: IOException) {
            Log.e("CustomMoodRepository", "사용자 정의 감정 로드 실패", e)
        }
        return mutableListOf() // 오류 발생 시 빈 리스트
    }

    // 사용자 정의 감정 목록을 파일에 저장
    fun saveCustomMoods(moods: List<CustomMood>) {
        try {
            // TODO: List<CustomMood>를 JSON 문자열로 변환하는 로직 구현 (예: Gson 사용)
            // val jsonString = Gson().toJson(moods)
            val jsonString = "" // 임시 값
            if (moods.isNotEmpty() && jsonString.isBlank()){
                 Log.w("CustomMoodRepository", "saveCustomMoods: JSON 변환 로직 미구현 또는 빈 문자열 생성됨 (저장 건너뜀)")
                 // 실제로는 여기서 moods를 jsonString으로 만들어야 함
            } else {
                context.openFileOutput(customMoodsFilename, Context.MODE_PRIVATE).use {
                    it.write(jsonString.toByteArray())
                }
            }
        } catch (e: IOException) {
            Log.e("CustomMoodRepository", "사용자 정의 감정 저장 실패", e)
        }
    }

    fun addCustomMood(name: String, colorHex: String): CustomMood {
        val moods = loadCustomMoods()
        val newMood = CustomMood(id = generateUniqueId(), name = name, colorHex = colorHex)
        moods.add(newMood)
        saveCustomMoods(moods)
        return newMood
    }

    fun updateCustomMood(updatedMood: CustomMood): Boolean {
        val moods = loadCustomMoods()
        val index = moods.indexOfFirst { it.id == updatedMood.id }
        if (index != -1) {
            moods[index] = updatedMood
            saveCustomMoods(moods)
            return true
        }
        return false
    }

    fun deleteCustomMood(moodId: String): Boolean {
        val moods = loadCustomMoods()
        val removed = moods.removeAll { it.id == moodId }
        if (removed) {
            saveCustomMoods(moods)
        }
        return removed
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }
} 