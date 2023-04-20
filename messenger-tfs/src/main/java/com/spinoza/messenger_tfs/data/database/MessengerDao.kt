package com.spinoza.messenger_tfs.data.database

import androidx.room.*
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel
import com.spinoza.messenger_tfs.data.database.model.StreamsWithTopics

@Dao
interface MessengerDao {

    @Transaction
    @Query("SELECT * FROM ${StreamDbModel.TABLE_STREAMS}")
    fun getStreamsWithTopics(): List<StreamsWithTopics>

//    @Query("SELECT * FROM $TABLE_SUBSCRIPTION WHERE streamId IN (:streamIds)")
//    fun getStreamsListByIds(streamIds: List<Long>): List<ChannelDbModel>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertStream(stream: ChannelDbModel)
//
//    @Query("DELETE FROM $TABLE_SUBSCRIPTION WHERE streamId=:streamId")
//    suspend fun removeStream(streamId: Long)
}