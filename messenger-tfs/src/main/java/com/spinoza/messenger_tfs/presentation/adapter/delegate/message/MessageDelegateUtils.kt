package com.spinoza.messenger_tfs.presentation.adapter.delegate.message

import com.bumptech.glide.Glide
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.FlexBoxGravity
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

interface MessageDelegateItem : DelegateItem {

    fun getAvatarClickListener(): ((MessageView) -> Unit)?

    fun getReactionAddClickListener(): ((MessageView) -> Unit)?

    fun getReactionClickListener(): ((MessageView, ReactionView) -> Unit)?

    fun getGravity(): FlexBoxGravity
}

internal fun MessageView.bind(item: MessageDelegateItem) {
    val message = item.content() as Message
    setMessage(message, item.getGravity())
    Glide.with(avatarImage)
        .load(message.user.avatar_url)
        .circleCrop()
        .error(R.drawable.ic_default_avatar)
        .into(avatarImage)
    setOnAvatarClickListener(item.getAvatarClickListener())
    setOnMessageLongClickListener(item.getReactionAddClickListener())
    setOnReactionClickListener(item.getReactionClickListener())
}