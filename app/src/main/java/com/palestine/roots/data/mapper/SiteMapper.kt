package com.palestine.roots.data.mapper

import com.palestine.roots.data.local.entity.SiteEntity
import com.palestine.roots.domain.model.Site

fun SiteEntity.toDomainModel(): Site {
    return Site(
        id = id,
        name = name,
        nameEn = nameEn,
        city = city,
        cityEn = cityEn,
        description = description,
        descriptionEn = descriptionEn,
        history = history,
        historyEn = historyEn,
        imageUrl = imageUrl,
        latitude = latitude,
        longitude = longitude,
        category = category,
        categoryEn = categoryEn,
        foundationYear = foundationYear,
        isFavorite = isFavorite
    )
}

fun Site.toEntity(): SiteEntity {
    return SiteEntity(
        id = id,
        name = name,
        nameEn = nameEn,
        city = city,
        cityEn = cityEn,
        description = description,
        descriptionEn = descriptionEn,
        history = history,
        historyEn = historyEn,
        imageUrl = imageUrl,
        latitude = latitude,
        longitude = longitude,
        category = category,
        categoryEn = categoryEn,
        foundationYear = foundationYear,
        isFavorite = isFavorite
    )
}
