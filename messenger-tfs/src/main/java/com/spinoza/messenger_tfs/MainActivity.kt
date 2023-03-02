package com.spinoza.messenger_tfs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.messenger_tfs.presentation.ui.FlexBoxLayout
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emojiViewTest()
    }

    private fun emojiViewTest() {
        val reactionView = findViewById<ReactionView>(R.id.emojiView)
        reactionView.setOnClickListener {
            reactionView.count++
        }

        val flexBoxLayout = findViewById<FlexBoxLayout>(R.id.flexBoxLayout)
        repeat(25) {
            val reaction = ReactionView(this)
            reaction.emoji = "\uD83D\uDE0D"
            reaction.count = 1
            reaction.size = Random.nextInt(14, 24).toFloat()
            reaction.setOnClickListener {
                reaction.count++
            }

            flexBoxLayout.addView(reaction)
        }
    }
}