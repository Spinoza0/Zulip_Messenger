package com.spinoza.messenger_tfs.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class StreamsWithTopics(
    @Embedded val stream: StreamDbModel,
    @Relation(
        parentColumn = StreamDbModel.RELATION_KEY,
        entityColumn = TopicDbModel.RELATION_KEY
    )
    val topics: List<TopicDbModel>,
)