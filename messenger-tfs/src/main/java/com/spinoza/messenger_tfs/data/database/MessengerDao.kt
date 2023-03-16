package com.spinoza.messenger_tfs.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spinoza.messenger_tfs.data.model.StreamDbModel
import com.spinoza.messenger_tfs.data.model.StreamDbModel.Companion.TABLE_SUBSCRIBED_STREAMS

@Dao
interface MessengerDao {

    @Query("SELECT * FROM $TABLE_SUBSCRIBED_STREAMS WHERE streamId IN (:streamIds)")
    fun getStreamsListByIds(streamIds: List<Long>): List<StreamDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: StreamDbModel)

    @Query("DELETE FROM $TABLE_SUBSCRIBED_STREAMS WHERE streamId=:streamId")
    suspend fun removeStream(streamId: Long)
}