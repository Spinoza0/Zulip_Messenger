package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentItemPeopleBinding
import com.spinoza.messenger_tfs.domain.usecase.GetAllUsersUseCase
import com.spinoza.messenger_tfs.presentation.adapter.user.UserAdapter
import com.spinoza.messenger_tfs.presentation.fragment.showError
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.state.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import com.spinoza.messenger_tfs.presentation.viewmodel.MainPeopleFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MainPeopleFragmentViewModelFactory
import kotlinx.coroutines.launch

class ItemPeopleFragment : Fragment() {

    private var _binding: FragmentItemPeopleBinding? = null
    private val binding: FragmentItemPeopleBinding
        get() = _binding ?: throw RuntimeException("FragmentItemPeopleBinding == null")

    private val viewModel: MainPeopleFragmentViewModel by viewModels {
        MainPeopleFragmentViewModelFactory(
            GetAllUsersUseCase(MessagesRepositoryImpl.getInstance()),
        )
    }

    private val adapter by lazy { UserAdapter(::onUserClickListener) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentItemPeopleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        if (savedInstanceState == null) {
            viewModel.loadAllUsers()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUsers.adapter = adapter
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
            binding.progressBar.off()
        }
        when (state) {
            is PeopleScreenState.Users -> adapter.submitList(state.value)
            is PeopleScreenState.Error -> requireContext().showError(state.value)
            is PeopleScreenState.Loading -> binding.progressBar.on()
        }
    }

    private fun onUserClickListener(userId: Long) {
        App.router.navigateTo(Screens.UserProfile(userId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ItemPeopleFragment {
            return ItemPeopleFragment()
        }
    }
}