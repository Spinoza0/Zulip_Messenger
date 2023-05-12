package com.spinoza.messenger_tfs.presentation.feature.channels.model

import android.view.View
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class ChannelsPageScreenEvent {

    sealed class Ui : ChannelsPageScreenEvent() {

        object CheckLoginStatus : Ui()

        object UpdateMessageCount : Ui()

        object Load : Ui()

        class Filter(val filter: ChannelsFilter) : Ui()

        class OnChannelClick(val value: ChannelItem) : Ui()

        class OpenMessagesScreen(val messagesFilter: MessagesFilter) : Ui()

        object OnScrolled : Ui()

        object ScrollStateDragging : Ui()

        class ScrollStateIdle(val canScrollUp: Boolean, val canScrollDown: Boolean) : Ui()

        class CreateChannel(val name: CharSequence?, val description: CharSequence?) : Ui()

        class ShowChannelMenu(val channelItem: ChannelItem, val view: View) : Ui()

        class SubscribeToChannel(val name: String) : Ui()

        class UnsubscribeFromChannel(val name: String) : Ui()

        class DeleteChannel(val channelId: Long) : Ui()
    }
}