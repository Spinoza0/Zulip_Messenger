package com.spinoza.messenger_tfs.data.network.ownuser

import com.spinoza.messenger_tfs.data.network.model.user.UserDto

object OwnUserKeeperImpl : OwnUserKeeper {

    override var value: UserDto = UserDto()
}