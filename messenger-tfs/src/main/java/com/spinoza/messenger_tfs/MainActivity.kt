package com.spinoza.messenger_tfs

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.messenger_tfs.presentation.ui.FlexBoxLayout
import com.spinoza.messenger_tfs.presentation.ui.MessageLayout
import com.spinoza.messenger_tfs.presentation.ui.ReactionView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test()
    }

    private fun test() {
        val flexBoxLayout = findViewById<FlexBoxLayout>(R.id.flexBoxLayout)
        val layoutParams = flexBoxLayout.layoutParams as ViewGroup.MarginLayoutParams
        val margin = 8f.dpToPx(flexBoxLayout).toInt()
        layoutParams.setMargins(margin, margin, margin, margin)
        flexBoxLayout.layoutParams = layoutParams
        flexBoxLayout.onIconAddClickListener = {
            flexBoxLayout.addView(testGetReaction())
        }

        val messageLayout = findViewById<MessageLayout>(R.id.messageLayout)
        messageLayout.layoutParams = layoutParams
        messageLayout.onReactionAddClickListener = {
            messageLayout.reactionsGroup.addView(testGetReaction())
        }
        messageLayout.name = "Martin"
        messageLayout.message =
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