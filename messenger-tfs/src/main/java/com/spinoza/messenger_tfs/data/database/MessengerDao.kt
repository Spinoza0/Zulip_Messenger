package com.spinoza.messenger_tfs.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spinoza.messenger_tfs.data.model.ChannelDbModel
import com.spinoza.messenger_tfs.data.model.ChannelDbModel.Companion.TABLE_SUBSCRIPTION

@Dao
interface MessengerDao {

    @Query("SELECT * FROM $TABLE_SUBSCRIPTION WHERE streamId IN (:streamIds)")
    fun getStreamsListByIds(streamIds: List<Long>): List<ChannelDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: ChannelDbModel)

    @Query("DELETE FROM $TABLE_SUBSCRIPTION WHERE streamId=:streamId")
    suspend fun removeStream(streamId: Long)
}