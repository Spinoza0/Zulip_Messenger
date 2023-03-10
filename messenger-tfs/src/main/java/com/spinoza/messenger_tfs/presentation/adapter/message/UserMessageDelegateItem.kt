package com.spinoza.messenger_tfs.presentation.adapter.message

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.utils.DelegateItem
import com.spinoza.messenger_tfs.presentation.ui.MessageView

class UserMessageDelegateItem(
    private val id: Int,
    private val value: Message,
    private val onAvatarLongClickListener: ((MessageView) -> Unit)? = null,
    private val onReactionAddClickListener: ((MessageView) -> Unit)? = null,
    private val onReactionClickListener: ((MessageView) -> Unit)? = null,
) : DelegateItem, OnMessageClickListeners {

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

    override fun onReactionClickListener(): ((MessageView) -> Unit)? {
        return onReactionClickListener
    }
}