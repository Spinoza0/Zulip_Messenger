package com.spinoza.messenger_tfs.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.feature.channels.ChannelsPageFragment
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class ChannelsPageScreen : KScreen<ChannelsPageScreen>() {

    override val layoutId: Int = R.layout.fragment_channels_page
    override val viewClass: Class<*> = ChannelsPageFragment::class.java

    val channels = KRecyclerView(
        { withId(R.id.recyclerViewChannels) },
        {
            itemType(::ChannelScreenItem)
            itemType(::TopicScreenItem)
        }
    )

    val channelUnsubscribe = KView { withId(R.id.textViewUnsubscribe) }

    class ChannelScreenItem(parent: Matcher<View>) : KRecyclerItem<ChannelScreenItem>(parent) {
        val channel = KTextView(parent) { withId(R.id.textViewChannel) }
        val arrowArea = KTextView(parent) { withId(R.id.textViewArrowArea) }
    }

    class TopicScreenItem(parent: Matcher<View>) : KRecyclerItem<TopicScreenItem>(parent) {
        val topic = KTextView(parent) { withId(R.id.textViewTopicName) }
    }
}