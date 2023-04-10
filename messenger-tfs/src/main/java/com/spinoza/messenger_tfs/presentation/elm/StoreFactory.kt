package com.spinoza.messenger_tfs.presentation.elm

import com.spinoza.messenger_tfs.presentation.model.profile.ProfileState
import vivid.money.elmslie.coroutines.ElmStoreCompat

fun provideProfileStore(initialState: ProfileState, actor: ProfileActor) = ElmStoreCompat(
    initialState = initialState,
    reducer = ProfileReducer(),
    actor = actor
)