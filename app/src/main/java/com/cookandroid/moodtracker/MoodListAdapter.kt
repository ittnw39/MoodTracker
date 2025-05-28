package com.cookandroid.moodtracker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView

class MoodListAdapter(
    context: Context,
    private val moods: Map<String, String>,
    private val moodNames: Array<String>,
    private var selectedPosition: Int
) : ArrayAdapter<String>(context, R.layout.dialog_mood_item, moodNames) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.dialog_mood_item, parent, false)
            viewHolder = ViewHolder(
                view.findViewById(R.id.colorCircleView),
                view.findViewById(R.id.moodRadioButton)
            )
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val moodName = moodNames[position]
        val moodColorHex = moods[moodName]

        viewHolder.moodRadioButton.text = moodName
        viewHolder.moodRadioButton.isChecked = (position == selectedPosition)

        if (moodColorHex != null) {
            val color = Color.parseColor(moodColorHex)
            val circleDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                // 테두리를 추가하고 싶다면 아래 주석 해제
                // setStroke(2, Color.DKGRAY) 
            }
            viewHolder.colorCircleView.background = circleDrawable
        } else {
            viewHolder.colorCircleView.background = null // 색상 정보가 없을 경우 기본 배경
        }
        
        // 라디오 버튼 상태 변경을 위해 아이템 뷰 전체에 클릭 리스너를 설정할 수도 있음
        // 또는 AlertDialog에서 직접 아이템 클릭을 처리.
        // 여기서는 RadioButton 자체의 클릭은 막고 AlertDialog에서 처리하도록 설정.

        return view
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    private class ViewHolder(val colorCircleView: View, val moodRadioButton: RadioButton)
} 