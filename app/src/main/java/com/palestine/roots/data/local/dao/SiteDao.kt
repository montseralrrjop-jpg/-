package com.palestine.roots.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.palestine.roots.data.local.entity.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {

    @Query("SELECT * FROM sites ORDER BY name ASC")
    fun getAllSites(): Flow<List<SiteEntity>>

    @Query("SELECT * FROM sites WHERE city LIKE :cityQuery ORDER BY name ASC")
    fun getSitesByCity(cityQuery: String): Flow<List<SiteEntity>>

    @Query("SELECT * FROM sites WHERE id = :siteId")
    suspend fun getSiteById(siteId: String): SiteEntity?

    @Query("SELECT * FROM sites WHERE name LIKE :query OR name_en LIKE :query OR city LIKE :query OR city_en LIKE :query OR description LIKE :query OR description_en LIKE :query OR category LIKE :query OR category_en LIKE :query")
    fun searchSites(query: String): Flow<List<SiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSites(sites: List<SiteEntity>)

    @Query("UPDATE sites SET is_favorite = :isFavorite WHERE id = :siteId")
    suspend fun updateFavoriteStatus(siteId: String, isFavorite: Boolean)

    @Query("SELECT * FROM sites WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteSites(): Flow<List<SiteEntity>>

    @Query("SELECT COUNT(*) FROM sites")
    suspend fun getSiteCount(): Int
}
