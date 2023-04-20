package com.spinoza.messenger_tfs.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel.Companion.TABLE_TOPICS

@Entity(
    tableName = TABLE_TOPICS,
    foreignKeys = [ForeignKey(
        entity = StreamDbModel::class,
        parentColumns = [StreamDbModel.COLUMN_ID],
        childColumns = [TopicDbModel.COLUMN_STREAM_ID]
    )]
)
data class TopicDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = DEFAULT_ID,
    val name: String,
    @ColumnInfo(name = COLUMN_STREAM_ID, index = true)
    val streamId: Long,
    val isSubscribed: Boolean,
) {

    companion object {

        const val TABLE_TOPICS = "topics"
        const val COLUMN_STREAM_ID = "streamId"
        private const val DEFAULT_ID = 0
    }
}