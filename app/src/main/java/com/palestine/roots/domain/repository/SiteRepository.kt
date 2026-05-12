package com.palestine.roots.domain.repository

import com.palestine.roots.domain.model.Site
import kotlinx.coroutines.flow.Flow

interface SiteRepository {

    fun getAllSites(): Flow<List<Site>>

    fun getSitesByCity(city: String): Flow<List<Site>>

    suspend fun getSiteById(siteId: String): Site?

    fun searchSites(query: String): Flow<List<Site>>

    fun getFavoriteSites(): Flow<List<Site>>

    suspend fun toggleFavorite(siteId: String, isFavorite: Boolean)
}
