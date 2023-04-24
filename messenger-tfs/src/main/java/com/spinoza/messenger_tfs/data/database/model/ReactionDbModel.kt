package com.spinoza.messenger_tfs.data.database.model

import androidx.room.*
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel.Companion.COLUMN_EMOJI_CODE
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel.Companion.COLUMN_MESSAGE_ID
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel.Companion.COLUMN_USER_ID
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel.Companion.TABLE_REACTIONS


@Entity(
    tableName = TABLE_REACTIONS,
    foreignKeys = [ForeignKey(
        entity = MessageDataDbModel::class,
        parentColumns = [MessageDataDbModel.COLUMN_ID],
        childColumns = [COLUMN_MESSAGE_ID],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(
        value = [COLUMN_MESSAGE_ID, COLUMN_EMOJI_CODE, COLUMN_USER_ID],
        unique = true
    )]
)
data class ReactionDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = DEFAULT_ID,
    val emojiName: String,
    @ColumnInfo(name = COLUMN_EMOJI_CODE)
    val emojiCode: String,
    val reactionType: String,
    @ColumnInfo(name = COLUMN_USER_ID)
    val userId: Long,
    @ColumnInfo(name = COLUMN_MESSAGE_ID)
    val messageId: Long,
) {


    companion object {

        const val TABLE_REACTIONS = "reactions"
        const val COLUMN_EMOJI_CODE = "emojiCode"
        const val COLUMN_USER_ID = "userId"
        const val COLUMN_MESSAGE_ID = "messageId"
        private const val DEFAULT_ID = 0
    }
}