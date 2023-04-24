package com.spinoza.messenger_tfs.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spinoza.messenger_tfs.data.database.model.MessageDataDbModel
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel

@Database(
    entities = [
        StreamDbModel::class, TopicDbModel::class, MessageDataDbModel::class, ReactionDbModel::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MessengerDatabase : RoomDatabase() {

    companion object {

        @Volatile
        private var db: MessengerDatabase? = null
        private val lock = Any()
        private const val DATABASE_NAME = "messenger-tfs-cache.db"

        fun getInstance(context: Context): MessengerDatabase {
            synchronized(lock) {
                db?.let { return it }
                return Room.databaseBuilder(context, MessengerDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { db = it }
            }
        }
    }

    abstract fun dao(): MessengerDao
}