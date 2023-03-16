package com.spinoza.messenger_tfs.presentation.adapter

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.MessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.channel.ChannelFoldedDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.channel.ChannelUnfoldedDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.CompanionMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import java.util.*

fun List<Channel>.toDelegateItems(onChannelClickListener: (Long) -> Unit): List<DelegateItem> {

    val delegateItemList = mutableListOf<DelegateItem>()
    this.forEach { channel ->
        if (channel.type == Channel.Type.FOLDED) {
            delegateItemList.add(ChannelFoldedDelegateItem(channel, onChannelClickListener))
        } else {
            delegateItemList.add(ChannelUnfoldedDelegateItem(channel, onChannelClickListener))
        }
    }
    return delegateItemList
}

fun List<Message>.groupByDate(
    userId: Long,
    onAvatarLongClickListener: ((MessageView) -> Unit)? = null,
    onReactionAddClickListener: ((MessageView) -> Unit)? = null,
    onReactionClickListener: ((MessageView, ReactionView) -> Unit)? = null,
): List<DelegateItem> {

    val delegateItemList = mutableListOf<DelegateItem>()
    val dates = TreeSet<MessageDate>()
    this.forEach {
        dates.add(it.date)
    }

    dates.forEach { messageDate ->
        delegateItemList.add(DateDelegateItem(messageDate))
        val allDayMessages = this.filter { message ->
            message.date.date == messageDate.date
        }

        allDayMessages.forEach { message ->
            if (message.userId == userId) {
                delegateItemList.add(
                    UserMessageDelegateItem(
                        message,
                        onAvatarLongClickListener,
                        onReactionAddClickListener,
                        onReactionClickListener
                    )
                )
            } else {
                delegateItemList.add(
                    CompanionMessageDelegateItem(
                        message,
                        onAvatarLongClickListener,
                        onReactionAddClickListener,
                        onReactionClickListener
                    )
                )
            }
        }
    }

    return delegateItemList
}

fun MessageView.bind(item: MessageDelegateItem) {
    setMessage(item.content() as Message, item.getGravity())
    setOnAvatarClickListener(item.onAvatarLongClickListener())
    setOnMessageLongClickListener(item.onReactionAddClickListener())
    setOnReactionClickListener(item.onReactionClickListener())
}