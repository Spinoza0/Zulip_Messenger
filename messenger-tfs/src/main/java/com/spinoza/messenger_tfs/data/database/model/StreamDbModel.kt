package com.spinoza.messenger_tfs.data.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel.Companion.COLUMN_IS_SUBSCRIBED
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel.Companion.COLUMN_STREAM_ID
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel.Companion.TABLE_STREAMS

@Entity(
    tableName = TABLE_STREAMS,
    indices = [Index(value = [COLUMN_STREAM_ID, COLUMN_IS_SUBSCRIBED], unique = true)]
)
data class StreamDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = DEFAULT_ID,
    val streamId: Long,
    val name: String,
    val isSubscribed: Boolean,
) {

    companion object {

        const val TABLE_STREAMS = "streams"
        const val COLUMN_ID = "id"
        const val COLUMN_STREAM_ID = "streamId"
        const val COLUMN_IS_SUBSCRIBED = "isSubscribed"
        private const val DEFAULT_ID = 0
    }
}