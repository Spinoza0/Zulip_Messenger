package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.database.model.MessageDataDbModel
import com.spinoza.messenger_tfs.data.database.model.MessageDbModel
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel
import com.spinoza.messenger_tfs.data.utils.toDbModel

class MessengerDaoStub(private val messagesGenerator: MessagesGenerator, private val type: Type) :
    MessengerDao {

    override suspend fun getStreams(): List<StreamDbModel> {
        return emptyList()
    }

    override suspend fun getTopics(streamId: Long, isSubscribed: Boolean): List<TopicDbModel> {
        return emptyList()
    }

    override suspend fun insertTopics(topics: List<TopicDbModel>) {}

    override suspend fun insertStreams(streams: List<StreamDbModel>) {}

    override suspend fun removeTopics(streamId: Long, isSubscribed: Boolean) {}

    override suspend fun removeStreams(isSubscribed: Boolean) {}

    override suspend fun getMessages(): List<MessageDbModel> {
        return when (type) {
            Type.EMPTY -> emptyList()
            Type.WITH_MESSAGES -> messagesGenerator.getListOfMessagesDto().map { it.toDbModel() }
            Type.WITH_GET_MESSAGES_ERROR -> throw RuntimeException("getMessages test error")
        }
    }

    override suspend fun removeMessages() {}

    override suspend fun insertMessage(message: MessageDataDbModel) {}

    override suspend fun insertReaction(reaction: ReactionDbModel) {}

    enum class Type {
        EMPTY,
        WITH_MESSAGES,
        WITH_GET_MESSAGES_ERROR
    }
}