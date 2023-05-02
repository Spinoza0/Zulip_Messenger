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

    val messagesList =
        KRecyclerView({ withId(R.id.recyclerViewMessages) }, { itemType(::MessageItem) })
    val errorMessage = KTextView { withText(R.string.check_internet_connection) }
    val chooseReactionDialogTopLine = KView { withId(R.id.textViewTopLine) }

    class MessageItem(parent: Matcher<View>) : KRecyclerItem<MessageItem>(parent) {
        val iconAddReaction = KImageView(parent) { withId(R.id.flexbox_icon_add) }
    }
}