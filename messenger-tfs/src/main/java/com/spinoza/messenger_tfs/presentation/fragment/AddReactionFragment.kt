package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.EmojiListBinding
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import com.spinoza.messenger_tfs.domain.utils.emojiSet
import com.spinoza.messenger_tfs.presentation.viewmodel.AddReactionViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.AddReactionFragmentViewModelFactory

class AddReactionFragment : BottomSheetDialogFragment() {

    private var _binding: EmojiListBinding? = null
    private val binding: EmojiListBinding
        get() = _binding ?: throw RuntimeException("EmojiListBinding == null")

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            AddReactionFragmentViewModelFactory(
                UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
            )
        )[AddReactionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = EmojiListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupScreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.flexBoxLayout.setOnChildClickListener { _, view ->
            if (view is ReactionView) {
                val messageId = AddReactionFragmentArgs.fromBundle(requireArguments()).messageId
                val userId = AddReactionFragmentArgs.fromBundle(requireArguments()).userId
                viewModel.updateReaction(messageId, userId, view.emoji)
                dismiss()
            }
        }
        binding.textViewTopLine.setOnClickListener {
            dismiss()
        }
    }

    private fun setupScreen() {
        emojiSet.forEach { emojiFromSet ->
            val reactionView = ReactionView(requireContext()).apply {
                emoji = emojiFromSet.toString()
                isBackgroundVisible = false
                isCountVisible = false
                size = 30f
            }
            binding.flexBoxLayout.addView(reactionView)
        }
    }
}