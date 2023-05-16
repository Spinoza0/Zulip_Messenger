package com.spinoza.messenger_tfs.presentation.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentProfileBinding
import com.spinoza.messenger_tfs.di.profile.DaggerProfileComponent
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenState
import com.spinoza.messenger_tfs.presentation.util.getAppComponent
import com.spinoza.messenger_tfs.presentation.util.getParam
import com.spinoza.messenger_tfs.presentation.util.off
import com.spinoza.messenger_tfs.presentation.util.on
import com.spinoza.messenger_tfs.presentation.util.showError
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.coroutines.ElmStoreCompat
import javax.inject.Inject

open class ProfileFragment :
    ElmFragment<ProfileScreenEvent, ProfileScreenEffect, ProfileScreenState>() {

    @Inject
    lateinit var profileStore: ElmStoreCompat<
            ProfileScreenEvent,
            ProfileScreenState,
            ProfileScreenEffect,
            ProfileScreenCommand>

    protected val binding: FragmentProfileBinding
        get() = _binding ?: throw RuntimeException("FragmentProfileBinding == null")

    private var _binding: FragmentProfileBinding? = null

    override val initEvent: ProfileScreenEvent
        get() = ProfileScreenEvent.Ui.Idle

    override val storeHolder:
            StoreHolder<ProfileScreenEvent, ProfileScreenEffect, ProfileScreenState> by lazy {
        LifecycleAwareStoreHolder(lifecycle) {
            val initialState = savedStateRegistry.consumeRestoredStateForKey(PARAM_STATE)
                ?.getParam<ProfileScreenState>(PARAM_STATE)
                ?: ProfileScreenState()
            savedStateRegistry.unregisterSavedStateProvider(PARAM_STATE)
            DaggerProfileComponent.factory()
                .create(requireContext().getAppComponent(), lifecycle, initialState)
                .inject(this)
            profileStore
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun render(state: ProfileScreenState) {
        if (state.isLoading) {
            binding.shimmer.on()
        } else {
            binding.shimmer.off()
        }
        if (state.user != null) {
            showProfileInfo(state.user)
        }
    }

    override fun handleEffect(effect: ProfileScreenEffect) {
        when (effect) {
            is ProfileScreenEffect.Failure.ErrorUserLoading ->
                showError(getString(R.string.error_user_loading), effect.value)

            is ProfileScreenEffect.Failure.ErrorNetwork ->
                showError(getString(R.string.error_network), effect.value)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedStateRegistry.registerSavedStateProvider(PARAM_STATE) {
            Bundle().apply { putParcelable(PARAM_STATE, store.currentState) }
        }
    }

    private companion object {

        const val PARAM_STATE = "state"
    }
}