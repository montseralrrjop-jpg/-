package com.palestine.roots.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.palestine.roots.data.local.PreferencesManager
import com.palestine.roots.data.local.db.PalestineDatabase
import com.palestine.roots.data.repository.SiteRepositoryImpl
import com.palestine.roots.databinding.ActivityFavoritesBinding
import com.palestine.roots.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private val binding by lazy { ActivityFavoritesBinding.inflate(layoutInflater) }
    private val viewModel: HomeViewModel by viewModels {
        val dao = PalestineDatabase.getInstance(this).siteDao()
        val repo = SiteRepositoryImpl(dao)
        val prefs = PreferencesManager(this)
        HomeViewModel.Factory(repo, prefs)
    }

    private lateinit var siteAdapter: SiteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRecyclerView()
        observeFavorites()

        binding.toolbarFavorites.setNavigationOnClickListener {
            finish()
        }
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
        binding.rvFavorites.apply {
            layoutManager = GridLayoutManager(this@FavoritesActivity, 2)
            adapter = siteAdapter
        }
    }

    private fun observeFavorites() {
        lifecycleScope.launch {
            viewModel.favoriteSites.collectLatest { favorites ->
                siteAdapter.submitList(favorites)
                if (favorites.isEmpty()) {
                    binding.rvFavorites.visibility = View.GONE
                    binding.llEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvFavorites.visibility = View.VISIBLE
                    binding.llEmptyState.visibility = View.GONE
                }
            }
        }
    }
}
