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
        parentColumns = [StreamDbModel.RELATION_KEY],
        childColumns = [TopicDbModel.RELATION_KEY]
    )]
)
data class TopicDbModel(
    @PrimaryKey
    val id: Long,
    val name: String,
    @ColumnInfo(name = RELATION_KEY, index = true)
    val streamId: Long,
) {

    companion object {

        const val TABLE_TOPICS = "topics"
        const val RELATION_KEY = "streamId"
    }
}