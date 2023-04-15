package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.spinoza.messenger_tfs.databinding.FragmentDialogChooseReactionBinding
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.utils.emojiSet
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

class ChooseReactionDialogFragment : BottomSheetDialogFragment() {

    var listener: ((Long, Emoji) -> Unit)? = null

    private var _binding: FragmentDialogChooseReactionBinding? = null
    private val binding: FragmentDialogChooseReactionBinding
        get() = _binding ?: throw RuntimeException("FragmentDialogChooseReactionBinding == null")

    private var messageId = UNDEFINED_ID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (savedInstanceState != null) {
            dismiss()
        }
        _binding = FragmentDialogChooseReactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        parseParams()
        setupListeners()
        setupScreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.flexBoxLayout.setOnChildrenClickListener { _, view ->
            if (view is ReactionView) {
                dismissWithResult(view.emoji)
            }
        }
        binding.textViewTopLine.setOnClickListener {
            dismiss()
        }
    }

    private fun setupScreen() {
        emojiSet.forEach { emojiFromSet ->
            val reactionView = ReactionView(requireContext()).apply {
                emoji = emojiFromSet
                isBackgroundVisible = false
                isCountVisible = false
                size = 30f
                setCustomPadding(REACTION_PADDING)
            }
            binding.flexBoxLayout.addView(reactionView)
        }
    }

    private fun dismissWithResult(emoji: Emoji) {
        listener?.invoke(messageId, emoji)
        dismiss()
    }

    private fun parseParams() {
        messageId = arguments?.getLong(MESSAGE_ID) ?: UNDEFINED_ID
        if (messageId == UNDEFINED_ID)
            dismiss()
    }

    companion object {
        const val TAG = "ChooseReactionDialog"
        private const val REACTION_PADDING = 4f
        private const val UNDEFINED_ID = -1L

        private const val MESSAGE_ID = "messageId"

        fun newInstance(messageId: Long): ChooseReactionDialogFragment {
            return ChooseReactionDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(MESSAGE_ID, messageId)
                }
            }
        }
    }
}