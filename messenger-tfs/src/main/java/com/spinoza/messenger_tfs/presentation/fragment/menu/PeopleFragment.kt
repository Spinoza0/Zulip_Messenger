package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentPeopleBinding
import com.spinoza.messenger_tfs.domain.usecase.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetPresenceEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.adapter.people.PeopleAdapter
import com.spinoza.messenger_tfs.presentation.fragment.showError
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.state.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import com.spinoza.messenger_tfs.presentation.viewmodel.PeopleFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.PeopleFragmentViewModelFactory
import kotlinx.coroutines.launch

class PeopleFragment : Fragment() {

    private val globalRouter = App.router

    private var _binding: FragmentPeopleBinding? = null
    private val binding: FragmentPeopleBinding
        get() = _binding ?: throw RuntimeException("FragmentPeopleBinding == null")

    private val viewModel: PeopleFragmentViewModel by viewModels {
        PeopleFragmentViewModelFactory(
            GetUsersByFilterUseCase(MessagesRepositoryImpl.getInstance()),
            RegisterEventQueueUseCase(MessagesRepositoryImpl.getInstance()),
            DeleteEventQueueUseCase(MessagesRepositoryImpl.getInstance()),
            GetPresenceEventsUseCase(MessagesRepositoryImpl.getInstance()),
        )
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
        setupObservers()
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
                        viewModel.loadUsers()
                    }
                }
            }
        })
    }

    private fun setupListeners() {
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.doOnTextChanged(text)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleState)
            }
        }
    }

    private fun handleState(state: PeopleScreenState) {
        if (state !is PeopleScreenState.Loading) {
            binding.shimmerLarge.off()
        }
        when (state) {
            is PeopleScreenState.Users ->
                (binding.recyclerViewUsers.adapter as PeopleAdapter).submitList(state.value)
            is PeopleScreenState.Loading -> if (peopleListIsEmpty()) {
                binding.shimmerLarge.on()
            }
            is PeopleScreenState.Failure.LoadingUsers -> showError(
                String.format(
                    getString(R.string.error_loading_users),
                    state.value
                )
            )
            is PeopleScreenState.Start -> viewModel.setUsersFilter(NO_FILTER)
        }
    }

    private fun peopleListIsEmpty(): Boolean {
        return (binding.recyclerViewUsers.adapter as PeopleAdapter).itemCount == NO_ITEMS
    }

    private fun onUserClickListener(userId: Long) {
        globalRouter.navigateTo(Screens.UserProfile(userId))
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

        private const val NO_ITEMS = 0
        private const val NO_FILTER = ""

        fun newInstance(): PeopleFragment {
            return PeopleFragment()
        }
    }
}