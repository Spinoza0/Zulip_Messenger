package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.View
import com.spinoza.messenger_tfs.presentation.fragment.ProfileFragment
import com.spinoza.messenger_tfs.presentation.model.profilescreen.ProfileEvent

class OwnUserProfileFragment : ProfileFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        if (savedInstanceState == null) {
            viewModel.reduce(ProfileEvent.Ui.LoadCurrentUser)
        }
    }

    companion object {

        fun newInstance(): OwnUserProfileFragment {
            return OwnUserProfileFragment()
        }
    }
}