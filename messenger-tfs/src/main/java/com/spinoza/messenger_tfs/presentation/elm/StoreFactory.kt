package com.spinoza.messenger_tfs.presentation.elm

import com.spinoza.messenger_tfs.presentation.model.profile.ProfileState
import vivid.money.elmslie.coroutines.ElmStoreCompat

fun provideProfileStore(actor: ProfileActor) = ElmStoreCompat(
    initialState = ProfileState(),
    reducer = ProfileReducer(),
    actor = actor
)