package com.spinoza.messenger_tfs.presentation.elm

import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesCommand
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesEffect
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesEvent
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class MessagesReducer :
    ScreenDslReducer<MessagesEvent, MessagesEvent.Ui, MessagesEvent.Internal, MessagesState, MessagesEffect, MessagesCommand>(
        MessagesEvent.Ui::class, MessagesEvent.Internal::class
    ) {

    private val router = GlobalDI.INSTANCE.globalRouter

    override fun Result.internal(event: MessagesEvent.Internal) = when (event) {
        is MessagesEvent.Internal.Messages ->
            state { copy(isLoading = false, messages = event.value) }
        is MessagesEvent.Internal.MessageSent -> {
            state { copy(isSendingMessage = false) }
            effects { +MessagesEffect.MessageSent }
        }
        is MessagesEvent.Internal.IconActionResId -> state { copy(iconActionResId = event.value) }
        is MessagesEvent.Internal.ErrorMessages -> {
            state { copy(isLoading = false, isSendingMessage = false) }
            effects { +MessagesEffect.Failure.ErrorMessages(event.value) }
        }
        is MessagesEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false, isSendingMessage = false) }
            effects { +MessagesEffect.Failure.ErrorNetwork(event.value) }
        }
    }

    override fun Result.ui(event: MessagesEvent.Ui) = when (event) {
        is MessagesEvent.Ui.NewMessageText -> {
            commands { +MessagesCommand.NewMessageText(event.value) }
        }
        is MessagesEvent.Ui.Load -> {
            state { copy(isLoading = true) }
            commands { +MessagesCommand.Load(event.filter) }
        }
        is MessagesEvent.Ui.SendMessage -> {
            state { copy(isSendingMessage = true) }
            commands { +MessagesCommand.SendMessage(event.value) }
        }
        is MessagesEvent.Ui.ShowUserInfo ->
            router.navigateTo(Screens.UserProfile(event.message.userId))
        is MessagesEvent.Ui.Exit -> router.exit()
        is MessagesEvent.Ui.UpdateReaction ->
            commands { +MessagesCommand.UpdateReaction(event.messageId, event.emoji) }
        is MessagesEvent.Ui.AfterSubmitMessages ->
            state.messages?.let { messages ->
                state {
                    copy(
                        messages = messages.copy(
                            position = messages.position.copy(type = MessagePosition.Type.UNDEFINED)
                        )
                    )
                }
            }
        is MessagesEvent.Ui.SetMessagesRead ->
            commands { +MessagesCommand.SetMessagesRead(event.messageIds) }
        is MessagesEvent.Ui.ShowChooseReactionDialog ->
            effects { +MessagesEffect.ShowChooseReactionDialog(event.messageView.messageId) }
        is MessagesEvent.Ui.Init -> {}
    }
}