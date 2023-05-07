package com.spinoza.messenger_tfs.presentation.feature.messages.model

import com.spinoza.messenger_tfs.domain.model.UploadedFileInfo
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView

sealed class MessagesScreenEffect {

    object MessageSent : MessagesScreenEffect()

    object ScrollToLastMessage : MessagesScreenEffect()

    object AddAttachment : MessagesScreenEffect()

    class ShowMessageMenu(
        val isDeleteMessageVisible: Boolean,
        val isEditMessageVisible: Boolean,
        val urls: List<String>,
        val messageView: MessageView,
    ) : MessagesScreenEffect()

    class ShowChooseReactionDialog(val messageId: Long) : MessagesScreenEffect()

    class FileUploaded(val value: UploadedFileInfo) : MessagesScreenEffect()

    class FilesDownloaded(val value: Map<String, Boolean>) : MessagesScreenEffect()

    class ConfirmDeleteMessage(val messageId: Long) : MessagesScreenEffect()

    class RawMessageContent(val messageId: Long, val content: String) : MessagesScreenEffect()

    sealed class Failure : MessagesScreenEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorMessages(val value: String) : Failure()
    }
}