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

        with(binding.messageLayout) {
            name = "John Dow"
            message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                    "Suspendisse ac magna purus. Lorem ipsum dolor sit amet, " +
                    "consectetur adipiscing elit. Suspendisse ac magna purus."
            setRoundAvatar(R.drawable.face)
            reactions.iconAddVisibility = false
            onMessageClickListener = {
                reactions.addView(testGetReaction())
                reactions.iconAddVisibility = true
                onMessageClickListener = null
            }
            reactions.onIconAddClickListener = { it.addView(testGetReaction()) }
        }
    }

    private fun testGetReaction() = ReactionView(this).apply {
        emoji = "\uD83D\uDE0D"
        count = 1
        setOnClickListener {
            count++
        }
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

        if (reactions == null) return

        reactions.forEach { reaction ->
            val reactionView = testGetReaction().apply {
                emoji = reaction.emoji
                count = reaction.count
                isSelected = reaction.selected
            }
            binding.messageLayout.reactions.addView(reactionView)
        }
    }

    private companion object {
        const val EXTRA_REACTIONS = "reactions"
    }
}