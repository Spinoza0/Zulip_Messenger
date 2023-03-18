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
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMainPeopleBinding
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetAllUsersUseCase
import com.spinoza.messenger_tfs.presentation.adapter.user.UserAdapter
import com.spinoza.messenger_tfs.presentation.viewmodel.MainPeopleFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MainPeopleFragmentViewModelFactory
import kotlinx.coroutines.launch

class MainPeopleFragment : Fragment() {

    private var _binding: FragmentMainPeopleBinding? = null
    private val binding: FragmentMainPeopleBinding
        get() = _binding ?: throw RuntimeException("FragmentMainPeopleBinding == null")

    private val viewModel: MainPeopleFragmentViewModel by viewModels {
        MainPeopleFragmentViewModelFactory(
            GetAllUsersUseCase(MessagesRepositoryImpl.getInstance()),
        )
    }

    val adapter by lazy { UserAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainPeopleBinding.inflate(inflater, container, false)
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
        when (state) {
            is RepositoryState.Users -> adapter.submitList(state.value)
            is RepositoryState.Error -> {
                Toast.makeText(context, state.text, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): MainPeopleFragment {
            return MainPeopleFragment()
        }
    }
}