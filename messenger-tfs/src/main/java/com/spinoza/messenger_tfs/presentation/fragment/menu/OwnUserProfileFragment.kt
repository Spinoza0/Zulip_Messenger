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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentProfileBinding
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.fragment.showError
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEffect
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEvent
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileState
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
            App.router,
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
            viewModel.reduce(ProfileEvent.Ui.LoadCurrentUser)
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

    private fun handleState(state: ProfileState) {
        if (state.isLoading) {
            binding.shimmer.on()
        } else {
            binding.shimmer.off()
        }
        if (state.user != null) {
            showProfileInfo(state.user)
        }
    }

    private fun handleEffect(effect: ProfileEffect) {
        when (effect) {
            is ProfileEffect.Failure.UserNotFound -> showError(
                String.format(getString(R.string.error_user_not_found), effect.userId, effect.value)
            )
            is ProfileEffect.Failure.Network -> showError(
                String.format(getString(R.string.error_network), effect.value)
            )
        }
    }

    private fun showProfileInfo(user: User) {
        with(binding) {
            textViewName.text = user.fullName
            textViewStatusActive.isVisible = user.presence == User.Presence.ACTIVE
            textViewStatusIdle.isVisible = user.presence == User.Presence.IDLE
            textViewStatusOffline.isVisible = user.presence == User.Presence.OFFLINE
            Glide.with(imageViewAvatar)
                .load(user.avatarUrl)
                .transform(RoundedCorners(20))
                .error(R.drawable.ic_default_avatar)
                .into(imageViewAvatar)
        }
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