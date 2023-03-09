package com.spinoza.messenger_tfs.presentation.adapter.utils

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.presentation.adapter.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.CompanionMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.UserMessageDelegateItem
import java.util.*

fun List<Message>.groupByDate(user: User): List<DelegateItem> {

    val delegateItemList = mutableListOf<DelegateItem>()
    val dates = TreeSet<MessageDate>()
    this.forEach {
        dates.add(it.date)
    }

    dates.forEach { messageDate ->
        delegateItemList.add(DateDelegateItem(messageDate.id, messageDate))
        val date = messageDate.value
        val allDayMessages = this.filter { message ->
            message.date.value == date
        }

        allDayMessages.forEach { message ->
            if (message.user.id == user.id) {
                delegateItemList.add(UserMessageDelegateItem(message.id, message))
            } else {
                delegateItemList.add(CompanionMessageDelegateItem(message.id, message))
            }
        }
    }

    return delegateItemList
}