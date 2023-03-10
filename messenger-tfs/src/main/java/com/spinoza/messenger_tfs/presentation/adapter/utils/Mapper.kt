package com.spinoza.messenger_tfs.presentation.adapter.utils

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.presentation.adapter.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.CompanionMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import java.util.*

fun List<Message>.groupByDate(
    user: User,
    onAvatarLongClickListener: ((MessageView) -> Unit)? = null,
    onReactionAddClickListener: ((MessageView) -> Unit)? = null,
    onReactionClickListener: ((MessageView) -> Unit)? = null,
): List<DelegateItem> {

    val delegateItemList = mutableListOf<DelegateItem>()
    val dates = TreeSet<MessageDate>()
    this.forEach {
        dates.add(it.date)
    }

    dates.forEach { messageDate ->
        delegateItemList.add(DateDelegateItem(messageDate.id, messageDate))
        val allDayMessages = this.filter { message ->
            message.date.value == messageDate.value
        }

        allDayMessages.forEach { message ->
            if (message.user.id == user.id) {
                delegateItemList.add(
                    UserMessageDelegateItem(
                        message.id,
                        message,
                        onAvatarLongClickListener,
                        onReactionAddClickListener,
                        onReactionClickListener
                    )
                )
            } else {
                delegateItemList.add(
                    CompanionMessageDelegateItem(
                        message.id, message,
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