package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.spinoza.messenger_tfs.presentation.fragment.ProfileFragment
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileScreenEvent

class OwnUserProfileFragment : ProfileFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonLogout()
        if (savedInstanceState == null) {
            store.accept(ProfileScreenEvent.Ui.LoadCurrentUser)
        } else {
            store.accept(ProfileScreenEvent.Ui.SubscribePresence(store.currentState.user))
        }
    }

    private fun setupButtonLogout() {
        binding.buttonLogout.isVisible = true
        binding.buttonLogout.setOnClickListener {
            store.accept(ProfileScreenEvent.Ui.Logout)
        }
    }

    companion object {

        fun newInstance(): OwnUserProfileFragment {
            return OwnUserProfileFragment()
        }
    }
}