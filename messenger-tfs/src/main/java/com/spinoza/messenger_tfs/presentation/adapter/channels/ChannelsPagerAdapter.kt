package com.spinoza.messenger_tfs.presentation.adapter.channels

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.spinoza.messenger_tfs.presentation.fragment.menu.ChannelsPageFragment

class ChannelsPagerAdapter(
    parentFragment: Fragment,
) : FragmentStateAdapter(parentFragment) {

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount() = ITEM_COUNT

    override fun createFragment(position: Int): Fragment {
        val isAllChannels = position % 2 != 0
        return ChannelsPageFragment.newInstance(isAllChannels)
    }

    private companion object {
        const val ITEM_COUNT = 2
    }
}