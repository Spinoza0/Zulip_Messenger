package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.data.network.model.user.UserDto

interface OwnUserKeeper {

    var value: UserDto
}