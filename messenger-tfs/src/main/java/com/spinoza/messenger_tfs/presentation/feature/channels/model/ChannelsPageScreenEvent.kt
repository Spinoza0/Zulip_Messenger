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

        class OpenMessagesScreen(val messagesFilter: MessagesFilter) : Ui()

        object OnScrolled : Ui()

        object ScrollStateDragging : Ui()

        class ScrollStateIdle(val canScrollUp: Boolean, val canScrollDown: Boolean) : Ui()

        class CreateChannel(val name: String, val description: String) : Ui()
    }
}