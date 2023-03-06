package com.spinoza.messenger_tfs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding
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
}