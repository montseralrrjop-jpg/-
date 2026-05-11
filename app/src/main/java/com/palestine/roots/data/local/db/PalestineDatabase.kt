package com.palestine.roots.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.palestine.roots.data.local.dao.SiteDao
import com.palestine.roots.data.local.entity.SiteEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(entities = [SiteEntity::class], version = 3, exportSchema = false)
abstract class PalestineDatabase : RoomDatabase() {

    abstract fun siteDao(): SiteDao

    companion object {
        @Volatile
        private var INSTANCE: PalestineDatabase? = null

        fun getInstance(context: Context): PalestineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PalestineDatabase::class.java,
                    "palestine_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PopulateCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    class PopulateCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateDatabase()
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Also check on open to ensure data is populated even if onCreate was missed
            CoroutineScope(Dispatchers.IO).launch {
                ensureDataPopulated()
            }
        }

        private suspend fun populateDatabase() {
            try {
                val dao = getInstance(context).siteDao()
                val inputStream = context.assets.open("palestine_sites.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                val json = String(buffer, Charsets.UTF_8)

                val type = object : TypeToken<List<SiteEntity>>() {}.type
                val sites: List<SiteEntity> = Gson().fromJson(json, type)
                dao.insertSites(sites)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private suspend fun ensureDataPopulated() {
            try {
                val dao = getInstance(context).siteDao()
                // Check if database is empty
                val count = dao.getSiteCount()
                if (count == 0) {
                    // Database is empty, populate it
                    val inputStream = context.assets.open("palestine_sites.json")
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()
                    val json = String(buffer, Charsets.UTF_8)

                    val type = object : TypeToken<List<SiteEntity>>() {}.type
                    val sites: List<SiteEntity> = Gson().fromJson(json, type)
                    dao.insertSites(sites)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
