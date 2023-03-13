package com.spinoza.messenger_tfs.presentation.fragment

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.spinoza.messenger_tfs.presentation.ui.MessageView

// for testing purpose
const val TEST_USER_ID = 100

fun Fragment.onReactionAddClickListener(messageView: MessageView) {
    val action =
        MessagesFragmentDirections.actionMessagesFragmentToAddReactionFragment(
            messageView.messageId,
            TEST_USER_ID
        )
    this.findNavController().navigate(action)
}