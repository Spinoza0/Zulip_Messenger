package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentProfileBinding
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUserUseCase
import com.spinoza.messenger_tfs.presentation.model.ProfileFragmentState
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
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
                viewModel.state.collect(::handleState)
            }
        }
    }

    private fun setupScreen() {
        binding.toolbar.visibility = View.VISIBLE
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.background_700_color)
        viewModel.loadUser(userId)
    }

    private fun handleState(state: ProfileFragmentState) {
        if (state !is ProfileFragmentState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is ProfileFragmentState.UserData -> showProfileInfo(state.value)
            is ProfileFragmentState.Loading -> binding.progressBar.on()
            is ProfileFragmentState.Error -> requireContext().showError(state.value)
        }
    }

    private fun showProfileInfo(user: User) {
        with(binding) {
            textViewName.text = user.full_name
            textViewStatus.text = user.status
            if (user.status.isEmpty()) {
                textViewStatus.visibility = View.GONE
            } else {
                textViewStatus.text = user.status
            }
            if (user.isActive) {
                textViewStatusOnline.visibility = View.VISIBLE
                textViewStatusOffline.visibility = View.GONE
            } else {
                textViewStatusOnline.visibility = View.GONE
                textViewStatusOffline.visibility = View.VISIBLE
            }
            com.bumptech.glide.Glide.with(imageViewAvatar)
                .load(user.avatar_url)
                .transform(RoundedCorners(20))
                .error(R.drawable.ic_default_avatar)
                .into(imageViewAvatar)
        }
    }

    private fun goBack() {
        App.router.exit()
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

        fun newInstance(userId: Long): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_USER_ID, userId)
                }
            }
        }
    }
}