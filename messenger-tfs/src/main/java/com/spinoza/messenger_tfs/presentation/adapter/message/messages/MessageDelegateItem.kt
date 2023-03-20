package com.spinoza.messenger_tfs.presentation.adapter.message.messages

import com.spinoza.messenger_tfs.domain.model.FlexBoxGravity
import com.spinoza.messenger_tfs.presentation.adapter.message.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

interface MessageDelegateItem : DelegateAdapterItem {

    fun getAvatarClickListener(): ((MessageView) -> Unit)?

    fun getReactionAddClickListener(): ((MessageView) -> Unit)?

    fun getReactionClickListener(): ((MessageView, ReactionView) -> Unit)?

    fun getGravity(): FlexBoxGravity
}