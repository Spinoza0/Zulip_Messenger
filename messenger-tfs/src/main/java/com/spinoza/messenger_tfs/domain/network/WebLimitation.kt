package com.spinoza.messenger_tfs.domain.network

interface WebLimitation {

    fun updateLimitations(
        maxStreamNameLength: Int,
        maxStreamDescriptionLength: Int,
        maxTopicLength: Int,
        maxMessageLength: Int,
        serverPresencePingIntervalSeconds: Int,
        serverPresenceOfflineThresholdSeconds: Int,
    )

    fun getMaxChannelName(): Int

    fun getMaxChannelDescription(): Int

    fun getMaxTopicName(): Int

    fun getMaxMessage(): Int

    fun getPresencePingIntervalSeconds(): Int

    fun getPresenceOfflineThresholdSeconds(): Int
}