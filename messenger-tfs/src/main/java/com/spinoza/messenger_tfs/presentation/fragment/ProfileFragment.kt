package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentProfileBinding
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.presentation.elm.ProfileActor
import com.spinoza.messenger_tfs.presentation.elm.provideProfileStore
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEffect
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEvent
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileState
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.core.store.Store

open class ProfileFragment : ElmFragment<ProfileEvent, ProfileEffect, ProfileState>() {

    protected val binding: FragmentProfileBinding
        get() = _binding ?: throw RuntimeException("FragmentProfileBinding == null")

    private var _binding: FragmentProfileBinding? = null

    override val initEvent: ProfileEvent
        get() = ProfileEvent.Ui.Init

    override fun createStore(): Store<ProfileEvent, ProfileEffect, ProfileState>? {
        return provideProfileStore(ProfileActor(lifecycleScope))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun render(state: ProfileState) {
        if (state.isLoading) {
            binding.shimmer.on()
        } else {
            binding.shimmer.off()
        }
        if (state.user != null) {
            showProfileInfo(state.user)
        }
    }

    override fun handleEffect(effect: ProfileEffect) {
        when (effect) {
            is ProfileEffect.Failure.ErrorUserLoading -> showError(
                String.format(getString(R.string.error_user_loading), effect.value)
            )
            is ProfileEffect.Failure.ErrorNetwork -> showError(
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
}