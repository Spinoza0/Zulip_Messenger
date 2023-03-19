package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.MessengerApp
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentProfileBinding
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUserUseCase
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import com.spinoza.messenger_tfs.presentation.ui.setup
import com.spinoza.messenger_tfs.presentation.viewmodel.ProfileFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ProfileFragmentViewModelFactory
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding
            ?: throw RuntimeException("FragmentProfileBinding == null")

    private var userId = UNDEFINED_USER_ID

    private val viewModel: ProfileFragmentViewModel by viewModels {
        ProfileFragmentViewModelFactory(
            GetCurrentUserUseCase(MessagesRepositoryImpl.getInstance()),
            GetUserUseCase(MessagesRepositoryImpl.getInstance())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parseParams()
        setupOnBackPressedCallback()
        setupListeners()
        setupObservers()
        setupScreen()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            goBack()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleRepositoryState)
            }
        }
    }

    private fun setupScreen() {
        binding.toolbar.visibility = View.VISIBLE
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.background_700_color)
        viewModel.getUserInfo(userId)
    }

    private fun handleRepositoryState(state: RepositoryState) {
        if (state !is RepositoryState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is RepositoryState.Users -> {
                val user = state.value[FIRST_VALUE]
                binding.setup(user)
            }
            is RepositoryState.Loading -> binding.progressBar.on()
            is RepositoryState.Error -> {
                when (state.type) {
                    RepositoryState.ErrorType.USER_WITH_ID_NOT_FOUND -> {
                        val text = String.format(
                            getString(R.string.error_user_not_found),
                            state.text
                        )
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> {}
        }
    }

    private fun goBack() {
        MessengerApp.router.exit()
    }

    private fun parseParams() {
        userId = arguments?.getLong(EXTRA_USER_ID, UNDEFINED_USER_ID) ?: UNDEFINED_USER_ID
        if (userId == UNDEFINED_USER_ID) {
            goBack()
        }
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )
    }

    override fun onStop() {
        super.onStop()
        onBackPressedCallback.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val EXTRA_USER_ID = "userId"
        private const val UNDEFINED_USER_ID = -1L
        private const val FIRST_VALUE = 0

        fun newInstance(userId: Long): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_USER_ID, userId)
                }
            }
        }
    }
}