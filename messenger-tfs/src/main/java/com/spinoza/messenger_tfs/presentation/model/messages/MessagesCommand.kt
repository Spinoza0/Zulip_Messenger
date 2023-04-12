package com.spinoza.messenger_tfs.presentation.model.messages

import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class MessagesCommand {

    object GetMessagesEvent : MessagesCommand()

    object GetDeleteMessagesEvent : MessagesCommand()

    object GetReactionsEvent : MessagesCommand()

    class Load(val filter: MessagesFilter) : MessagesCommand()

    class SetMessagesRead(val messageIds: List<Long>) : MessagesCommand()

    class NewMessageText(val value: CharSequence?) : MessagesCommand()

    class SendMessage(val value: CharSequence?) : MessagesCommand()

    class UpdateReaction(val messageId: Long, val emoji: Emoji) : MessagesCommand()
}