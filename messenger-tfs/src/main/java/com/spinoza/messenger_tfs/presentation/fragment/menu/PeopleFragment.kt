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
import com.spinoza.messenger_tfs.presentation.fragment.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.fragment.showError
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEffect
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEvent
import com.spinoza.messenger_tfs.presentation.model.people.PeopleState
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import com.spinoza.messenger_tfs.presentation.viewmodel.PeopleFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.PeopleFragmentViewModelFactory
import kotlinx.coroutines.launch

class PeopleFragment : Fragment() {

    private var _binding: FragmentPeopleBinding? = null
    private val binding: FragmentPeopleBinding
        get() = _binding ?: throw RuntimeException("FragmentPeopleBinding == null")

    private val viewModel: PeopleFragmentViewModel by viewModels {
        PeopleFragmentViewModelFactory(
            App.router,
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
                        viewModel.reduce(PeopleEvent.Ui.Load)
                    }
                }
            }
        })
    }

    private fun setupListeners() {
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.reduce(PeopleEvent.Ui.Filter(text.toString()))
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleState)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect(::handleEffect)
            }
        }
    }

    private fun handleState(state: PeopleState) {
        if (state.isLoading) {
            binding.shimmerLarge.on()
        } else {
            binding.shimmerLarge.off()
        }
        state.users?.let {
            (binding.recyclerViewUsers.adapter as PeopleAdapter).submitList(it)
        }
    }

    private fun handleEffect(effect: PeopleEffect) {
        when (effect) {
            is PeopleEffect.Failure.LoadingUsers ->
                showError(String.format(getString(R.string.error_loading_users), effect.value))
            is PeopleEffect.Failure.Network ->
                showCheckInternetConnectionDialog({ viewModel.reduce(PeopleEvent.Ui.Load) }) {
                    viewModel.reduce(PeopleEvent.Ui.OpenMainMenu)
                }
        }
    }

    private fun onUserClickListener(userId: Long) {
        viewModel.reduce(PeopleEvent.Ui.ShowUserInfo(userId))
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

        fun newInstance(): PeopleFragment {
            return PeopleFragment()
        }
    }
}