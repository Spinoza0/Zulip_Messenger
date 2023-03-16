package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.spinoza.messenger_tfs.databinding.FragmentDialogChooseReactionBinding
import com.spinoza.messenger_tfs.domain.utils.emojiSet
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

class ChooseReactionDialogFragment : BottomSheetDialogFragment() {

    var listener: ((Long, Long, String) -> Unit)? = null

    private var _binding: FragmentDialogChooseReactionBinding? = null
    private val binding: FragmentDialogChooseReactionBinding
        get() = _binding ?: throw RuntimeException("FragmentDialogChooseReactionBinding == null")

    private var messageId = UNDEFINED_ID
    private var userId = UNDEFINED_ID

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

        parseParams()

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
                emoji = emojiFromSet.toString()
                isBackgroundVisible = false
                isCountVisible = false
                size = 30f
                setCustomPadding(REACTION_PADDING)
            }
            binding.flexBoxLayout.addView(reactionView)
        }
    }

    private fun dismissWithResult(reaction: String) {
        listener?.invoke(messageId, userId, reaction)
        dismiss()
    }

    private fun parseParams() {
        messageId = arguments?.getLong(MESSAGE_ID) ?: UNDEFINED_ID
        userId = arguments?.getLong(USER_ID) ?: UNDEFINED_ID
        if (messageId == UNDEFINED_ID || userId == UNDEFINED_ID)
            dismiss()
    }

    companion object {
        const val TAG = "ChooseReactionDialog"
        private const val REACTION_PADDING = 4f
        private const val UNDEFINED_ID = -1L

        private const val MESSAGE_ID = "messageId"
        private const val USER_ID = "userId"

        fun newInstance(messageId: Long, userId: Long): ChooseReactionDialogFragment {
            return ChooseReactionDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(MESSAGE_ID, messageId)
                    putLong(USER_ID, userId)
                }
            }
        }
    }
}