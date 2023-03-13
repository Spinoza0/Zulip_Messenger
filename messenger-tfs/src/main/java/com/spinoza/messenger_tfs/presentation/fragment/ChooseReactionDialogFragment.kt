package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentDialogChooseReactionBinding
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.domain.utils.emojiSet
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import com.spinoza.messenger_tfs.presentation.viewmodel.ChooseReactionViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ChooseReactionViewModelFactory

class ChooseReactionDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDialogChooseReactionBinding? = null
    private val binding: FragmentDialogChooseReactionBinding
        get() = _binding ?: throw RuntimeException("FragmentDialogChooseReactionBinding == null")

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ChooseReactionViewModelFactory(
                UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
            )
        )[ChooseReactionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDialogChooseReactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

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
                val messageId =
                    ChooseReactionDialogFragmentArgs.fromBundle(requireArguments()).messageId
                val userId =
                    ChooseReactionDialogFragmentArgs.fromBundle(requireArguments()).userId
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
                setCustomPadding(REACTION_PADDING)
            }
            binding.flexBoxLayout.addView(reactionView)
        }
    }

    private companion object {
        const val REACTION_PADDING = 4f
    }
}