package com.spinoza.messenger_tfs.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleFragment
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

class PeopleScreen : KScreen<PeopleScreen>() {

    override val layoutId: Int = R.layout.fragment_people
    override val viewClass: Class<*> = PeopleFragment::class.java

    val users =
        KRecyclerView({ withId(R.id.recyclerViewUsers) }, { itemType(::PeopleScreenItem) })

    class PeopleScreenItem(parent: Matcher<View>) : KRecyclerItem<PeopleScreenItem>(parent) {
        val textViewName = KTextView(parent) { withId(R.id.textViewName) }
    }
}