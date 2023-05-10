package com.spinoza.messenger_tfs.presentation.feature.messages.model

import android.content.Context
import android.net.Uri
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.UploadedFileInfo
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView

sealed class MessagesScreenEvent {

    sealed class Ui : MessagesScreenEvent() {

        object Init : Ui()

        object CheckLoginStatus : Ui()

        object Reload : Ui()

        class Load(val filter: MessagesFilter) : Ui()

        object Exit : Ui()

        object LoadPreviousPage : Ui()

        object LoadNextPage : Ui()

        object ScrollToLastMessage : Ui()

        class MessagesOnScrolled(
            val visibleMessagesIds: List<Long>,
            val isNextMessageExisting: Boolean,
            val isLastMessageVisible: Boolean,
        ) : Ui()

        class MessagesScrollStateIdle(
            val canScrollUp: Boolean,
            val canScrollDown: Boolean,
            val isNextMessageExisting: Boolean,
        ) : Ui()

        object MessagesScrollStateDragging : Ui()

        class SendMessage(val value: CharSequence?) : Ui()

        class AfterSubmitMessages(
            val isNextMessageExisting: Boolean,
            val isLastMessageVisible: Boolean,
        ) : Ui()

        class UpdateReaction(val messageId: Long, val emoji: Emoji) : Ui()

        class NewMessageText(val value: CharSequence?) : Ui()

        class ShowUserInfo(val message: MessageView) : Ui()

        class ShowChooseActionMenu(val messageView: MessageView) : Ui()

        class GetRawMessageContent(
            val messageView: MessageView,
            val isMessageWithAttachments: Boolean,
        ) : Ui()

        class CopyToClipboard(
            val context: Context, val messageView: MessageView,
            val isMessageWithAttachments: Boolean,
        ) : Ui()

        class EditMessageContent(val messageId: Long, val content: CharSequence) : Ui()

        class EditMessageTopic(val messageId: Long, val topic: CharSequence) : Ui()

        class ShowChooseReactionDialog(val messageView: MessageView) : Ui()

        class UploadFile(val context: Context, val uri: Uri) : Ui()

        class SaveAttachments(val context: Context, val urls: List<String>) : Ui()

        class ConfirmDeleteMessage(val messageView: MessageView) : Ui()

        class DeleteMessage(val messageId: Long) : Ui()
    }

    sealed class Internal : MessagesScreenEvent() {

        object Idle : Internal()

        object LoginSuccess : Internal()

        object LogOut : Internal()

        object EmptyMessagesQueueEvent : Internal()

        object EmptyUpdateMessagesQueueEvent : Internal()

        object EmptyDeleteMessagesQueueEvent : Internal()

        object EmptyReactionsQueueEvent : Internal()

        class NextPageExists(val value: Boolean, val isGoingToLastMessage: Boolean) : Internal()

        class IconActionResId(val value: Int) : Internal()

        class Messages(val value: MessagesResultDelegate) : Internal()

        class StoredMessages(val value: MessagesResultDelegate) : Internal()

        class MessageSent(val messageId: Long) : Internal()

        class MessagesEventFromQueue(val value: MessagesResultDelegate) : Internal()

        class UpdateMessagesEventFromQueue(val value: MessagesResultDelegate) : Internal()

        class DeleteMessagesEventFromQueue(val value: MessagesResultDelegate) : Internal()

        class ReactionsEventFromQueue(val value: MessagesResultDelegate) : Internal()

        class FileUploaded(val value: UploadedFileInfo) : Internal()

        class FilesDownloaded(val value: Map<String, Boolean>) : Internal()

        class RawMessageContent(val messageId: Long, val content: String) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorMessages(val value: String) : Internal()
    }
}