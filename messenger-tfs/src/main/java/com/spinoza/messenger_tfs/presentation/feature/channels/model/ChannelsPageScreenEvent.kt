package com.spinoza.messenger_tfs.presentation.feature.channels.model

import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class ChannelsPageScreenEvent {

    sealed class Ui : ChannelsPageScreenEvent() {

        object CheckLoginStatus : Ui()

        object UpdateMessageCount : Ui()

        object Load : Ui()

        object RegisterEventQueue : Ui()

        object DeleteEventQueue : Ui()

        class Filter(val filter: ChannelsFilter) : Ui()

        class OnChannelClick(val value: ChannelItem) : Ui()

        class OnTopicClick(val messagesFilter: MessagesFilter) : Ui()

        class OnScrolled(val canScrollUp: Boolean, val canScrollDown: Boolean, val dy: Int) : Ui()
    }

    companion object {

        const val DIRECTION_UP = -1
        const val DIRECTION_DOWN = 1
    }
}