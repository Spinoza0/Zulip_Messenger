package com.spinoza.messenger_tfs.presentation.model.messages

import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.presentation.ui.MessageView

sealed class MessagesEvent {

    sealed class Ui : MessagesEvent() {

        object Load : Ui()

        object Exit : Ui()

        object AfterSubmitMessages : Ui()

        class SendMessage(val value: CharSequence?) : Ui()

        class UpdateReaction(val messageId: Long, val emoji: Emoji) : Ui()

        class NewMessageText(val value: CharSequence?) : Ui()

        class SetMessagesRead(val messageIds: List<Long>) : Ui()

        class ShowUserInfo(val message: MessageView) : Ui()
    }
}