package com.spinoza.messenger_tfs.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spinoza.messenger_tfs.data.model.ChannelDbModel.Companion.TABLE_SUBSCRIPTION

@Entity(tableName = TABLE_SUBSCRIPTION)
data class ChannelDbModel(
    @PrimaryKey
    val streamId: Long,
) {
    companion object {
        const val TABLE_SUBSCRIPTION = "subscription"
    }
}
