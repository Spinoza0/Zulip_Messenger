package com.spinoza.messenger_tfs.presentation.model.messages

import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.ui.MessageView

sealed class MessagesEvent {

    sealed class Ui : MessagesEvent() {

        object Init : Ui()

        class Load(val filter: MessagesFilter) : Ui()

        object Exit : Ui()

        object AfterSubmitMessages : Ui()

        class SendMessage(val value: CharSequence?) : Ui()

        class UpdateReaction(val messageId: Long, val emoji: Emoji) : Ui()

        class NewMessageText(val value: CharSequence?) : Ui()

        class SetMessagesRead(val messageIds: List<Long>) : Ui()

        class ShowUserInfo(val message: MessageView) : Ui()

        class ShowChooseReactionDialog(val messageView: MessageView) : Ui()
    }

    sealed class Internal : MessagesEvent() {

        object MessageSent : Internal()

        class IconActionResId(val value: Int) : Internal()

        class Messages(val value: MessagesResultDelegate) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorMessages(val value: String) : Internal()
    }
}