package com.spinoza.messenger_tfs.presentation.adapter.message

import com.spinoza.messenger_tfs.presentation.ui.MessageView

interface OnMessageClickListeners {
    fun onAvatarLongClickListener(): ((MessageView) -> Unit)?
    fun onReactionAddClickListener(): ((MessageView) -> Unit)?
    fun onReactionClickListener(): ((MessageView) -> Unit)?
}