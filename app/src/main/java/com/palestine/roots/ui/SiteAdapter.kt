package com.palestine.roots.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.HapticFeedbackConstants
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.palestine.roots.R
import com.palestine.roots.domain.model.Site

class SiteAdapter(
    private val onSiteClick: (Site) -> Unit,
    private val onFavoriteClick: (Site) -> Unit
) : ListAdapter<Site, SiteAdapter.SiteViewHolder>(SiteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_site, parent, false)
        return SiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val site = getItem(position)
        holder.bind(site)
    }

    inner class SiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // IDs match the XML layout: item_site.xml
        private val ivSiteImage: ImageView = itemView.findViewById(R.id.img_site)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category_badge)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ib_favorite)
        private val tvSiteName: TextView = itemView.findViewById(R.id.tv_site_name)
        private val tvSiteCity: TextView = itemView.findViewById(R.id.tv_site_city)

        fun bind(site: Site) {
            tvSiteName.text = site.name
            tvSiteCity.text = site.city
            tvCategory.text = site.category

            // Load image from assets
            // Glide supports file:///android_asset/ URIs natively
            val imageUri = site.imageUrl
            try {
                Glide.with(itemView.context)
                    .load(imageUri)
                    .placeholder(R.drawable.placeholder_site)
                    .error(R.drawable.placeholder_site)
                    .centerCrop()
                    .into(ivSiteImage)
            } catch (e: Exception) {
                ivSiteImage.setImageResource(R.drawable.placeholder_site)
            }

            ivFavorite.setImageResource(
                if (site.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )

            itemView.setOnClickListener {
                onSiteClick(site)
            }

            ivFavorite.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onFavoriteClick(site)
            }

            itemView.setOnLongClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onFavoriteClick(site)
                true
            }
        }
    }

    class SiteDiffCallback : DiffUtil.ItemCallback<Site>() {
        override fun areItemsTheSame(oldItem: Site, newItem: Site): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Site, newItem: Site): Boolean {
            return oldItem == newItem
        }
    }
}
