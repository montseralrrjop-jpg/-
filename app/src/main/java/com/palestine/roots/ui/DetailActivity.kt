package com.palestine.roots.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.palestine.roots.R
import com.palestine.roots.data.local.PreferencesManager
import com.palestine.roots.data.local.db.PalestineDatabase
import com.palestine.roots.data.repository.SiteRepositoryImpl
import com.palestine.roots.databinding.ActivityDetailBinding
import com.palestine.roots.domain.model.Site
import com.palestine.roots.util.LocaleHelper
import com.palestine.roots.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }
    private val viewModel: HomeViewModel by viewModels {
        val dao = PalestineDatabase.getInstance(this).siteDao()
        val repo = SiteRepositoryImpl(dao)
        val prefs = PreferencesManager(this)
        HomeViewModel.Factory(repo, prefs)
    }

    private var currentSite: Site? = null
    private var currentLang: String = "ar"

    companion object {
        const val EXTRA_SITE_ID = "extra_site_id"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycleScope.launch {
            currentLang = viewModel.language.first()
        }

        val siteId = intent.getStringExtra(EXTRA_SITE_ID)
        if (siteId == null) {
            finish()
            return
        }

        setupToolbar()
        loadSite(siteId)
    }

    private fun setupToolbar() {
        binding.ibDetailBack.setOnClickListener {
            finish()
        }
    }

    private fun loadSite(siteId: String) {
        lifecycleScope.launch {
            try {
                val site = viewModel.getSiteById(siteId)
                if (site != null) {
                    currentSite = site
                    displaySite(site)
                } else {
                    val msg = if (currentLang == "en") "Site not found" else "الموقع غير موجود"
                    Toast.makeText(this@DetailActivity, msg, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                val msg = if (currentLang == "en") "An error occurred" else "حدث خطأ"
                Toast.makeText(this@DetailActivity, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displaySite(site: Site) {
        val lang = currentLang
        binding.tvDetailSiteName.text = if (lang == "en") site.nameEn else site.name
        binding.tvDetailCityChip.text = if (lang == "en") site.cityEn else site.city
        binding.tvDetailDescription.text = if (lang == "en") site.descriptionEn else site.description
        binding.tvDetailHistory.text = if (lang == "en") site.historyEn else site.history
        binding.tvDetailCategoryBadge.text = if (lang == "en") site.categoryEn else site.category

        if (site.foundationYear.isNullOrEmpty()) {
            binding.tvDetailYearChip.visibility = View.GONE
        } else {
            binding.tvDetailYearChip.visibility = View.VISIBLE
            binding.tvDetailYearChip.text = site.foundationYear
        }

        Glide.with(this)
            .load(site.imageUrl)
            .placeholder(R.drawable.placeholder_site)
            .error(R.drawable.placeholder_site)
            .centerCrop()
            .into(binding.imgDetailHero)

        updateFavoriteButton(site.isFavorite)

        binding.ibDetailFavorite.setOnClickListener {
            val newFavorite = !site.isFavorite
            viewModel.toggleFavorite(site.id, newFavorite)
            currentSite = site.copy(isFavorite = newFavorite)
            updateFavoriteButton(newFavorite)
        }

        binding.btnOpenMaps.setOnClickListener {
            openInGoogleMaps(site)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.ibDetailFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )
    }

    private fun openInGoogleMaps(site: Site) {
        val uri = "geo:${site.latitude},${site.longitude}?q=${site.latitude},${site.longitude}(${site.name})"
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val webUri = "https://www.google.com/maps/search/?api=1&query=${site.latitude},${site.longitude}"
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri)))
        }
    }
}
