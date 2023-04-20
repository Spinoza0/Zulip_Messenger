package com.spinoza.messenger_tfs.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel.Companion.TABLE_STREAMS

@Entity(tableName = TABLE_STREAMS)
data class StreamDbModel(
    @PrimaryKey
    val streamId: Long,
    val name: String,
) {

    companion object {

        const val TABLE_STREAMS = "streams"
        const val RELATION_KEY = "streamId"
    }
}