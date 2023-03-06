package com.spinoza.messenger_tfs

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding
import com.spinoza.messenger_tfs.domain.Reaction
import com.spinoza.messenger_tfs.presentation.ui.ReactionView


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        test()
    }

    private fun test() {

        with(binding) {
            messageLayout.name = "John Dow"
            messageLayout.message =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse ac magna purus." +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse ac magna purus."
            messageLayout.setRoundAvatar(R.drawable.face)
            messageLayout.reactions.iconAddVisibility = false
            messageLayout.onMessageClickListener = {
                messageLayout.reactions.addView(testGetReaction())
                messageLayout.reactions.iconAddVisibility = true
                messageLayout.onMessageClickListener = null
            }
            messageLayout.reactions.onIconAddClickListener = {
                it.addView(testGetReaction())
            }
        }
    }

    private fun testGetReaction(): ReactionView {
        val reaction = ReactionView(this)
        reaction.emoji = "\uD83D\uDE0D"
        reaction.count = 1
        reaction.setOnClickListener {
            reaction.count++
        }
        return reaction
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val reactions = arrayListOf<Reaction>()
        binding.messageLayout.reactions.children.forEach { view ->
            if (view is ReactionView) {
                reactions.add(Reaction(view.emoji, view.count, view.isSelected))
            }
        }
        outState.putParcelableArrayList(EXTRA_REACTIONS, reactions)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val reactions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelableArrayList(EXTRA_REACTIONS, Reaction::class.java)
        } else {
            @Suppress("deprecation")
            savedInstanceState.getParcelableArrayList(EXTRA_REACTIONS)
        }
        reactions?.let {
            it.forEach {
                val reaction = testGetReaction().apply {
                    emoji = it.emoji
                    count = it.count
                    isSelected = it.selected
                }
                binding.messageLayout.reactions.addView(reaction)
            }
        }
    }

    private companion object {
        const val EXTRA_REACTIONS = "reactions"
    }
}