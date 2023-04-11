package org.kimp.tfs.hw7.presentation.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw7.databinding.FragmentPeopleBinding
import org.kimp.tfs.hw7.databinding.ShimmerUserItemBinding
import org.kimp.tfs.hw7.presentation.ShimmerItemAdapter
import org.kimp.tfs.hw7.presentation.base.SpacingItemDecoration
import org.kimp.tfs.hw7.presentation.people.adapter.UsersAdapter
import org.kimp.tfs.hw7.presentation.people.elm.Effect
import org.kimp.tfs.hw7.presentation.people.elm.Event
import org.kimp.tfs.hw7.presentation.people.elm.State
import timber.log.Timber
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.core.store.Store
import javax.inject.Inject

@AndroidEntryPoint
class PeopleFragment : ElmFragment<Event, Effect, State>() {
    private lateinit var binding: FragmentPeopleBinding

    @Inject lateinit var injectedStore: Store<Event, Effect, State>
    @Inject lateinit var imageLoader: ImageLoader

    private val shimmerAdapter = ShimmerItemAdapter(6, ShimmerUserItemBinding::class)
    private lateinit var adapter: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeopleBinding.inflate(
            inflater, container, false
        )

        store.accept(Event.Ui.UsersListRequested)

        binding.usersRecyclerView.addItemDecoration(SpacingItemDecoration(8))
        adapter = UsersAdapter(imageLoader)

        return binding.root
    }

    override fun render(state: State) {
        Timber.tag(TAG).i("Try to render state: $state")

        if (state.isLoading) {
            binding.contentShimmer.showShimmer(true)
            binding.contentShimmer.startShimmer()

            binding.usersRecyclerView.adapter = shimmerAdapter
        } else if (state.loadedUsers != null) {
            binding.contentShimmer.stopShimmer()
            binding.contentShimmer.hideShimmer()

            adapter.submitList(state.loadedUsers)
            binding.usersRecyclerView.adapter = adapter
        }
    }

    override val initEvent: Event = Event.Ui.FragmentInitialized

    override val storeHolder: StoreHolder<Event, Effect, State>
        get() = object : StoreHolder<Event, Effect, State> {
            override val isStarted: Boolean
                get() = this@PeopleFragment::injectedStore.isInitialized
            override val store: Store<Event, Effect, State>
                get() = this@PeopleFragment.injectedStore
        }

    companion object {
        const val TAG = "PeopleFragment"
    }
}
