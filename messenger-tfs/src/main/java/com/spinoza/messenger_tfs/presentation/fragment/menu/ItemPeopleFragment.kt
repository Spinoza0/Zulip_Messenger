package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.MessengerApp
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentItemPeopleBinding
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetAllUsersUseCase
import com.spinoza.messenger_tfs.presentation.adapter.user.UserAdapter
import com.spinoza.messenger_tfs.presentation.cicerone.Screens
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

    val adapter by lazy { UserAdapter(::onUserClickListener) }

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
        viewModel.getAllUsers()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUsers.adapter = adapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleFragmentState)
            }
        }
    }

    private fun handleFragmentState(state: RepositoryState) {
        if (state !is RepositoryState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is RepositoryState.Users -> adapter.submitList(state.value)
            is RepositoryState.Error -> {
                Toast.makeText(context, state.text, Toast.LENGTH_LONG).show()
            }
            is RepositoryState.Loading -> binding.progressBar.on()
            else -> {}
        }
    }

    private fun onUserClickListener(userId: Long) {
        MessengerApp.router.navigateTo(Screens.UserProfile(userId))
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