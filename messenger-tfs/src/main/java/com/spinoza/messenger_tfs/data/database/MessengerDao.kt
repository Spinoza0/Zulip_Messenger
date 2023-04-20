package com.spinoza.messenger_tfs.data.database

import androidx.room.*
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel

@Dao
interface MessengerDao {

    @Query("SELECT * FROM ${StreamDbModel.TABLE_STREAMS}")
    fun getStreams(): List<StreamDbModel>

    @Query(
        "SELECT * FROM ${TopicDbModel.TABLE_TOPICS} " +
                "WHERE streamId = :streamId AND isSubscribed = :isSubscribed"
    )
    fun getTopics(streamId: Long, isSubscribed: Boolean): List<TopicDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicDbModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreams(streams: List<StreamDbModel>)

    @Query(
        "DELETE FROM ${TopicDbModel.TABLE_TOPICS} " +
                "WHERE streamId = :streamId AND isSubscribed = :isSubscribed"
    )
    suspend fun removeTopics(streamId: Long, isSubscribed: Boolean)

    @Query("DELETE FROM ${TopicDbModel.TABLE_TOPICS} WHERE isSubscribed = :isSubscribed")
    suspend fun removeTopics(isSubscribed: Boolean)

    @Query("DELETE FROM ${StreamDbModel.TABLE_STREAMS} WHERE isSubscribed = :isSubscribed")
    suspend fun removeStreams(isSubscribed: Boolean)
}