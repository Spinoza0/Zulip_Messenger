package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.presentation.model.ProfileFragmentState
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import com.spinoza.messenger_tfs.presentation.viewmodel.ProfileFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ProfileFragmentViewModelFactory
import kotlinx.coroutines.launch

class ItemProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding ?: throw RuntimeException("FragmentProfileBinding == null")


    private val viewModel: ProfileFragmentViewModel by viewModels {
        ProfileFragmentViewModelFactory(
            GetCurrentUserUseCase(MessagesRepositoryImpl.getInstance())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setupObservers()
        setupScreen()
        return binding.root
    }

    private fun setupScreen() {
        binding.textViewLogout.visibility = View.VISIBLE
        viewModel.loadCurrentUser()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleState)
            }
        }
    }

    private fun handleState(state: ProfileFragmentState) {
        if (state !is ProfileFragmentState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is ProfileFragmentState.UserData -> showProfileInfo(state.value)
            is ProfileFragmentState.Error -> {
                Toast.makeText(context, state.value.text, Toast.LENGTH_LONG).show()
            }
            is ProfileFragmentState.Loading -> binding.progressBar.on()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): ItemProfileFragment {
            return ItemProfileFragment()
        }
    }
}