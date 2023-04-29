package com.spinoza.messenger_tfs.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel.Companion.COLUMN_IS_SUBSCRIBED
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel.Companion.COLUMN_NAME
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel.Companion.COLUMN_STREAM_ID
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel.Companion.TABLE_TOPICS

@Entity(
    tableName = TABLE_TOPICS,
    indices = [Index(value = [COLUMN_NAME, COLUMN_STREAM_ID, COLUMN_IS_SUBSCRIBED], unique = true)]
)
data class TopicDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = DEFAULT_ID,
    @ColumnInfo(name = COLUMN_NAME)
    val name: String,
    @ColumnInfo(name = COLUMN_STREAM_ID)
    val streamId: Long,
    @ColumnInfo(name = COLUMN_IS_SUBSCRIBED)
    val isSubscribed: Boolean,
) {

    companion object {

        const val TABLE_TOPICS = "topics"
        const val COLUMN_NAME = "name"
        const val COLUMN_STREAM_ID = "streamId"
        const val COLUMN_IS_SUBSCRIBED = "isSubscribed"
        private const val DEFAULT_ID = 0
    }
}