package com.palestine.roots.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.palestine.roots.R
import com.palestine.roots.data.local.PreferencesManager
import com.palestine.roots.data.local.db.PalestineDatabase
import com.palestine.roots.data.repository.SiteRepositoryImpl
import com.palestine.roots.databinding.ActivityHomeBinding
import com.palestine.roots.util.LocaleHelper
import com.palestine.roots.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private val viewModel: HomeViewModel by viewModels {
        val dao = PalestineDatabase.getInstance(this).siteDao()
        val repo = SiteRepositoryImpl(dao)
        val prefs = PreferencesManager(this)
        HomeViewModel.Factory(repo, prefs)
    }

    private lateinit var siteAdapter: SiteAdapter
    private var currentLang = "ar"

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupToolbarActions()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        siteAdapter = SiteAdapter(
            onSiteClick = { site ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_SITE_ID, site.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { site ->
                viewModel.toggleFavorite(site.id, !site.isFavorite)
            }
        )
        binding.rvSites.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
            adapter = siteAdapter
        }
    }

    private fun setupProvinceChips() {
        val chipGroup = binding.cgProvinces
        chipGroup.removeAllViews()

        // "All" chip – uses string resource so it switches with locale
        val allLabel = getString(R.string.filter_all)
        val allChip = Chip(this).apply {
            text = allLabel
            isClickable = true
            isCheckable = true
            isChecked = true
            setOnClickListener {
                viewModel.loadAllSites()
                clearChipSelection(chipGroup, this)
            }
        }
        chipGroup.addView(allChip)

        // Province chips – display name depends on current language
        viewModel.getProvinceNames(currentLang).forEach { displayName ->
            val chip = Chip(this).apply {
                text = displayName
                isClickable = true
                isCheckable = true
                setOnClickListener {
                    // Always resolve to Arabic name for the DB query
                    val arabicCity = viewModel.resolveArabicCityName(displayName)
                    viewModel.filterByCity(arabicCity)
                    clearChipSelection(chipGroup, this)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun clearChipSelection(chipGroup: com.google.android.material.chip.ChipGroup, selectedChip: Chip) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = (chip == selectedChip)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text?.toString()?.trim() ?: ""
            viewModel.onSearchQueryChanged(query)
        }
    }

    private fun setupToolbarActions() {
        binding.ibMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        binding.ibFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        binding.ibLanguage.setOnClickListener {
            currentLang = if (currentLang == "ar") "en" else "ar"
            viewModel.setLanguage(currentLang)
            // Apply the new locale and recreate so all resources reload
            LocaleHelper.setLocale(this, currentLang)
            recreate()
        }

        binding.ibDarkMode.setOnClickListener {
            lifecycleScope.launch {
                val currentDarkMode = viewModel.isDarkMode.first()
                val newMode = !currentDarkMode
                viewModel.toggleDarkMode(newMode)
                AppCompatDelegate.setDefaultNightMode(
                    if (newMode) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        binding.fabMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is HomeViewModel.UiState.Loading -> {
                        binding.rvSites.visibility = View.GONE
                    }
                    is HomeViewModel.UiState.Success -> {
                        binding.rvSites.visibility = View.VISIBLE
                        siteAdapter.language = currentLang
                        siteAdapter.submitList(state.sites)
                    }
                    is HomeViewModel.UiState.Error -> {
                        binding.rvSites.visibility = View.GONE
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.language.collectLatest { lang ->
                currentLang = lang
                siteAdapter.language = lang
                setupProvinceChips()
            }
        }
    }
}
