package com.spinoza.messenger_tfs.presentation.feature.messages

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesAnchor
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
        is MessagesScreenEvent.Ui.MessagesOnScrolled -> {
            val layoutManager = event.recyclerView.layoutManager as LinearLayoutManager
            val adapter = event.recyclerView.adapter as MainDelegateAdapter
            var firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            if (firstVisiblePosition == UNDEFINED_POSITION) {
                firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
            }
            var lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
            if (lastVisiblePosition == UNDEFINED_POSITION) {
                lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
            }
            if (event.dy < 0 && firstVisiblePosition <= BORDER_POSITION) {
                commands { +MessagesScreenCommand.LoadPage(MessagesAnchor.OLDEST) }
            }
            if (event.dy > 0 && lastVisiblePosition >= adapter.itemCount - BORDER_POSITION) {
                commands { +MessagesScreenCommand.LoadPage(MessagesAnchor.NEWEST) }
            }
            val ids = getVisibleMessagesIds(adapter, firstVisiblePosition, lastVisiblePosition)
            commands { +MessagesScreenCommand.SetMessagesRead(ids) }
        }
        is MessagesScreenEvent.Ui.MessagesScrollStateIdle ->
            state { copy(isNextMessageExists = isNextMessageExists(event.recyclerView)) }
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
        is MessagesScreenEvent.Ui.AfterSubmitMessages -> state.messages?.let { messages ->
            state {
                copy(
                    isNextMessageExists = isNextMessageExists(event.recyclerView),
                    messages = messages.copy(
                        position = messages.position.copy(type = MessagePosition.Type.UNDEFINED)
                    )
                )
            }
        }
        is MessagesScreenEvent.Ui.ShowChooseReactionDialog ->
            effects { +MessagesScreenEffect.ShowChooseReactionDialog(event.messageView.messageId) }
        is MessagesScreenEvent.Ui.Init -> {}
    }

    private fun getVisibleMessagesIds(
        adapter: MainDelegateAdapter,
        firstVisiblePosition: Int,
        lastVisiblePosition: Int,
    ): List<Long> {
        val visibleMessageIds = mutableListOf<Long>()
        if (firstVisiblePosition != UNDEFINED_POSITION && lastVisiblePosition != UNDEFINED_POSITION) {
            for (i in firstVisiblePosition..lastVisiblePosition) {
                val item = adapter.getItem(i)
                if (item is UserMessageDelegateItem || item is OwnMessageDelegateItem) {
                    visibleMessageIds.add((item.content() as Message).id)
                }
            }
        }
        return visibleMessageIds
    }

    private fun isNextMessageExists(recyclerView: RecyclerView): Boolean {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val lastItemPosition = recyclerView.adapter?.itemCount?.minus(1)
        return lastItemPosition != null && lastVisibleItemPosition < lastItemPosition
    }

    private companion object {

        const val UNDEFINED_POSITION = -1
        const val BORDER_POSITION = 5
    }
}