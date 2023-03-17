package com.spinoza.messenger_tfs.presentation.adapter.delegate.message

import com.spinoza.messenger_tfs.domain.model.FlexBoxGravity
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

interface MessageDelegateItem : DelegateItem {

    fun onAvatarLongClickListener(): ((MessageView) -> Unit)?

    fun onReactionAddClickListener(): ((MessageView) -> Unit)?

    fun onReactionClickListener(): ((MessageView, ReactionView) -> Unit)?

    fun getGravity(): FlexBoxGravity
}