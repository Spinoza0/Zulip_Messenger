package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.network.ZulipAuthKeeper
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.usecase.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterEventQueueUseCase
import kotlinx.serialization.json.Json


class GlobalDI private constructor() {

    private val repository by lazy {
        MessagesRepositoryImpl.getInstance(ZulipApiFactory.apiService, ZulipAuthKeeper, Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }

    val registerEventQueueUseCase by lazy { RegisterEventQueueUseCase(repository) }
    val deleteEventQueueUseCase by lazy { DeleteEventQueueUseCase(repository) }

    companion object {

        lateinit var INSTANCE: GlobalDI

        fun init() {
            INSTANCE = GlobalDI()
        }
    }
}