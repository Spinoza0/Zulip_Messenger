package com.spinoza.messenger_tfs.presentation.feature.messages.model

import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView

sealed class MessagesScreenEffect {

    object MessageSent : MessagesScreenEffect()

    object ScrollToLastMessage : MessagesScreenEffect()

    object AddAttachment : MessagesScreenEffect()

    class ShowMessageMenu(val urls: List<String>, val messageView: MessageView) :
        MessagesScreenEffect()

    class ShowChooseReactionDialog(val messageId: Long) : MessagesScreenEffect()

    class FileUploaded(val value: Pair<String, String>) : MessagesScreenEffect()

    class FilesDownloaded(val value: Map<String, Boolean>) : MessagesScreenEffect()

    sealed class Failure : MessagesScreenEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorMessages(val value: String) : Failure()
    }
}