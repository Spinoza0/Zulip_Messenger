package com.spinoza.messenger_tfs.data.database

import androidx.room.*
import com.spinoza.messenger_tfs.data.database.model.*

@Dao
interface MessengerDao {

    @Query("SELECT * FROM ${StreamDbModel.TABLE_STREAMS}")
    suspend fun getStreams(): List<StreamDbModel>

    @Query(
        "SELECT * FROM ${TopicDbModel.TABLE_TOPICS} " +
                "WHERE streamId = :streamId AND isSubscribed = :isSubscribed"
    )
    suspend fun getTopics(streamId: Long, isSubscribed: Boolean): List<TopicDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreams(streams: List<StreamDbModel>)

    @Query(
        "DELETE FROM ${TopicDbModel.TABLE_TOPICS} " +
                "WHERE streamId = :streamId AND isSubscribed = :isSubscribed"
    )
    suspend fun removeTopics(streamId: Long, isSubscribed: Boolean)

    @Query("DELETE FROM ${StreamDbModel.TABLE_STREAMS} WHERE isSubscribed = :isSubscribed")
    suspend fun removeStreams(isSubscribed: Boolean)

    @Transaction
    @Query("SELECT * FROM ${MessageDataDbModel.TABLE_MESSAGES}")
    suspend fun getMessages(): List<MessageDbModel>

    @Transaction
    suspend fun insertMessages(messages: List<MessageDbModel>) {
        messages.forEach { message ->
            insertMessage(message.message)
            message.reactions.forEach { insertReaction(it) }
        }
    }

    @Query("DELETE FROM ${MessageDataDbModel.TABLE_MESSAGES}")
    suspend fun removeMessages()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageDataDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: ReactionDbModel)
}