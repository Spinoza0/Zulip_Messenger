package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentProfileBinding
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.fragment.showError
import com.spinoza.messenger_tfs.presentation.state.ProfileScreenState
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import com.spinoza.messenger_tfs.presentation.viewmodel.ProfileFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ProfileFragmentViewModelFactory
import kotlinx.coroutines.launch

class OwnUserProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding ?: throw RuntimeException("FragmentProfileBinding == null")


    private val viewModel: ProfileFragmentViewModel by viewModels {
        ProfileFragmentViewModelFactory(
            GetOwnUserUseCase(MessagesRepositoryImpl.getInstance()),
            GetUserUseCase(MessagesRepositoryImpl.getInstance()),
            RegisterEventQueueUseCase(MessagesRepositoryImpl.getInstance()),
            DeleteEventQueueUseCase(MessagesRepositoryImpl.getInstance()),
            GetPresenceEventsUseCase(MessagesRepositoryImpl.getInstance()),
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

        setupObservers()

        if (savedInstanceState == null) {
            viewModel.loadCurrentUser()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleState)
            }
        }
    }

    private fun handleState(state: ProfileScreenState) {
        if (state !is ProfileScreenState.Loading) {
            binding.shimmer.off()
        }
        when (state) {
            is ProfileScreenState.UserData -> showProfileInfo(state.value)
            is ProfileScreenState.Loading -> binding.shimmer.on()
            is ProfileScreenState.Failure.UserNotFound -> showError(
                String.format(getString(R.string.error_user_not_found), state.userId, state.value)
            )
            is ProfileScreenState.Failure.Network -> showError(
                String.format(getString(R.string.error_network), state.value)
            )
            is ProfileScreenState.Presence -> showPresence(state.value)
            is ProfileScreenState.Idle -> {}
        }
    }

    private fun showProfileInfo(user: User) {
        binding.textViewName.text = user.fullName
        showPresence(user.presence)
        com.bumptech.glide.Glide.with(binding.imageViewAvatar)
            .load(user.avatarUrl)
            .transform(RoundedCorners(20))
            .error(R.drawable.ic_default_avatar)
            .into(binding.imageViewAvatar)
    }

    private fun showPresence(presence: User.Presence) {
        binding.textViewStatusActive.isVisible = presence == User.Presence.ACTIVE
        binding.textViewStatusIdle.isVisible = presence == User.Presence.IDLE
        binding.textViewStatusOffline.isVisible = presence == User.Presence.OFFLINE
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

        fun newInstance(): OwnUserProfileFragment {
            return OwnUserProfileFragment()
        }
    }
}