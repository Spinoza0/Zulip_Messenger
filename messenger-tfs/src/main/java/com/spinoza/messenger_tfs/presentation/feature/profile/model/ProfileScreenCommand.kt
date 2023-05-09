package com.spinoza.messenger_tfs.presentation.feature.profile.model

import com.spinoza.messenger_tfs.domain.model.User

sealed class ProfileScreenCommand {

    object LogIn : ProfileScreenCommand()

    object LoadCurrentUser : ProfileScreenCommand()

    object GetEvent : ProfileScreenCommand()

    class SubscribePresence(val user: User) : ProfileScreenCommand()

    class LoadUser(val userId: Long) : ProfileScreenCommand()
}