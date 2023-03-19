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
import com.spinoza.messenger_tfs.presentation.viewmodel.ProfileFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ProfileFragmentViewModelFactory
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

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

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
        }
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
        setupListeners()
        setupObservers()
        setupScreen()
        viewModel.getUserInfo(userId)
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
    }

    private fun handleRepositoryState(state: RepositoryState) {
        when (state) {
            is RepositoryState.Users -> {
                TODO()
            }
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

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )

//        viewModel.doOnTextChanged(binding.editTextMessage.text)
//        viewModel.getMessages()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.remove()
        _binding = null
    }

    companion object {

        private const val EXTRA_USER_ID = "userId"
        private const val UNDEFINED_USER_ID = -1L

        fun newInstance(userId: Long): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_USER_ID, userId)
                }
            }
        }
    }
}