package com.spinoza.messenger_tfs.presentation.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.TopicItemBinding
import com.spinoza.messenger_tfs.databinding.UserItemBinding
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
    onAvatarClickListener: ((MessageView) -> Unit)? = null,
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
                        onAvatarClickListener,
                        onReactionAddClickListener,
                        onReactionClickListener
                    )
                )
            } else {
                delegateItemList.add(
                    CompanionMessageDelegateItem(
                        message,
                        onAvatarClickListener,
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
        .error(R.drawable.ic_default_avatar)
        .into(avatarImage)
    setOnAvatarClickListener(item.onAvatarClickListener())
    setOnMessageLongClickListener(item.onReactionAddClickListener())
    setOnReactionClickListener(item.onReactionClickListener())
}

fun TopicItemBinding.bind(
    channel: Channel,
    topic: Topic,
    position: Int,
    config: TopicAdapterConfig,
) {
    val color = if (position % 2 == 0) config.evenColor else config.oddColor
    with(this) {
        textViewTopic.text = String.format(config.template, topic.name, topic.messageCount)
        textViewTopic.setBackgroundColor(color)
        root.setOnClickListener {
            config.onClickListener.invoke(channel, topic.name)
        }
    }
}

fun UserItemBinding.bind(user: User, onClickListener: (Long) -> Unit) {
    textViewName.text = user.full_name
    textViewEmail.text = user.email
    Glide.with(imageViewAvatar)
        .load(user.avatar_url)
        .circleCrop()
        .error(R.drawable.ic_default_avatar)
        .into(imageViewAvatar)
    val visibility = if (user.isActive) View.VISIBLE else View.GONE
    imageViewCircleBorder.visibility = visibility
    imageViewCircle.visibility = visibility
    root.setOnClickListener {
        onClickListener(user.userId)
    }
}