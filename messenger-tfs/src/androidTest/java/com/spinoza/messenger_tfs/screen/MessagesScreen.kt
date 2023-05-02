package com.spinoza.messenger_tfs.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesFragment
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class MessagesScreen : KScreen<MessagesScreen>() {

    override val layoutId: Int = R.layout.fragment_messages
    override val viewClass: Class<*> = MessagesFragment::class.java

    val actionButton = KImageView { withId(R.id.imageViewAction) }
    val messagesList =
        KRecyclerView({ withId(R.id.recyclerViewMessages) }, { itemType(::MessageItem) })

    class MessageItem(parent: Matcher<View>) : KRecyclerItem<MessageItem>(parent) {
        val avatar = KImageView(parent) { withId(R.id.avatarImageView) }
        val name = KTextView(parent) { withId(R.id.nameTextView) }
        val content = KTextView(parent) { withId(R.id.contentTextView) }
        val reactions = KView(parent) { withId(R.id.reactionsFlexBoxLayout) }
    }
}