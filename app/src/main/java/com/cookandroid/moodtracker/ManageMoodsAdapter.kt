package com.cookandroid.moodtracker

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ManageMoodsAdapter(
    private var moodList: List<MoodListItem>,
    private val onItemEdit: (CustomMood) -> Unit, // 수정 버튼 클릭 리스너 (미구현 상태)
    private val onItemDelete: (CustomMood) -> Unit // 삭제 버튼 클릭 리스너 (미구현 상태)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DEFAULT = 0
        private const val TYPE_CUSTOM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (moodList[position]) {
            is MoodListItem.DefaultMoodItem -> TYPE_DEFAULT
            is MoodListItem.CustomMoodItem -> TYPE_CUSTOM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_DEFAULT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_default_mood, parent, false)
            DefaultMoodViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_custom_mood, parent, false)
            CustomMoodViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = moodList[position]) {
            is MoodListItem.DefaultMoodItem -> (holder as DefaultMoodViewHolder).bind(item)
            is MoodListItem.CustomMoodItem -> (holder as CustomMoodViewHolder).bind(item, onItemEdit, onItemDelete)
        }
    }

    override fun getItemCount(): Int = moodList.size

    fun updateData(newMoodList: List<MoodListItem>) {
        moodList = newMoodList
        notifyDataSetChanged()
    }

    // --- ViewHolders ---

    class DefaultMoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodColorView: View = itemView.findViewById(R.id.viewMoodColor)
        private val moodNameTextView: TextView = itemView.findViewById(R.id.tvMoodName)

        fun bind(item: MoodListItem.DefaultMoodItem) {
            moodNameTextView.text = item.name
            val color = Color.parseColor(item.colorHex)
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            moodColorView.background = drawable
        }
    }

    class CustomMoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodColorView: View = itemView.findViewById(R.id.viewMoodColor)
        private val moodNameTextView: TextView = itemView.findViewById(R.id.tvMoodName)
        private val editButton: ImageView = itemView.findViewById(R.id.ivEditMood)
        private val deleteButton: ImageView = itemView.findViewById(R.id.ivDeleteMood)

        fun bind(item: MoodListItem.CustomMoodItem, 
                 onItemEdit: (CustomMood) -> Unit, 
                 onItemDelete: (CustomMood) -> Unit) {
            moodNameTextView.text = item.mood.name
            val color = Color.parseColor(item.mood.colorHex)
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            moodColorView.background = drawable

            // TODO: 수정 및 삭제 버튼 기능 활성화 및 리스너 연결 (다음 단계에서 진행)
            // editButton.visibility = View.VISIBLE 
            // deleteButton.visibility = View.VISIBLE
            // editButton.setOnClickListener { onItemEdit(item.mood) }
            // deleteButton.setOnClickListener { onItemDelete(item.mood) }
        }
    }
} 