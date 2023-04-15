package com.spinoza.messenger_tfs.presentation.elmstore

import com.spinoza.messenger_tfs.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class MessagesReducer : ScreenDslReducer<
        MessagesScreenEvent,
        MessagesScreenEvent.Ui,
        MessagesScreenEvent.Internal,
        MessagesScreenState,
        MessagesScreenEffect,
        MessagesScreenCommand>(
    MessagesScreenEvent.Ui::class, MessagesScreenEvent.Internal::class
) {

    private val router = GlobalDI.INSTANCE.globalRouter

    override fun Result.internal(event: MessagesScreenEvent.Internal) = when (event) {
        is MessagesScreenEvent.Internal.Messages -> {
            state { copy(isLoading = false, messages = event.value) }
            commands {
                +MessagesScreenCommand.GetMessagesEvent
                +MessagesScreenCommand.GetDeleteMessagesEvent
                +MessagesScreenCommand.GetReactionsEvent
            }
        }
        is MessagesScreenEvent.Internal.MessagesEventFromQueue -> {
            state { copy(messages = event.value) }
            commands { +MessagesScreenCommand.GetMessagesEvent }
        }
        is MessagesScreenEvent.Internal.DeleteMessagesEventFromQueue -> {
            state { copy(messages = event.value) }
            commands { +MessagesScreenCommand.GetDeleteMessagesEvent }
        }
        is MessagesScreenEvent.Internal.ReactionsEventFromQueue -> {
            state { copy(messages = event.value) }
            commands { +MessagesScreenCommand.GetReactionsEvent }
        }
        is MessagesScreenEvent.Internal.EmptyMessagesQueueEvent ->
            commands { +MessagesScreenCommand.GetMessagesEvent }
        is MessagesScreenEvent.Internal.EmptyDeleteMessagesQueueEvent ->
            commands { +MessagesScreenCommand.GetDeleteMessagesEvent }
        is MessagesScreenEvent.Internal.EmptyReactionsQueueEvent ->
            commands { +MessagesScreenCommand.GetReactionsEvent }
        is MessagesScreenEvent.Internal.MessageSent -> {
            state { copy(isSendingMessage = false) }
            effects { +MessagesScreenEffect.MessageSent }
        }
        is MessagesScreenEvent.Internal.IconActionResId ->
            state { copy(iconActionResId = event.value) }
        is MessagesScreenEvent.Internal.ErrorMessages -> {
            state { copy(isLoading = false, isSendingMessage = false) }
            effects { +MessagesScreenEffect.Failure.ErrorMessages(event.value) }
        }
        is MessagesScreenEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false, isSendingMessage = false) }
            effects { +MessagesScreenEffect.Failure.ErrorNetwork(event.value) }
        }
        is MessagesScreenEvent.Internal.Idle -> {}
    }

    override fun Result.ui(event: MessagesScreenEvent.Ui) = when (event) {
        is MessagesScreenEvent.Ui.NewMessageText -> {
            commands { +MessagesScreenCommand.NewMessageText(event.value) }
        }
        is MessagesScreenEvent.Ui.Load -> {
            state { copy(isLoading = true) }
            commands { +MessagesScreenCommand.Load(event.filter) }
        }
        is MessagesScreenEvent.Ui.SendMessage -> {
            val text = event.value.toString().trim()
            when (text.isNotEmpty()) {
                true -> {
                    state { copy(isSendingMessage = true) }
                    commands { +MessagesScreenCommand.SendMessage(text) }
                }
                // TODO: show field for creating new topic
                false -> {}
            }
        }
        is MessagesScreenEvent.Ui.ShowUserInfo ->
            router.navigateTo(Screens.UserProfile(event.message.userId))
        is MessagesScreenEvent.Ui.Exit -> router.exit()
        is MessagesScreenEvent.Ui.UpdateReaction ->
            commands { +MessagesScreenCommand.UpdateReaction(event.messageId, event.emoji) }
        is MessagesScreenEvent.Ui.AfterSubmitMessages ->
            state.messages?.let { messages ->
                state {
                    copy(
                        messages = messages.copy(
                            position = messages.position.copy(type = MessagePosition.Type.UNDEFINED)
                        )
                    )
                }
            }
        is MessagesScreenEvent.Ui.VisibleMessages ->
            commands { +MessagesScreenCommand.SetMessagesRead(event.messageIds) }
        is MessagesScreenEvent.Ui.ShowChooseReactionDialog ->
            effects { +MessagesScreenEffect.ShowChooseReactionDialog(event.messageView.messageId) }
        is MessagesScreenEvent.Ui.Init -> {}
    }
}