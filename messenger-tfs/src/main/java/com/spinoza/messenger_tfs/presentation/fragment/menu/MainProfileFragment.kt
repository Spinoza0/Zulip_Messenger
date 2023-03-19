package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMainProfileBinding
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.presentation.viewmodel.MainProfileFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MainProfileFragmentViewModelFactory

class MainProfileFragment : Fragment() {

    private var _binding: FragmentMainProfileBinding? = null
    private val binding: FragmentMainProfileBinding
        get() = _binding ?: throw RuntimeException("FragmentMainProfileBinding == null")


    private val viewModel: MainProfileFragmentViewModel by viewModels {
        MainProfileFragmentViewModelFactory(
            GetCurrentUserUseCase(MessagesRepositoryImpl.getInstance())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupScreen()
    }

    private fun setupScreen() {
        with(viewModel.user) {
            binding.textViewName.text = this.full_name
            binding.textViewStatus.text = this.status
            if (this.status.isEmpty()) {
                binding.textViewStatus.visibility = View.GONE
            } else {
                binding.textViewStatus.text = this.status
            }
            if (this.isActive) {
                binding.textViewStatusOnline.visibility = View.VISIBLE
                binding.textViewStatusOffline.visibility = View.GONE
            } else {
                binding.textViewStatusOnline.visibility = View.GONE
                binding.textViewStatusOffline.visibility = View.VISIBLE
            }
            Glide.with(binding.imageViewAvatar)
                .load(this.avatar_url)
                .transform(RoundedCorners(20))
                .error(R.drawable.ic_default_avatar)
                .into(binding.imageViewAvatar)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): MainProfileFragment {
            return MainProfileFragment()
        }
    }
}