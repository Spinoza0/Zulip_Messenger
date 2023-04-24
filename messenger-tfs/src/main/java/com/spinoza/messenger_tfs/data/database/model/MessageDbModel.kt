package com.spinoza.messenger_tfs.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class MessageDbModel(
    @Embedded
    val message: MessageDataDbModel,
    @Relation(
        parentColumn = MessageDataDbModel.COLUMN_ID,
        entityColumn = ReactionDbModel.COLUMN_MESSAGE_ID
    )
    val reactions: List<ReactionDbModel>,
)