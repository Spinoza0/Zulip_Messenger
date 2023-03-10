package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.spinoza.messenger_tfs.databinding.EmojiListBinding
import com.spinoza.messenger_tfs.domain.emojiSet
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

class AddReactionFragment : BottomSheetDialogFragment() {

    private var _binding: EmojiListBinding? = null
    private val binding: EmojiListBinding
        get() = _binding ?: throw RuntimeException("EmojiListBinding == null")


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

        setupScreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupScreen() {
        emojiSet.forEach { emojiFromSet ->
            val reactionView = ReactionView(requireContext()).apply {
                emoji = emojiFromSet.toString()
                isBackgroundVisible = false
                isCountVisible = false
            }
            binding.flexBoxLayout.addView(reactionView)
        }
    }
}