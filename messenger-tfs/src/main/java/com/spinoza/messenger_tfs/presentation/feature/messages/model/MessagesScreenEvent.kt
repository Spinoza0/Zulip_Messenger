package com.spinoza.messenger_tfs.presentation.feature.messages.model

import android.net.Uri
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView

sealed class MessagesScreenEvent {

    sealed class Ui : MessagesScreenEvent() {

        object Init : Ui()

        object Reload : Ui()

        class Load(val filter: MessagesFilter) : Ui()

        object Exit : Ui()

        object MessagesScrollStateIdle : Ui()

        object ScrollToLastMessage : Ui()

        class SendMessage(val value: CharSequence?) : Ui()

        class AfterSubmitMessages(
            val isNextMessageExisting: Boolean,
            val isLastMessageVisible: Boolean,
        ) : Ui()

        class MessagesOnScrolled(
            val canScrollUp: Boolean,
            val canScrollDown: Boolean,
            val visibleMessagesIds: List<Long>,
            val firstVisiblePosition: Int,
            val lastVisiblePosition: Int,
            val itemCount: Int,
            val dy: Int,
            val isNextMessageExisting: Boolean,
            val isLastMessageVisible: Boolean,
        ) : Ui()

        class UpdateReaction(val messageId: Long, val emoji: Emoji) : Ui()

        class NewMessageText(val value: CharSequence?) : Ui()

        class ShowUserInfo(val message: MessageView) : Ui()

        class OnMessageLongClick(val messageView: MessageView) : Ui()

        class ShowChooseReactionDialog(val messageView: MessageView) : Ui()

        class UploadFile(val message: CharSequence?, val uri: Uri) : Ui()

        class SaveAttachments(val urls: List<String>) : Ui()
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

        class FileUploaded(val newMessageText: String) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorMessages(val value: String) : Internal()
    }

    companion object {

        const val DIRECTION_UP = -1
        const val DIRECTION_DOWN = 1
    }
}