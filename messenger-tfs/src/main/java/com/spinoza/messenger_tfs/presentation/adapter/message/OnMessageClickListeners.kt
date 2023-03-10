package com.spinoza.messenger_tfs.presentation.adapter.message

import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

interface OnMessageClickListeners {
    fun onAvatarLongClickListener(): ((MessageView) -> Unit)?
    fun onReactionAddClickListener(): ((MessageView) -> Unit)?
    fun onReactionClickListener(): ((MessageView, ReactionView) -> Unit)?
}