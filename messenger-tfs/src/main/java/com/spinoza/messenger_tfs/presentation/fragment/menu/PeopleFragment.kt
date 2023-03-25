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
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentPeopleBinding
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.presentation.adapter.people.PeopleAdapter
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
        if (savedInstanceState == null) {
            viewModel.loadUsers()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUsers.adapter = PeopleAdapter(::onUserClickListener)
    }

    private fun setupListeners() {
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.doOnTextChanged(text)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect(::handleState)
            }
        }
    }

    private fun handleState(state: PeopleScreenState) {
        if (state !is PeopleScreenState.Loading) {
            binding.shimmer.off()
        }
        when (state) {
            is PeopleScreenState.Users -> {
                (binding.recyclerViewUsers.adapter as PeopleAdapter).submitList(state.value)
            }
            is PeopleScreenState.Filter -> {
                viewModel.setUsersFilter(state.value)
                viewModel.loadUsers()
            }
            is PeopleScreenState.Loading -> binding.shimmer.on()
            is PeopleScreenState.Idle -> {}
        }
    }

    private fun onUserClickListener(userId: Long) {
        globalRouter.navigateTo(Screens.UserProfile(userId))
    }

    override fun onPause() {
        super.onPause()
        binding.shimmer.off()
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