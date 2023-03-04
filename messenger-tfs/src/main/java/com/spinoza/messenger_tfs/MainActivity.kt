package com.spinoza.messenger_tfs

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.messenger_tfs.presentation.ui.MessageLayout
import com.spinoza.messenger_tfs.presentation.ui.ReactionView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test()
    }

    private fun test() {

        val messageLayout = findViewById<MessageLayout>(R.id.messageLayout)
        val layoutParams =
            messageLayout.layoutParams as ViewGroup.MarginLayoutParams
        val margin = 8f.dpToPx(messageLayout).toInt()
        layoutParams.setMargins(margin, margin, margin, margin)
        messageLayout.layoutParams = layoutParams
        messageLayout.onReactionAddClickListener = {
            messageLayout.reactions.addView(testGetReaction())
        }
        messageLayout.avatarResId = R.drawable.face
        messageLayout.name = "John Dow"
        messageLayout.message =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse ac magna purus." +
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse ac magna purus."
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