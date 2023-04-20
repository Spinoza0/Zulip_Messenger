package com.spinoza.messenger_tfs.presentation.feature.profile

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.getThemeColor

class UserProfileFragment : ProfileFragment() {

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var userId = UNDEFINED_USER_ID

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseParams()
        setupListeners()
        setupScreen()
        if (savedInstanceState == null) {
            store.accept(ProfileScreenEvent.Ui.LoadUser(userId))
        } else {
            store.accept(ProfileScreenEvent.Ui.SubscribePresence(store.currentState.user))
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            store.accept(ProfileScreenEvent.Ui.GoBack)
        }
    }

    private fun setupScreen() {
        binding.toolbar.isVisible = true
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.background_700_color)
    }

    private fun parseParams() {
        userId = arguments?.getLong(EXTRA_USER_ID, UNDEFINED_USER_ID) ?: UNDEFINED_USER_ID
        if (userId == UNDEFINED_USER_ID) {
            store.accept(ProfileScreenEvent.Ui.GoBack)
        }
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                store.accept(ProfileScreenEvent.Ui.GoBack)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onStart() {
        super.onStart()
        setupOnBackPressedCallback()
    }

    override fun onStop() {
        super.onStop()
        onBackPressedCallback.remove()
    }

    companion object {

        private const val EXTRA_USER_ID = "userId"
        private const val UNDEFINED_USER_ID = -1L

        fun newInstance(userId: Long): UserProfileFragment {
            return UserProfileFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_USER_ID, userId)
                }
            }
        }
    }
}