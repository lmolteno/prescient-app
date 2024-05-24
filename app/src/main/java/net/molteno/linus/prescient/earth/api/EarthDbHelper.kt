package net.molteno.linus.prescient.earth.api

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

private const val DATABASE_NAME = "assets/world-land-areas-110-million.gpkg"
private const val DATABASE_VERSION = 1

class EarthDb(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        // nothing yet - db is static
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // nothing yet - db is static
    }

    fun getCoastlines() {
        val cursor = this.readableDatabase.query("world_land_areas_110_million", null, null, null, null, null, null)

//        val itemIds = mutableListOf<Long>()
        with(cursor) {
            while (moveToNext()) {
//                Timber.d(getBlob(getColumnIndexOrThrow("geom")).toString())
                Timber.d(getInt(getColumnIndexOrThrow("ScaleRank")).toString())
//                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
//                itemIds.add(itemId)
            }
        }

        cursor.close()
    }
}

@Module
@InstallIn(SingletonComponent::class)
class EarthDbModule {
    @Provides
    @Singleton
    fun provideEarthDb(@ApplicationContext appContext: Context): EarthDb {
        return EarthDb(appContext)
    }
}