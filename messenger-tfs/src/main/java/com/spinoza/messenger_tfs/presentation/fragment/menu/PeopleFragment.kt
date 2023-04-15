package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.di.GlobalDI
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentPeopleBinding
import com.spinoza.messenger_tfs.presentation.adapter.people.PeopleAdapter
import com.spinoza.messenger_tfs.presentation.elmstore.PeopleActor
import com.spinoza.messenger_tfs.presentation.fragment.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.fragment.showError
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEvent
import com.spinoza.messenger_tfs.presentation.model.people.PeopleScreenEffect
import com.spinoza.messenger_tfs.presentation.model.people.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder

class PeopleFragment : ElmFragment<PeopleEvent, PeopleScreenEffect, PeopleScreenState>() {

    private var _binding: FragmentPeopleBinding? = null
    private val binding: FragmentPeopleBinding
        get() = _binding ?: throw RuntimeException("FragmentPeopleBinding == null")

    override val initEvent: PeopleEvent
        get() = PeopleEvent.Ui.Init

    override val storeHolder:
            StoreHolder<PeopleEvent, PeopleScreenEffect, PeopleScreenState> by lazy {
        LifecycleAwareStoreHolder(lifecycle) {
            GlobalDI.INSTANCE.providePeopleStore(PeopleActor(lifecycle))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPeopleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUsers.adapter = PeopleAdapter(::onUserClickListener)
        binding.recyclerViewUsers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastItem = layoutManager.itemCount - 1
                val firstItem = 0
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (lastVisibleItemPosition == lastItem ||
                        firstVisibleItemPosition == firstItem
                    ) {
                        store.accept(PeopleEvent.Ui.Load)
                    }
                }
            }
        })
    }

    private fun setupListeners() {
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            store.accept(PeopleEvent.Ui.Filter(text.toString()))
        }
    }

    override fun render(state: PeopleScreenState) {
        if (state.isLoading) {
            binding.shimmerLarge.on()
        } else {
            binding.shimmerLarge.off()
        }
        state.users?.let {
            (binding.recyclerViewUsers.adapter as PeopleAdapter).submitList(it) {
                binding.recyclerViewUsers.scrollToPosition(FIRST_ITEM)
            }
        }
    }

    override fun handleEffect(effect: PeopleScreenEffect) {
        when (effect) {
            is PeopleScreenEffect.Failure.ErrorLoadingUsers ->
                showError(String.format(getString(R.string.error_loading_users), effect.value))
            is PeopleScreenEffect.Failure.ErrorNetwork ->
                showCheckInternetConnectionDialog(
                    { store.accept(PeopleEvent.Ui.Load) }
                ) {
                    store.accept(PeopleEvent.Ui.OpenMainMenu)
                }
        }
    }

    private fun onUserClickListener(userId: Long) {
        store.accept(PeopleEvent.Ui.ShowUserInfo(userId))
    }

    override fun onResume() {
        super.onResume()
        store.accept(PeopleEvent.Ui.Filter(binding.editTextSearch.text.toString()))
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val FIRST_ITEM = 0

        fun newInstance(): PeopleFragment {
            return PeopleFragment()
        }
    }
}