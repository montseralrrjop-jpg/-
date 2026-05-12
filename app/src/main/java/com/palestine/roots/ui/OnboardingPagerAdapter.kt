package com.palestine.roots.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.palestine.roots.R

class OnboardingPagerAdapter(
    private val items: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder>() {

    data class OnboardingItem(
        val icon: Int,
        val title: String,
        val description: String,
        val color: Int = 0
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivIcon: ImageView = itemView.findViewById(R.id.img_onboarding_icon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_onboarding_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_onboarding_description)

        fun bind(item: OnboardingItem) {
            ivIcon.setImageResource(item.icon)
            tvTitle.text = item.title
            tvDescription.text = item.description

            if (item.color != 0) {
                ivIcon.setBackgroundColor(item.color)
            }
        }
    }
}
