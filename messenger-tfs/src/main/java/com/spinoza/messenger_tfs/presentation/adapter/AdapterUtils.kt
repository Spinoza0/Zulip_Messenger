package com.spinoza.messenger_tfs.presentation.adapter

import com.bumptech.glide.Glide
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.TopicItemBinding
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.CompanionMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.MessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapterConfig
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import java.util.*

fun List<Message>.groupByDate(
    user: User,
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
            if (message.user.userId == user.userId) {
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
    val message = item.content() as Message
    setMessage(message, item.getGravity())
    Glide.with(avatarImage)
        .load(message.user.avatar_url)
        .circleCrop()
        .error(R.drawable.face)
        .into(avatarImage)
    setOnAvatarClickListener(item.onAvatarLongClickListener())
    setOnMessageLongClickListener(item.onReactionAddClickListener())
    setOnReactionClickListener(item.onReactionClickListener())
}

fun TopicItemBinding.bind(
    channel: Channel,
    topic: Topic,
    position: Int,
    config: TopicAdapterConfig,
) {
    val color = if (position % 2 == 0) {
        config.evenColor
    } else {
        config.oddColor
    }
    this.textViewTopic.text = String.format(config.template, topic.name, topic.messageCount)
    this.textViewTopic.setBackgroundColor(color)
    this.root.setOnClickListener {
        config.onClickListener.invoke(channel, topic.name)
    }
}