package org.kimp.tfs.hw7.presentation.streams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw7.databinding.FragmentStreamsBinding
import org.kimp.tfs.hw7.databinding.ShimmerStreamItemBinding
import org.kimp.tfs.hw7.presentation.ShimmerItemAdapter
import org.kimp.tfs.hw7.presentation.streams.adapter.StreamsAdapter
import org.kimp.tfs.hw7.presentation.streams.elm.Effect
import org.kimp.tfs.hw7.presentation.streams.elm.Event
import org.kimp.tfs.hw7.presentation.streams.elm.State
import timber.log.Timber
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.core.store.Store
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class StreamsFragment : ElmFragment<Event, Effect, State>() {
    private lateinit var binding: FragmentStreamsBinding
    private var subscribedOnly by Delegates.notNull<Boolean>()

    @Inject lateinit var injectedStore: Store<Event, Effect, State>

    private val shimmerAdapter = ShimmerItemAdapter(3, ShimmerStreamItemBinding::class)
    private val streamsAdapter = StreamsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStreamsBinding.inflate(
            inflater, container, false
        )

        subscribedOnly = requireArguments().getBoolean(SUBSCRIBED_ONLY)
        store.accept(Event.Ui.ChannelsRequested(subscribedOnly))

        return binding.root
    }

    override fun render(state: State) {
        Timber.tag(TAG).i("Try to render state: $state")

        if (state.isLoading) {
            binding.contentShimmer.showShimmer(true)
            binding.contentShimmer.startShimmer()

            binding.streamsAndTopicsRecyclerView.adapter = shimmerAdapter
        } else if (state.loadedChannels != null) {
            binding.contentShimmer.stopShimmer()
            binding.contentShimmer.hideShimmer()

            streamsAdapter.submitList(state.loadedChannels.toList())
            binding.streamsAndTopicsRecyclerView.adapter = streamsAdapter
        }
    }

    override val initEvent: Event = Event.Ui.FragmentInitialized

    override val storeHolder: StoreHolder<Event, Effect, State>
        get() = object : StoreHolder<Event, Effect, State> {
            override val isStarted: Boolean
                get() = this@StreamsFragment::injectedStore.isInitialized
            override val store: Store<Event, Effect, State>
                get() = this@StreamsFragment.injectedStore
        }

    companion object {
        const val TAG = "StreamsFragment"

        private const val SUBSCRIBED_ONLY = "SUBSCRIBED_ONLY"

        fun newInstance(subscribedOnly: Boolean) = StreamsFragment().also {
            it.arguments = Bundle().apply {
                putBoolean(SUBSCRIBED_ONLY, subscribedOnly)
            }
        }
    }
}
