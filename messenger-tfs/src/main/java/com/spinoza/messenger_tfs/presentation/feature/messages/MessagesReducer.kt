package com.spinoza.messenger_tfs.presentation.feature.messages

import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.webutil.WebUtil
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer
import javax.inject.Inject

class MessagesReducer @Inject constructor(
    private val router: Router,
    private val webUtil: WebUtil,
) : ScreenDslReducer<
        MessagesScreenEvent,
        MessagesScreenEvent.Ui,
        MessagesScreenEvent.Internal,
        MessagesScreenState,
        MessagesScreenEffect,
        MessagesScreenCommand>(
    MessagesScreenEvent.Ui::class, MessagesScreenEvent.Internal::class
) {

    private val visibleMessageIds = mutableSetOf<Long>()
    private var isLastMessageVisible = false
    private var messageSentId = Message.UNDEFINED_ID

    override fun Result.internal(event: MessagesScreenEvent.Internal) = when (event) {
        is MessagesScreenEvent.Internal.Messages -> {
            state {
                copy(
                    isLoading = false,
                    isLongOperation = false,
                    isNewMessageExisting = false,
                    messages = event.value
                )
            }
            commands {
                +MessagesScreenCommand.GetMessagesEvent(isLastMessageVisible)
                +MessagesScreenCommand.GetDeleteMessagesEvent(isLastMessageVisible)
                +MessagesScreenCommand.GetReactionsEvent(isLastMessageVisible)
            }
        }
        is MessagesScreenEvent.Internal.StoredMessages -> {
            state {
                copy(
                    isLoading = event.value.messages.isEmpty(),
                    isLongOperation = false,
                    messages = event.value
                )
            }
            commands { +MessagesScreenCommand.LoadFirstPage(event.value.messages.isEmpty()) }
        }
        is MessagesScreenEvent.Internal.MessagesEventFromQueue -> {
            state {
                copy(
                    messages = event.value,
                    isNewMessageExisting = event.value.isNewMessageExisting
                )
            }
            if (messageSentId != Message.UNDEFINED_ID) {
                state.messages?.let {
                    commands { +MessagesScreenCommand.IsNextPageExisting(it, true, messageSentId) }
                }
                messageSentId = Message.UNDEFINED_ID
            }
            commands { +MessagesScreenCommand.GetMessagesEvent(isLastMessageVisible) }
        }
        is MessagesScreenEvent.Internal.DeleteMessagesEventFromQueue -> {
            state { copy(messages = event.value) }
            commands { +MessagesScreenCommand.GetDeleteMessagesEvent(isLastMessageVisible) }
        }
        is MessagesScreenEvent.Internal.ReactionsEventFromQueue -> {
            state { copy(messages = event.value) }
            commands { +MessagesScreenCommand.GetReactionsEvent(isLastMessageVisible) }
        }
        is MessagesScreenEvent.Internal.EmptyMessagesQueueEvent ->
            commands { +MessagesScreenCommand.GetMessagesEvent(isLastMessageVisible) }
        is MessagesScreenEvent.Internal.EmptyDeleteMessagesQueueEvent ->
            commands { +MessagesScreenCommand.GetDeleteMessagesEvent(isLastMessageVisible) }
        is MessagesScreenEvent.Internal.EmptyReactionsQueueEvent ->
            commands { +MessagesScreenCommand.GetReactionsEvent(isLastMessageVisible) }
        is MessagesScreenEvent.Internal.MessageSent -> {
            if (event.messageId != Message.UNDEFINED_ID) {
                messageSentId = event.messageId
                effects { +MessagesScreenEffect.MessageSent }
            }
            state { copy(isSendingMessage = false, isNewMessageExisting = false) }
        }
        is MessagesScreenEvent.Internal.IconActionResId ->
            state { copy(iconActionResId = event.value) }
        is MessagesScreenEvent.Internal.NextPageExists -> {
            if (event.isGoingToLastMessage) {
                if (event.value) {
                    state { copy(isLongOperation = true) }
                    commands { +MessagesScreenCommand.LoadLastPage }
                } else {
                    effects { +MessagesScreenEffect.ScrollToLastMessage }
                }
            } else {
                state { copy(isLongOperation = event.value) }
                commands { +MessagesScreenCommand.LoadNextPage }
            }
        }
        is MessagesScreenEvent.Internal.FileUploaded -> {
            state { copy(isLongOperation = false) }
            effects { +MessagesScreenEffect.FileUploaded(event.newMessageText) }
        }
        is MessagesScreenEvent.Internal.ErrorMessages -> {
            state { copy(isLoading = false, isLongOperation = false, isSendingMessage = false) }
            effects { +MessagesScreenEffect.Failure.ErrorMessages(event.value) }
        }
        is MessagesScreenEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false, isLongOperation = false, isSendingMessage = false) }
            effects { +MessagesScreenEffect.Failure.ErrorNetwork(event.value) }
        }
        is MessagesScreenEvent.Internal.Idle -> {}
    }

    override fun Result.ui(event: MessagesScreenEvent.Ui) = when (event) {
        is MessagesScreenEvent.Ui.MessagesOnScrolled -> {
            if (event.dy.isScrollUp()) {
                state { copy(isLongOperation = false) }
                if (event.firstVisiblePosition <= BORDER_POSITION || !event.canScrollUp) {
                    state { copy(isLongOperation = true) }
                    commands { +MessagesScreenCommand.LoadPreviousPage }
                }
            }
            if (event.dy.isScrollDown()) {
                state { copy(isLongOperation = false) }
                if (event.lastVisiblePosition >= (event.itemCount - BORDER_POSITION) ||
                    !event.canScrollDown
                ) {
                    state.messages?.let {
                        commands { +MessagesScreenCommand.IsNextPageExisting(it, false) }
                    } ?: {
                        state { copy(isLongOperation = true) }
                        commands { +MessagesScreenCommand.LoadNextPage }
                    }
                }
            }
            visibleMessageIds.addAll(event.visibleMessagesIds)
            isLastMessageVisible = event.isLastMessageVisible
            state { copy(isNextMessageExisting = event.isNextMessageExisting) }
        }
        is MessagesScreenEvent.Ui.MessagesScrollStateIdle -> {
            val list = visibleMessageIds.toList()
            if (visibleMessageIds.size > MAX_NUMBER_OF_SAVED_VISIBLE_MESSAGE_IDS) {
                visibleMessageIds.clear()
            }
            commands { +MessagesScreenCommand.SetMessagesRead(list) }
        }
        is MessagesScreenEvent.Ui.NewMessageText -> {
            commands { +MessagesScreenCommand.NewMessageText(event.value) }
        }
        is MessagesScreenEvent.Ui.OnMessageLongClick -> {
            val attachments = webUtil.getAttachmentsUrls(event.messageView.rawContent)
            if (attachments.isNotEmpty()) {
                effects { +MessagesScreenEffect.ShowMessageMenu(attachments, event.messageView) }
            } else {
                effects {
                    +MessagesScreenEffect.ShowChooseReactionDialog(event.messageView.messageId)
                }
            }
        }
        is MessagesScreenEvent.Ui.Load ->
            commands { +MessagesScreenCommand.LoadStored(event.filter) }
        is MessagesScreenEvent.Ui.SendMessage -> {
            val text = event.value.toString().trim()
            when (text.isNotEmpty()) {
                true -> {
                    state { copy(isSendingMessage = true) }
                    commands { +MessagesScreenCommand.SendMessage(text) }
                }
                false -> effects { +MessagesScreenEffect.AddAttachment }
            }
        }
        is MessagesScreenEvent.Ui.ShowUserInfo ->
            router.navigateTo(Screens.UserProfile(event.message.userId))
        is MessagesScreenEvent.Ui.UpdateReaction ->
            commands { +MessagesScreenCommand.UpdateReaction(event.messageId, event.emoji) }
        is MessagesScreenEvent.Ui.AfterSubmitMessages -> state.messages?.let { messages ->
            isLastMessageVisible = event.isLastMessageVisible
            state {
                copy(
                    isNextMessageExisting = event.isNextMessageExisting,
                    messages = messages.copy(
                        position = messages.position.copy(type = MessagePosition.Type.UNDEFINED)
                    )
                )
            }
        }
        is MessagesScreenEvent.Ui.ShowChooseReactionDialog ->
            effects { +MessagesScreenEffect.ShowChooseReactionDialog(event.messageView.messageId) }
        is MessagesScreenEvent.Ui.Reload -> {
            state { copy(isLongOperation = true) }
            commands { +MessagesScreenCommand.Reload }
        }
        is MessagesScreenEvent.Ui.ScrollToLastMessage -> state.messages?.let {
            commands { +MessagesScreenCommand.IsNextPageExisting(it, true) }
        } ?: {
            state { copy(isLongOperation = true) }
            commands { +MessagesScreenCommand.LoadLastPage }
        }
        is MessagesScreenEvent.Ui.UploadFile -> {
            state { copy(isLongOperation = true) }
            commands { +MessagesScreenCommand.UploadFile(event.message.toString(), event.uri) }
        }
        is MessagesScreenEvent.Ui.SaveAttachments ->
            commands { +MessagesScreenCommand.SaveAttachments(event.urls) }
        is MessagesScreenEvent.Ui.Exit -> router.exit()
        is MessagesScreenEvent.Ui.Init -> {}
    }

    private fun Int.isScrollUp() = this < 0

    private fun Int.isScrollDown() = this > 0

    private companion object {

        const val BORDER_POSITION = 5
        const val MAX_NUMBER_OF_SAVED_VISIBLE_MESSAGE_IDS = 50
    }
}