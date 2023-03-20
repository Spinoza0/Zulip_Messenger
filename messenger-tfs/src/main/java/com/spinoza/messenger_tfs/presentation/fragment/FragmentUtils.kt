package com.spinoza.messenger_tfs.presentation.fragment

import android.content.Context
import android.view.View
import android.widget.Toast
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentProfileBinding
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

internal fun Context.showError(result: RepositoryResult) {
    val text = when (result.type) {
        RepositoryResult.Type.ERROR_MESSAGE_WITH_ID_NOT_FOUND -> {
            String.format(getString(R.string.error_message_not_found), result.text)
        }
        RepositoryResult.Type.ERROR_USER_WITH_ID_NOT_FOUND -> {
            String.format(getString(R.string.error_user_not_found), result.text)
        }
        else -> result.text
    }
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

internal fun FragmentProfileBinding.setup(user: User) {
    textViewName.text = user.full_name
    textViewStatus.text = user.status
    if (user.status.isEmpty()) {
        textViewStatus.visibility = View.GONE
    } else {
        textViewStatus.text = user.status
    }
    if (user.isActive) {
        textViewStatusOnline.visibility = View.VISIBLE
        textViewStatusOffline.visibility = View.GONE
    } else {
        textViewStatusOnline.visibility = View.GONE
        textViewStatusOffline.visibility = View.VISIBLE
    }
    com.bumptech.glide.Glide.with(imageViewAvatar)
        .load(user.avatar_url)
        .transform(RoundedCorners(20))
        .error(R.drawable.ic_default_avatar)
        .into(imageViewAvatar)
}