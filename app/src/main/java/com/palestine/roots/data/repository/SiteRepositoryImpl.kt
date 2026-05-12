package com.palestine.roots.data.repository

import com.palestine.roots.data.local.dao.SiteDao
import com.palestine.roots.data.mapper.toDomainModel
import com.palestine.roots.domain.model.Site
import com.palestine.roots.domain.repository.SiteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SiteRepositoryImpl(
    private val siteDao: SiteDao
) : SiteRepository {

    override fun getAllSites(): Flow<List<Site>> {
        return siteDao.getAllSites().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getSitesByCity(city: String): Flow<List<Site>> {
        return siteDao.getSitesByCity("%$city%").map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getSiteById(siteId: String): Site? {
        return siteDao.getSiteById(siteId)?.toDomainModel()
    }

    override fun searchSites(query: String): Flow<List<Site>> {
        return siteDao.searchSites("%$query%").map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFavoriteSites(): Flow<List<Site>> {
        return siteDao.getFavoriteSites().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun toggleFavorite(siteId: String, isFavorite: Boolean) {
        siteDao.updateFavoriteStatus(siteId, isFavorite)
    }
}
