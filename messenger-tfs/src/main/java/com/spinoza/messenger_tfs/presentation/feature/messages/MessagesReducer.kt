package com.spinoza.messenger_tfs.presentation.feature.messages

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer
import javax.inject.Inject

class MessagesReducer @Inject constructor(private val router: Router) : ScreenDslReducer<
        MessagesScreenEvent,
        MessagesScreenEvent.Ui,
        MessagesScreenEvent.Internal,
        MessagesScreenState,
        MessagesScreenEffect,
        MessagesScreenCommand>(
    MessagesScreenEvent.Ui::class, MessagesScreenEvent.Internal::class
) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var delegateAdapter: MainDelegateAdapter
    private val visibleMessageIds = mutableSetOf<Long>()

    override fun Result.internal(event: MessagesScreenEvent.Internal) = when (event) {
        is MessagesScreenEvent.Internal.Messages -> {
            state {
                copy(
                    isLoading = false,
                    isLoadingPreviousPage = false,
                    isLoadingNextPage = false,
                    isNewMessageExisting = false,
                    messages = event.value
                )
            }
            val isLastMessageVisible = isLastMessageVisible()
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
                    isLoadingPreviousPage = false,
                    isLoadingNextPage = false,
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
            commands { +MessagesScreenCommand.GetMessagesEvent(isLastMessageVisible()) }
        }
        is MessagesScreenEvent.Internal.DeleteMessagesEventFromQueue -> {
            state { copy(messages = event.value) }
            commands { +MessagesScreenCommand.GetDeleteMessagesEvent(isLastMessageVisible()) }
        }
        is MessagesScreenEvent.Internal.ReactionsEventFromQueue -> {
            state { copy(messages = event.value) }
            commands { +MessagesScreenCommand.GetReactionsEvent(isLastMessageVisible()) }
        }
        is MessagesScreenEvent.Internal.EmptyMessagesQueueEvent ->
            commands { +MessagesScreenCommand.GetMessagesEvent(isLastMessageVisible()) }
        is MessagesScreenEvent.Internal.EmptyDeleteMessagesQueueEvent ->
            commands { +MessagesScreenCommand.GetDeleteMessagesEvent(isLastMessageVisible()) }
        is MessagesScreenEvent.Internal.EmptyReactionsQueueEvent ->
            commands { +MessagesScreenCommand.GetReactionsEvent(isLastMessageVisible()) }
        is MessagesScreenEvent.Internal.MessageSent -> {
            state {
                copy(isSendingMessage = false, isNewMessageExisting = false, messages = event.value)
            }
            effects { +MessagesScreenEffect.MessageSent }
        }
        is MessagesScreenEvent.Internal.IconActionResId ->
            state { copy(iconActionResId = event.value) }
        is MessagesScreenEvent.Internal.NextPageExists -> {
            if (event.isGoingToLastMessage) {
                if (event.value) {
                    state { copy(isLoadingNextPage = true) }
                    commands { +MessagesScreenCommand.LoadLastPage }
                } else {
                    effects { +MessagesScreenEffect.ScrollToLastMessage }
                }
            } else {
                state { copy(isLoadingNextPage = event.value) }
                commands { +MessagesScreenCommand.LoadNextPage }
            }
        }
        is MessagesScreenEvent.Internal.ErrorMessages -> {
            state {
                copy(
                    isLoading = false, isLoadingPreviousPage = false,
                    isLoadingNextPage = false, isSendingMessage = false
                )
            }
            effects { +MessagesScreenEffect.Failure.ErrorMessages(event.value) }
        }
        is MessagesScreenEvent.Internal.ErrorNetwork -> {
            state {
                copy(
                    isLoading = false, isLoadingPreviousPage = false,
                    isLoadingNextPage = false, isSendingMessage = false
                )
            }
            effects { +MessagesScreenEffect.Failure.ErrorNetwork(event.value) }
        }
        is MessagesScreenEvent.Internal.Idle -> {}
    }

    override fun Result.ui(event: MessagesScreenEvent.Ui) = when (event) {
        is MessagesScreenEvent.Ui.MessagesOnScrolled -> {
            var firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            if (firstVisiblePosition == RecyclerView.NO_POSITION) {
                firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
            }
            var lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
            if (lastVisiblePosition == RecyclerView.NO_POSITION) {
                lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
            }
            if (event.dy.isScrollUp()) {
                state { copy(isLoadingNextPage = false) }
                if (firstVisiblePosition <= BORDER_POSITION) {
                    state { copy(isLoadingPreviousPage = true) }
                    commands { +MessagesScreenCommand.LoadPreviousPage }
                }
            }
            if (event.dy.isScrollDown()) {
                state { copy(isLoadingPreviousPage = false) }
                if (lastVisiblePosition >= delegateAdapter.itemCount - BORDER_POSITION) {
                    state.messages?.let {
                        commands { +MessagesScreenCommand.IsNextPageExisting(it, false) }
                    } ?: {
                        state { copy(isLoadingNextPage = true) }
                        commands { +MessagesScreenCommand.LoadNextPage }
                    }
                }
            }
            saveVisibleMessagesIds(firstVisiblePosition, lastVisiblePosition)
        }
        is MessagesScreenEvent.Ui.MessagesScrollStateIdle -> {
            commands { +MessagesScreenCommand.SetMessagesRead(visibleMessageIds.toList()) }
            if (visibleMessageIds.size > MAX_NUMBER_OF_SAVED_VISIBLE_MESSAGE_IDS) {
                visibleMessageIds.clear()
            }
            state { copy(isNextMessageExisting = isNextMessageExisting()) }
        }
        is MessagesScreenEvent.Ui.NewMessageText -> {
            commands { +MessagesScreenCommand.NewMessageText(event.value) }
        }
        is MessagesScreenEvent.Ui.Load -> {
            recyclerView = event.recyclerView
            layoutManager = recyclerView.layoutManager as LinearLayoutManager
            delegateAdapter = recyclerView.adapter as MainDelegateAdapter
            commands { +MessagesScreenCommand.LoadStored(event.filter) }
        }
        is MessagesScreenEvent.Ui.SendMessage -> {
            val text = event.value.toString().trim()
            when (text.isNotEmpty()) {
                true -> {
                    state { copy(isSendingMessage = true) }
                    commands { +MessagesScreenCommand.SendMessage(text) }
                }
                // TODO: show field for creating new topic or add attachment
                false -> {}
            }
        }
        is MessagesScreenEvent.Ui.ShowUserInfo ->
            router.navigateTo(Screens.UserProfile(event.message.userId))
        is MessagesScreenEvent.Ui.UpdateReaction ->
            commands { +MessagesScreenCommand.UpdateReaction(event.messageId, event.emoji) }
        is MessagesScreenEvent.Ui.AfterSubmitMessages -> state.messages?.let { messages ->
            state {
                copy(
                    isNextMessageExisting = isNextMessageExisting(),
                    messages = messages.copy(
                        position = messages.position.copy(type = MessagePosition.Type.UNDEFINED)
                    )
                )
            }
        }
        is MessagesScreenEvent.Ui.ShowChooseReactionDialog ->
            effects { +MessagesScreenEffect.ShowChooseReactionDialog(event.messageView.messageId) }
        is MessagesScreenEvent.Ui.Reload -> {
            state { copy(isLoadingPreviousPage = true) }
            commands { +MessagesScreenCommand.Reload }
        }
        is MessagesScreenEvent.Ui.ScrollToLastMessage -> state.messages?.let {
            commands { +MessagesScreenCommand.IsNextPageExisting(it, true) }
        } ?: {
            state { copy(isLoadingNextPage = true) }
            commands { +MessagesScreenCommand.LoadLastPage }
        }
        is MessagesScreenEvent.Ui.Exit -> router.exit()
        is MessagesScreenEvent.Ui.Init -> {}
    }

    private fun saveVisibleMessagesIds(firstVisiblePosition: Int, lastVisiblePosition: Int) {
        if (firstVisiblePosition != RecyclerView.NO_POSITION &&
            lastVisiblePosition != RecyclerView.NO_POSITION
        ) {
            for (i in firstVisiblePosition..lastVisiblePosition) {
                val item = delegateAdapter.getItem(i)
                if (item is UserMessageDelegateItem || item is OwnMessageDelegateItem) {
                    visibleMessageIds.add((item.content() as Message).id)
                }
            }
        }
    }

    private fun isLastMessageVisible(): Boolean {
        val lastItemPosition = delegateAdapter.itemCount.minus(1)
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        return lastVisibleItemPosition == lastItemPosition
    }

    private fun isNextMessageExisting(): Boolean {
        return layoutManager.findLastVisibleItemPosition() < delegateAdapter.itemCount.minus(1)
    }

    private fun Int.isScrollUp() = this < 0

    private fun Int.isScrollDown() = this > 0

    private companion object {

        const val BORDER_POSITION = 5
        const val MAX_NUMBER_OF_SAVED_VISIBLE_MESSAGE_IDS = 50
    }
}