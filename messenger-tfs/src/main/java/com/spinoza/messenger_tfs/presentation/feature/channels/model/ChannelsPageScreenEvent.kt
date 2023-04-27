package com.spinoza.messenger_tfs.presentation.feature.channels.model

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class ChannelsPageScreenEvent {

    sealed class Ui : ChannelsPageScreenEvent() {

        object UpdateMessageCount : Ui()

        object Load : Ui()

        object RegisterEventQueue : Ui()

        object DeleteEventQueue : Ui()

        class Filter(val filter: ChannelsFilter) : Ui()

        class OnChannelClick(val value: ChannelItem) : Ui()

        class OnTopicClick(val messagesFilter: MessagesFilter) : Ui()

        class OnScrolled(val recyclerView: RecyclerView, val dy: Int) : Ui()    }
}