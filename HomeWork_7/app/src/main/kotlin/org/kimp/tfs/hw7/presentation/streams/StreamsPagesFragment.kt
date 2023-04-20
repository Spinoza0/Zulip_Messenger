package org.kimp.tfs.hw7.presentation.streams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw7.R
import org.kimp.tfs.hw7.databinding.FragmentStreamsPagesBinding
import org.kimp.tfs.hw7.presentation.streams.adapter.StreamsPageAdapter

@AndroidEntryPoint
class StreamsPagesFragment : Fragment() {

    private lateinit var binding: FragmentStreamsPagesBinding
    private lateinit var pagesAdapter: StreamsPageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStreamsPagesBinding.inflate(
            layoutInflater, container, false
        )

        pagesAdapter = StreamsPageAdapter(
            childFragmentManager,
            lifecycle,
            listOf(
                StreamsFragment.newInstance(true),
                StreamsFragment.newInstance(false),
            )
        )
        binding.streamsPager.adapter = pagesAdapter

        val titles = listOf(
            getString(R.string.streams_fragment_subscribed_tab),
            getString(R.string.streams_fragment_all_tab)
        )

        TabLayoutMediator(
            binding.tabLayout, binding.streamsPager
        ) { tab, pos -> tab.text = titles[pos] }.attach()

        return binding.root
    }
}
