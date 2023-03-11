package com.spinoza.messenger_tfs.presentation.adapter.message

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.utils.DelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.utils.MessageDelegateItem
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

class UserMessageDelegateItem(
    private val id: Int,
    private val value: Message,
    private val onAvatarLongClickListener: ((MessageView) -> Unit)? = null,
    private val onReactionAddClickListener: ((MessageView) -> Unit)? = null,
    private val onReactionClickListener: ((MessageView, ReactionView) -> Unit)? = null,
) : MessageDelegateItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Int {
        return id
    }

    override fun compareToOther(other: DelegateItem): Boolean {
        return (other as UserMessageDelegateItem).value == content()
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
}