package com.spinoza.messenger_tfs.domain.network

interface WebLimitation {

    fun updateLimitations(
        maxStreamNameLength: Int,
        maxStreamDescriptionLength: Int,
        maxTopicLength: Int,
        maxMessageLength: Int,
        serverPresencePingIntervalSeconds: Int,
        serverPresenceOfflineThresholdSeconds: Int,
        messageContentEditLimitSeconds: Int,
        topicEditingLimitSeconds: Int,
        maxFileUploadSizeMib: Int,
    )

    fun getMaxChannelName(): Int

    fun getMaxChannelDescription(): Int

    fun getMaxTopicName(): Int

    fun getMaxMessage(): Int

    fun getPresencePingIntervalSeconds(): Int

    fun getPresenceOfflineThresholdSeconds(): Int

    fun getMessageContentEditLimitSeconds(): Int

    fun getTopicEditingLimitSeconds(): Int

    fun getMaxFileUploadSizeMib(): Int
}