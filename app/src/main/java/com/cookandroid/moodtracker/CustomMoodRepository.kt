package com.cookandroid.moodtracker

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.UUID

// Gson 같은 라이브러리를 사용하면 JSON 처리가 더 쉬워지지만, 우선은 기본 파일 입출력으로 가정합니다.
// 실제 구현 시에는 Gson 또는 Kotlinx.serialization 사용을 강력히 권장합니다.

class CustomMoodRepository(private val context: Context) {

    private val customMoodsFilename = "custom_moods.json"
    private val gson = Gson()

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
            val type = object : TypeToken<MutableList<CustomMood>>() {}.type
            return gson.fromJson(jsonString, type) ?: mutableListOf()
        } catch (e: IOException) {
            Log.e("CustomMoodRepository", "사용자 정의 감정 로드 실패 (IO)", e)
        } catch (e: Exception) { // JsonSyntaxException 등 기타 예외 처리
            Log.e("CustomMoodRepository", "사용자 정의 감정 로드 실패 (JSON 파싱 등)", e)
            // 문제가 있는 파일은 삭제하거나 백업 후 빈 리스트 반환 고려
            // file.delete() // 또는 file.renameTo(File(context.filesDir, "$customMoodsFilename.bak"))
        }
        return mutableListOf()
    }

    // 사용자 정의 감정 목록을 파일에 저장
    fun saveCustomMoods(moods: List<CustomMood>) {
        try {
            val jsonString = gson.toJson(moods)
            context.openFileOutput(customMoodsFilename, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            Log.e("CustomMoodRepository", "사용자 정의 감정 저장 실패", e)
        }
    }

    fun addCustomMood(name: String, colorHex: String): CustomMood {
        val moods = loadCustomMoods()
        // ID 중복 체크 (이론적으로 UUID는 거의 중복되지 않으나, 방어적으로 추가 가능)
        // 색상값 중복 허용 여부는 정책에 따라 결정 (현재는 허용)
        // 감정 이름 중복 허용 여부도 정책에 따라 결정 (현재는 허용, 단 UI에서 사용자에게 혼동을 줄 수 있음)

        val newMood = CustomMood(id = generateUniqueId(), name = name, colorHex = colorHex)
        moods.add(newMood)
        saveCustomMoods(moods)
        Log.d("CustomMoodRepository", "새 감정 추가됨: $newMood, 총 ${moods.size}개")
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
            Log.d("CustomMoodRepository", "감정 삭제됨: ID $moodId, 남은 감정 ${moods.size}개")
        }
        return removed
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }
} 