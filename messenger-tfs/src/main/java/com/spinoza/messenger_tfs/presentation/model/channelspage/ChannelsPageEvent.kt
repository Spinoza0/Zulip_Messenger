package com.spinoza.messenger_tfs.presentation.model.channelspage

import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.model.ChannelItem

sealed class ChannelsPageEvent {

    sealed class Ui : ChannelsPageEvent() {

        object UpdateMessageCount : Ui()

        object Load : Ui()

        class Filter(val filter: ChannelsFilter) : Ui()

        class OnChannelClick(val value: ChannelItem) : Ui()

        class OnTopicClick(val messagesFilter: MessagesFilter) : Ui()
    }
}