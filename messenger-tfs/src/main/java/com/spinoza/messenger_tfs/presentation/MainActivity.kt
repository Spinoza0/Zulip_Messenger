package com.spinoza.messenger_tfs.presentation

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding
import com.spinoza.messenger_tfs.domain.Reaction
import com.spinoza.messenger_tfs.domain.ReactionsState
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import kotlin.random.Random

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

        with(binding.messageLayout) {
            name.text = "John Dow"
            message.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                    "Suspendisse ac magna purus. Lorem ipsum dolor sit amet, " +
                    "consectetur adipiscing elit. Suspendisse ac magna purus."
            setRoundAvatar(R.drawable.face)
            reactions.setIconAddVisibility(false)
            setOnMessageClickListener {
                reactions.addView(testGetReaction())
                reactions.setIconAddVisibility(true)
            }
            reactions.setOnAddClickListener { it.addView(testGetReaction()) }

            binding.buttonMakeVisible.setOnClickListener {
                reactions.setIconAddVisibility(true)
            }

            binding.buttonMakeInvisible.setOnClickListener {
                reactions.setIconAddVisibility(false)
            }
        }
    }

    private fun testGetReaction(): ReactionView {
        val emojis = listOf("\uD83D\uDE00", "\uD83E\uDD10", "\uD83E\uDD17")
        val result = ReactionView(this).apply {
            emoji = emojis[Random.nextInt(emojis.size)]
            count = 1
            setOnClickListener {
                count++
            }
        }
        return result
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val reactions = arrayListOf<Reaction>()
        binding.messageLayout.reactions.children.forEach { view ->
            if (view is ReactionView) {
                reactions.add(
                    Reaction(
                        view.emoji,
                        view.count,
                        view.isSelected,
                    )
                )
            }
        }
        val reactionsState =
            ReactionsState(reactions, binding.messageLayout.reactions.getIconAddVisibility())
        outState.putParcelable(EXTRA_REACTIONS, reactionsState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val reactionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable(EXTRA_REACTIONS, ReactionsState::class.java)
        } else {
            @Suppress("deprecation")
            savedInstanceState.getParcelable(EXTRA_REACTIONS)
        }

        if (reactionsState == null) return

        with(binding.messageLayout) {
            reactionsState.value.forEach { reaction ->
                val reactionView = testGetReaction().apply {
                    emoji = reaction.emoji
                    count = reaction.count
                    isSelected = reaction.selected
                }
                reactions.addView(reactionView)
                reactions.setIconAddVisibility(reactionsState.iconAddVisibility)
            }
        }
    }

    private companion object {
        const val EXTRA_REACTIONS = "reactions"
    }
}