package com.spinoza.messenger_tfs.presentation.feature.messages.model

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView

sealed class MessagesScreenEvent {

    sealed class Ui : MessagesScreenEvent() {

        object Init : Ui()

        object Reload : Ui()

        class Load(val recyclerView: RecyclerView, val filter: MessagesFilter) : Ui()

        object Exit : Ui()

        object AfterSubmitMessages : Ui()

        object MessagesScrollStateIdle : Ui()

        object ScrollToLastMessage : Ui()

        class SendMessage(val value: CharSequence?) : Ui()

        class MessagesOnScrolled(val dy: Int) : Ui()

        class UpdateReaction(val messageId: Long, val emoji: Emoji) : Ui()

        class NewMessageText(val value: CharSequence?) : Ui()

        class ShowUserInfo(val message: MessageView) : Ui()

        class ShowChooseReactionDialog(val messageView: MessageView) : Ui()
    }

    sealed class Internal : MessagesScreenEvent() {

        object Idle : Internal()

        object EmptyMessagesQueueEvent : Internal()

        object EmptyDeleteMessagesQueueEvent : Internal()

        object EmptyReactionsQueueEvent : Internal()

        class NextPageExists(val value: Boolean, val isGoingToLastMessage: Boolean) : Internal()

        class IconActionResId(val value: Int) : Internal()

        class Messages(val value: MessagesResultDelegate) : Internal()

        class StoredMessages(val value: MessagesResultDelegate) : Internal()

        class MessageSent(val value: MessagesResultDelegate) : Internal()

        class MessagesEventFromQueue(val value: MessagesResultDelegate) : Internal()

        class DeleteMessagesEventFromQueue(val value: MessagesResultDelegate) : Internal()

        class ReactionsEventFromQueue(val value: MessagesResultDelegate) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorMessages(val value: String) : Internal()
    }
}