package com.spinoza.messenger_tfs.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spinoza.messenger_tfs.data.model.StreamDbModel.Companion.TABLE_SUBSCRIBED_STREAMS

@Entity(tableName = TABLE_SUBSCRIBED_STREAMS)
data class StreamDbModel(
    @PrimaryKey
    val streamId: Long,
) {
    companion object {
        const val TABLE_SUBSCRIBED_STREAMS = "subscribed_streams"
    }
}
