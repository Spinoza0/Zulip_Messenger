package org.kimp.tfs.hw7.presentation.streams.adapter

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.kimp.tfs.hw7.presentation.streams.StreamsFragment

class StreamsPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val fragments: List<StreamsFragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}
