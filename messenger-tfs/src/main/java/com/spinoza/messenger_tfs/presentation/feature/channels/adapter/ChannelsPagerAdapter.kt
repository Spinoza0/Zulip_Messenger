package com.spinoza.messenger_tfs.presentation.feature.channels.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.spinoza.messenger_tfs.presentation.feature.channels.ChannelsPageFragment

class ChannelsPagerAdapter(
    parentFragment: Fragment,
) : FragmentStateAdapter(parentFragment) {

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount() = ITEM_COUNT

    override fun createFragment(position: Int): Fragment {
        val isSubscribed = position % 2 == 0
        return ChannelsPageFragment.newInstance(isSubscribed)
    }

    private companion object {
        const val ITEM_COUNT = 2
    }
}