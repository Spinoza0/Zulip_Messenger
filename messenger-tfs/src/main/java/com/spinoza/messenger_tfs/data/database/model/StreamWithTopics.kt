package com.spinoza.messenger_tfs.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class StreamWithTopics(
    @Embedded val stream: StreamDbModel,
    @Relation(
        parentColumn = StreamDbModel.COLUMN_ID,
        entityColumn = TopicDbModel.COLUMN_STREAM_ID
    )
    val topics: List<TopicDbModel>,
)