package com.spinoza.messenger_tfs.presentation.elm

import com.spinoza.messenger_tfs.presentation.model.messages.MessagesState
import com.spinoza.messenger_tfs.presentation.model.people.PeopleState
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileState
import vivid.money.elmslie.coroutines.ElmStoreCompat

fun provideProfileStore(initialState: ProfileState, actor: ProfileActor) = ElmStoreCompat(
    initialState = initialState,
    reducer = ProfileReducer(),
    actor = actor
)

fun providePeopleStore(actor: PeopleActor) = ElmStoreCompat(
    initialState = PeopleState(),
    reducer = PeopleReducer(),
    actor = actor
)

fun provideMessagesStore(actor: MessagesActor) = ElmStoreCompat(
    initialState = MessagesState(),
    reducer = MessagesReducer(),
    actor = actor
)