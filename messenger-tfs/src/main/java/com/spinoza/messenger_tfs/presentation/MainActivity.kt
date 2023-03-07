package com.spinoza.messenger_tfs.presentation

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.ActivityMainBinding
import com.spinoza.messenger_tfs.presentation.ui.MessageEntity
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        test(savedInstanceState)
    }

    private fun test(savedInstanceState: Bundle?) {

        with(binding.messageView) {
            if (savedInstanceState == null) {
                name = context.getString(R.string.test_message_name)
                text = context.getString(R.string.test_message_text)
            }
            setRoundAvatar (R.drawable.face)
            setIconAddVisibility(false)
            setOnMessageLongClickListener {
                addReaction(testGetReaction())
                setIconAddVisibility(true)
            }
            setOnReactionAddClickListener {
                it.addReaction(testGetReaction())
            }

            binding.buttonMakeVisible.setOnClickListener {
                setIconAddVisibility(true)
            }

            binding.buttonMakeInvisible.setOnClickListener {
                setIconAddVisibility(false)
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
        val messageEntity = binding.messageView.getMessageEntity()
        outState.putParcelable(EXTRA_MESSAGE, messageEntity)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val messageEntity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable(EXTRA_MESSAGE, MessageEntity::class.java)
        } else {
            @Suppress("deprecation")
            savedInstanceState.getParcelable(EXTRA_MESSAGE)
        }

        if (messageEntity != null) {
            binding.messageView.setMessage(messageEntity)
        }
    }

    private companion object {
        const val EXTRA_MESSAGE = "message"
    }
}