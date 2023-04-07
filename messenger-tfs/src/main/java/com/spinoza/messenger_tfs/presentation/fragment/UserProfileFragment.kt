package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.model.profilescreen.ProfileEvent
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor

class UserProfileFragment : ProfileFragment() {

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var userId = UNDEFINED_USER_ID

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseParams()
        setupListeners()
        setupObservers()
        setupScreen()
        if (savedInstanceState == null) {
            viewModel.reduce(ProfileEvent.Ui.LoadUser(userId))
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            viewModel.reduce(ProfileEvent.Ui.GoBack)
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
            viewModel.reduce(ProfileEvent.Ui.GoBack)
        }
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.reduce(ProfileEvent.Ui.GoBack)
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