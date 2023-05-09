package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.database.model.MessageDataDbModel
import com.spinoza.messenger_tfs.data.database.model.MessageDbModel
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel

class MessengerDaoStub : MessengerDao {

    override suspend fun getStreams(): List<StreamDbModel> = emptyList()

    override suspend fun getTopics(streamId: Long, isSubscribed: Boolean): List<TopicDbModel> =
        emptyList()

    override suspend fun insertTopics(topics: List<TopicDbModel>) {}

    override suspend fun insertStreams(streams: List<StreamDbModel>) {}

    override suspend fun removeTopics(streamId: Long, isSubscribed: Boolean) {}

    override suspend fun removeStreams(isSubscribed: Boolean) {}

    override suspend fun getMessages(): List<MessageDbModel> = emptyList()

    override suspend fun removeMessages() {}

    override suspend fun insertMessage(message: MessageDataDbModel) {}

    override suspend fun insertReaction(reaction: ReactionDbModel) {}
}