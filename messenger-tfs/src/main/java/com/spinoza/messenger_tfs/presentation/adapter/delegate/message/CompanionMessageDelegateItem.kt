package com.spinoza.messenger_tfs.presentation.adapter.delegate.message

import com.spinoza.messenger_tfs.domain.model.FlexBoxGravity
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.MessageDelegateItem
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

class CompanionMessageDelegateItem(
    private val value: Message,
    private val onAvatarLongClickListener: ((MessageView) -> Unit)? = null,
    private val onReactionAddClickListener: ((MessageView) -> Unit)? = null,
    private val onReactionClickListener: ((MessageView, ReactionView) -> Unit)? = null,
) : MessageDelegateItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.id
    }

    override fun compareToOther(other: DelegateItem): Boolean {
        return (other as CompanionMessageDelegateItem).value == content()
    }

    override fun onAvatarLongClickListener(): ((MessageView) -> Unit)? {
        return onAvatarLongClickListener
    }

    override fun onReactionAddClickListener(): ((MessageView) -> Unit)? {
        return onReactionAddClickListener
    }

    override fun onReactionClickListener(): ((MessageView, ReactionView) -> Unit)? {
        return onReactionClickListener
    }

    override fun getGravity(): FlexBoxGravity {
        return FlexBoxGravity.START
    }
}