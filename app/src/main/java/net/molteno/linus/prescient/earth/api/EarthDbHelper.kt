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
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton


private const val DATABASE_NAME = "world-land-areas-110-million.gpkg"
private const val DATABASE_VERSION = 10200 // somehow in the gpkg
private const val DB_PATH = "/databases/"


class EarthDb(private val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    init {
        try {
            val myPath = context.dataDir.path + DB_PATH + DATABASE_NAME
//            File(myPath).delete()
//            File(myPath + "-journal").delete()
//            val files = File(context.dataDir.path + DB_PATH).listFiles()
            SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)?.close()
            Timber.d("Found database in data dir")
        } catch (e: Exception) {
            Timber.d("Copying database from assets")
            copyFromAssets()
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // nothing yet - db is static
    }

    fun getCoastlines() {
        val cursor = this.readableDatabase.query("world_land_areas_110_million", null, null, null, null, null, null)
        with(cursor) {
            while (moveToNext()) {
                val geom = getBlob(getColumnIndexOrThrow("geom"))
                geom?.parse()
            }
        }
        cursor.close()
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Timber.d("funky versioning")
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring byte stream.
     */
    @Throws(IOException::class)
    private fun copyFromAssets() {
        //Open your local db as the input stream

        val myInput: InputStream = context.assets.open(DATABASE_NAME)

        // Path to the just created empty db
        val outFileName: String = context.dataDir.path + DB_PATH + DATABASE_NAME

        //Open the empty db as the output stream
        val myOutput: OutputStream = FileOutputStream(outFileName)

        //transfer bytes from the input file to the output file
        val buffer = ByteArray(1024)
        var length: Int
        while ((myInput.read(buffer).also { length = it }) > 0) {
            myOutput.write(buffer, 0, length)
        }

        //Close the streams
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }
}

fun ByteArray.parse() {
//    val geopackage = this.;
    val bigEndian = this[0] == 0.toByte()
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