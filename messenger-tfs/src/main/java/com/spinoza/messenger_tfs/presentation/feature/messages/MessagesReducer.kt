package com.spinoza.messenger_tfs.presentation.feature.messages

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.network.WebUtil
import com.spinoza.messenger_tfs.domain.util.SECONDS_IN_DAY
import com.spinoza.messenger_tfs.domain.util.getCurrentTimestamp
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer
import javax.inject.Inject

class MessagesReducer @Inject constructor(
    private val router: AppRouter,
    private val webUtil: WebUtil,
    private val authorizationStorage: AuthorizationStorage,
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
    private var isDraggingWithoutScroll = false

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
        }

        is MessagesScreenEvent.Internal.StoredMessages -> {
            state {
                copy(
                    isLoading = event.value.messages.isEmpty(),
                    isLongOperation = false,
                    messages = event.value
                )
            }
            commands {
                +MessagesScreenCommand.LoadFirstPage(event.value.messages.isEmpty())
                +MessagesScreenCommand.GetMessagesEvent(isLastMessageVisible)
                +MessagesScreenCommand.GetUpdateMessagesEvent(isLastMessageVisible)
                +MessagesScreenCommand.GetDeleteMessagesEvent(isLastMessageVisible)
                +MessagesScreenCommand.GetReactionsEvent(isLastMessageVisible)
            }
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

        is MessagesScreenEvent.Internal.UpdateMessagesEventFromQueue -> {
            state { copy(messages = event.value) }
            commands { +MessagesScreenCommand.GetUpdateMessagesEvent(isLastMessageVisible) }
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

        is MessagesScreenEvent.Internal.EmptyUpdateMessagesQueueEvent ->
            commands { +MessagesScreenCommand.GetUpdateMessagesEvent(isLastMessageVisible) }

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
                    commands { +MessagesScreenCommand.LoadLastPage }
                } else {
                    effects { +MessagesScreenEffect.ScrollToLastMessage }
                }
            } else {
                commands { +MessagesScreenCommand.LoadNextPage }
            }
        }

        is MessagesScreenEvent.Internal.FileUploaded -> {
            state { copy(isLongOperation = false) }
            effects { +MessagesScreenEffect.FileUploaded(event.value) }
        }

        is MessagesScreenEvent.Internal.FilesDownloaded ->
            effects { +MessagesScreenEffect.FilesDownloaded(event.value) }

        is MessagesScreenEvent.Internal.RawMessageContent -> {
            state { copy(isLongOperation = false) }
            effects { +MessagesScreenEffect.RawMessageContent(event.messageId, event.content) }
        }

        is MessagesScreenEvent.Internal.ErrorMessages -> {
            state { copy(isLoading = false, isLongOperation = false, isSendingMessage = false) }
            effects { +MessagesScreenEffect.Failure.ErrorMessages(event.value) }
        }

        is MessagesScreenEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false, isLongOperation = false, isSendingMessage = false) }
            effects { +MessagesScreenEffect.Failure.ErrorNetwork(event.value) }
        }

        is MessagesScreenEvent.Internal.LogOut -> router.exit()
        is MessagesScreenEvent.Internal.LoginSuccess -> {}
        is MessagesScreenEvent.Internal.Idle -> {}
    }

    override fun Result.ui(event: MessagesScreenEvent.Ui) = when (event) {
        is MessagesScreenEvent.Ui.MessagesOnScrolled -> {
            isDraggingWithoutScroll = false
            visibleMessageIds.addAll(event.visibleMessagesIds)
            isLastMessageVisible = event.isLastMessageVisible
            state { copy(isNextMessageExisting = event.isNextMessageExisting) }
        }

        is MessagesScreenEvent.Ui.MessagesScrollStateIdle -> {
            if (isDraggingWithoutScroll) {
                isDraggingWithoutScroll = false
                if (!event.canScrollUp) {
                    commands { +MessagesScreenCommand.LoadPreviousPage }
                }
                if (!event.canScrollDown) {
                    commands { +MessagesScreenCommand.LoadNextPage }
                }
            } else {
                val list = visibleMessageIds.toList()
                if (visibleMessageIds.size > MAX_NUMBER_OF_SAVED_VISIBLE_MESSAGE_IDS) {
                    visibleMessageIds.clear()
                }
                commands { +MessagesScreenCommand.SetMessagesRead(list) }
            }
            state { copy(isNextMessageExisting = event.isNextMessageExisting) }
        }

        is MessagesScreenEvent.Ui.MessagesScrollStateDragging -> isDraggingWithoutScroll = true
        is MessagesScreenEvent.Ui.NewMessageText -> {
            commands { +MessagesScreenCommand.NewMessageText(event.value) }
        }

        is MessagesScreenEvent.Ui.ShowChooseActionMenu -> {
            val messageView = event.messageView
            val isAdmin = authorizationStorage.isAdmin()
            val attachments = webUtil.getAttachmentsUrls(messageView.rawContent)
            val isMessageEditable = messageView.isOwn() && messageView.isMessageEditable()
            val isTopicEditable = isAdmin || (messageView.isOwn() && messageView.isTopicEditable())
            effects {
                +MessagesScreenEffect.ShowMessageMenu(
                    isDeleteMessageVisible = isAdmin,
                    isEditMessageVisible = isMessageEditable,
                    isEditTopicVisible = isTopicEditable,
                    attachments,
                    event.messageView
                )
            }
        }

        is MessagesScreenEvent.Ui.Load ->
            commands { +MessagesScreenCommand.LoadStored(event.filter) }

        is MessagesScreenEvent.Ui.LoadPreviousPage ->
            commands { +MessagesScreenCommand.LoadPreviousPage }

        is MessagesScreenEvent.Ui.LoadNextPage -> state.messages?.let {
            commands { +MessagesScreenCommand.IsNextPageExisting(it, false) }
        } ?: {
            commands { +MessagesScreenCommand.LoadNextPage }
        }

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
            commands { +MessagesScreenCommand.UploadFile(event.context, event.uri) }
        }

        is MessagesScreenEvent.Ui.CopyToClipboard ->
            commands {
                +MessagesScreenCommand.CopyToClipboard(
                    event.context,
                    event.messageView.messageId,
                    event.messageView.rawContent,
                    event.isMessageWithAttachments
                )
            }

        is MessagesScreenEvent.Ui.GetRawMessageContent -> {
            state { copy(isLongOperation = event.isMessageWithAttachments) }
            commands {
                +MessagesScreenCommand.GetRawMessageContent(
                    event.messageView.messageId,
                    event.messageView.rawContent,
                    event.isMessageWithAttachments
                )
            }
        }

        is MessagesScreenEvent.Ui.EditMessageContent ->
            commands { +MessagesScreenCommand.EditMessageContent(event.messageId, event.content) }

        is MessagesScreenEvent.Ui.EditMessageTopic ->
            commands { +MessagesScreenCommand.EditMessageTopic(event.messageId, event.topic) }

        is MessagesScreenEvent.Ui.SaveAttachments ->
            commands { +MessagesScreenCommand.SaveAttachments(event.context, event.urls) }

        is MessagesScreenEvent.Ui.ConfirmDeleteMessage ->
            effects { +MessagesScreenEffect.ConfirmDeleteMessage(event.messageView.messageId) }

        is MessagesScreenEvent.Ui.DeleteMessage ->
            commands { +MessagesScreenCommand.DeleteMessage(event.messageId) }

        is MessagesScreenEvent.Ui.CheckLoginStatus -> {
            if (!authorizationStorage.isUserLoggedIn()) {
                if (authorizationStorage.isAuthorizationDataExisted()) {
                    commands { +MessagesScreenCommand.LogIn }
                } else {
                    router.exit()
                }
            }
            effects { }
        }

        is MessagesScreenEvent.Ui.Exit -> router.exit()
        is MessagesScreenEvent.Ui.Init -> {}
    }

    private fun MessageView.isMessageEditable(): Boolean {
        return (getCurrentTimestamp() - this.date.fullTimeStamp) < MESSAGE_EDITABLE_TIME_IN_SECONDS
    }

    private fun MessageView.isTopicEditable(): Boolean {
        return (getCurrentTimestamp() - this.date.fullTimeStamp) < TOPIC_EDITABLE_TIME_IN_SECONDS
    }

    private fun MessageView.isOwn(): Boolean {
        return this.userId == authorizationStorage.getUserId()
    }

    private companion object {

        const val MAX_NUMBER_OF_SAVED_VISIBLE_MESSAGE_IDS = 50
        const val MESSAGE_EDITABLE_TIME_IN_SECONDS = 300
        const val TOPIC_EDITABLE_TIME_IN_SECONDS = SECONDS_IN_DAY * 3
    }
}