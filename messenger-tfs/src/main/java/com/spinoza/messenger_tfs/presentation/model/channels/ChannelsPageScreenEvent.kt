package com.spinoza.messenger_tfs.presentation.model.channels

import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class ChannelsPageScreenEvent {

    sealed class Ui : ChannelsPageScreenEvent() {

        object UpdateMessageCount : Ui()

        object Load : Ui()

        class Filter(val filter: ChannelsFilter) : Ui()

        class OnChannelClick(val value: ChannelItem) : Ui()

        class OnTopicClick(val messagesFilter: MessagesFilter) : Ui()
    }
}