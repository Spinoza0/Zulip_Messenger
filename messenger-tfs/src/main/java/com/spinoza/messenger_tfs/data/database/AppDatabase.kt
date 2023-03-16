package com.spinoza.messenger_tfs.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spinoza.messenger_tfs.data.model.StreamDbModel

@Database(entities = [StreamDbModel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private const val DATABASE_NAME = "messenger-tfs.db"
        private var db: AppDatabase? = null
        private val LOCK = Any()

        fun getInstance(context: Context): AppDatabase {
            synchronized(LOCK) {
                db?.let { return it }
                val instance =
                    Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
                db = instance
                return instance
            }
        }
    }

    abstract fun dao(): MessengerDao
}