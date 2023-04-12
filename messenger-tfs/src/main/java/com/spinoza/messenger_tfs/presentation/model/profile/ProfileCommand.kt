package com.spinoza.messenger_tfs.presentation.model.profile

import com.spinoza.messenger_tfs.domain.model.User

sealed class ProfileCommand {

    object LoadCurrentUser : ProfileCommand()

    object GetEvent : ProfileCommand()

    class SubscribePresence(val user: User) : ProfileCommand()

    class LoadUser(val userId: Long) : ProfileCommand()
}