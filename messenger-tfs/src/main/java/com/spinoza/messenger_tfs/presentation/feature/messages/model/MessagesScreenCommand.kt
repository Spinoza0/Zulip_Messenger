package com.spinoza.messenger_tfs.presentation.feature.messages.model

import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class MessagesScreenCommand {

    object Reload : MessagesScreenCommand()

    class GetMessagesEvent(val isLastMessageVisible: Boolean) : MessagesScreenCommand()

    class GetDeleteMessagesEvent(val isLastMessageVisible: Boolean) : MessagesScreenCommand()

    class GetReactionsEvent(val isLastMessageVisible: Boolean) : MessagesScreenCommand()

    class Load(val filter: MessagesFilter) : MessagesScreenCommand()

    object LoadPreviousPage : MessagesScreenCommand()

    object LoadNextPage : MessagesScreenCommand()

    object LoadLastPage : MessagesScreenCommand()

    class IsNextPageExisting(val messagesResultDelegate: MessagesResultDelegate) :
        MessagesScreenCommand()

    class SetMessagesRead(val messageIds: List<Long>) : MessagesScreenCommand()

    class NewMessageText(val value: CharSequence?) : MessagesScreenCommand()

    class SendMessage(val value: String) : MessagesScreenCommand()

    class UpdateReaction(val messageId: Long, val emoji: Emoji) : MessagesScreenCommand()
}