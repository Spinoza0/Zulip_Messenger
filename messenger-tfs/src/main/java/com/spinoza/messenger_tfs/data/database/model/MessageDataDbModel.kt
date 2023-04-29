package com.spinoza.messenger_tfs.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spinoza.messenger_tfs.data.database.model.MessageDataDbModel.Companion.TABLE_MESSAGES

@Entity(tableName = TABLE_MESSAGES)
data class MessageDataDbModel(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    val id: Long,
    val streamId: Long,
    val senderId: Long,
    val content: String,
    val recipientId: Long,
    val timestamp: Long,
    val subject: String,
    val isMeMessage: Boolean,
    val senderFullName: String,
    val senderEmail: String,
    val avatarUrl: String
) {

    companion object {

        const val TABLE_MESSAGES = "messages"
        const val COLUMN_ID = "id"
    }
}