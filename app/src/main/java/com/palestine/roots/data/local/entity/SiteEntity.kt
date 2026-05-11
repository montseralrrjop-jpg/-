package com.palestine.roots.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "sites")
data class SiteEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @ColumnInfo(name = "name_en")
    @SerializedName("name_en")
    val nameEn: String,
    
    @SerializedName("city")
    val city: String,
    
    @ColumnInfo(name = "city_en")
    @SerializedName("city_en")
    val cityEn: String,
    
    @SerializedName("description")
    val description: String,
    
    @ColumnInfo(name = "description_en")
    @SerializedName("description_en")
    val descriptionEn: String,
    
    @SerializedName("history")
    val history: String,
    
    @ColumnInfo(name = "history_en")
    @SerializedName("history_en")
    val historyEn: String,
    
    @ColumnInfo(name = "image_url")
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("category")
    val category: String,
    
    @ColumnInfo(name = "category_en")
    @SerializedName("category_en")
    val categoryEn: String,
    
    @ColumnInfo(name = "foundation_year")
    @SerializedName("foundation_year")
    val foundationYear: String?,
    
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    @SerializedName("is_favorite")
    val isFavorite: Boolean = false
)
