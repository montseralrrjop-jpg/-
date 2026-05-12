package com.palestine.roots.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.palestine.roots.data.local.PreferencesManager
import com.palestine.roots.domain.model.Site
import com.palestine.roots.domain.repository.SiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SiteRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isDarkMode = preferencesManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val language = preferencesManager.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ar")

    val isOnboardingCompleted = preferencesManager.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val favoriteSites = repository.getFavoriteSites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Province with both Arabic and English names stored together. */
    data class Province(val ar: String, val en: String)

    val provinces = listOf(
        Province("القدس", "Jerusalem"),
        Province("رام الله والبيرة", "Ramallah & Al-Bireh"),
        Province("نابلس", "Nablus"),
        Province("جنين", "Jenin"),
        Province("طولكرم", "Tulkarem"),
        Province("قلقيلية", "Qalqilya"),
        Province("سلفيت", "Salfit"),
        Province("أريحا والأغوار", "Jericho & Jordan Valley"),
        Province("الخليل", "Hebron"),
        Province("بيت لحم", "Bethlehem")
    )

    /** Return province names in the requested language. */
    fun getProvinceNames(lang: String): List<String> {
        return provinces.map { if (lang == "en") it.en else it.ar }
    }

    /**
     * Given a display name (could be Arabic or English),
     * always return the Arabic name because the database
     * `city` column stores Arabic values.
     */
    fun resolveArabicCityName(displayName: String): String {
        return provinces.find { it.ar == displayName || it.en == displayName }?.ar
            ?: displayName
    }

    private var currentCity: String? = null

    init {
        loadAllSites()
    }

    fun loadAllSites() {
        currentCity = null
        viewModelScope.launch {
            try {
                repository.getAllSites().collect { sites ->
                    _uiState.value = UiState.Success(sites)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadAllSites()
        } else {
            viewModelScope.launch {
                try {
                    repository.searchSites(query).collect { sites ->
                        _uiState.value = UiState.Success(sites)
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun filterByCity(city: String) {
        currentCity = city
        _searchQuery.value = ""
        viewModelScope.launch {
            try {
                repository.getSitesByCity(city).collect { sites ->
                    _uiState.value = UiState.Success(sites)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavorite(siteId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(siteId, isFavorite)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.toggleDarkMode(enabled)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setLanguage(lang)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted()
        }
    }

    suspend fun getSiteById(siteId: String): Site? {
        return repository.getSiteById(siteId)
    }

    sealed class UiState {
        object Loading : UiState()
        data class Success(val sites: List<Site>) : UiState()
        data class Error(val message: String) : UiState()
    }

    class Factory(
        private val repository: SiteRepository,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository, preferencesManager) as T
        }
    }
}
