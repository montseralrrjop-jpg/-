package com.palestine.roots.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.palestine.roots.R
import com.palestine.roots.data.local.PreferencesManager
import com.palestine.roots.data.local.db.PalestineDatabase
import com.palestine.roots.data.repository.SiteRepositoryImpl
import com.palestine.roots.databinding.ActivityMapBinding
import com.palestine.roots.domain.model.Site
import com.palestine.roots.util.LocaleHelper
import com.palestine.roots.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding by lazy { ActivityMapBinding.inflate(layoutInflater) }
    private val viewModel: HomeViewModel by viewModels {
        val dao = PalestineDatabase.getInstance(this).siteDao()
        val repo = SiteRepositoryImpl(dao)
        val prefs = PreferencesManager(this)
        HomeViewModel.Factory(repo, prefs)
    }

    private var googleMap: GoogleMap? = null
    private val sites = mutableListOf<Site>()
    private var currentLang: String = "ar"

    companion object {
        private val JERUSALEM = LatLng(31.7683, 35.2137)
        private const val DEFAULT_ZOOM = 8f
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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.toolbarMap.setNavigationOnClickListener {
            finish()
        }

        binding.fabMyLocation.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(JERUSALEM, DEFAULT_ZOOM))
        }

        observeSites()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(JERUSALEM, DEFAULT_ZOOM))

        googleMap?.setOnInfoWindowClickListener { marker ->
            val siteId = marker.tag as? String
            if (siteId != null) {
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_SITE_ID, siteId)
                }
                startActivity(intent)
            }
        }

        addMarkers()
    }

    private fun observeSites() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state is HomeViewModel.UiState.Success) {
                    sites.clear()
                    sites.addAll(state.sites)
                    addMarkers()
                }
            }
        }
    }

    private fun addMarkers() {
        val map = googleMap ?: return
        map.clear()
        sites.forEach { site ->
            val position = LatLng(site.latitude, site.longitude)
            val title = if (currentLang == "en") site.nameEn else site.name
            val snippet = if (currentLang == "en") site.cityEn else site.city
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
            )
            marker?.tag = site.id
        }
    }
}
