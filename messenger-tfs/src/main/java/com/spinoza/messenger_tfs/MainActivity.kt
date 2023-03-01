package com.spinoza.messenger_tfs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.messenger_tfs.presentation.ui.EmojiView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emojiViewTest()
    }

    private fun emojiViewTest() {
        val emojiView1 = findViewById<EmojiView>(R.id.emojiView1)
        val emojiView2 = findViewById<EmojiView>(R.id.emojiView2)
        setupEmojiView(emojiView1)
        setupEmojiView(emojiView2)
    }

    private fun setupEmojiView(emojiView: EmojiView) {
        emojiView.emoji = "\uD83D\uDE0D"
        emojiView.count = 1
        emojiView.setOnClickListener {
            //emojiView1.count++
            emojiView.isSelected = !emojiView.isSelected()
        }
    }
}