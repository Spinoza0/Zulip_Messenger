package com.spinoza.messenger_tfs.data.database

import androidx.room.Database
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

    abstract fun dao(): MessengerDao
}