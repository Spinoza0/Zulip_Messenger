package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.domain.network.WebLimitation
import javax.inject.Inject

class WebLimitationImpl @Inject constructor() : WebLimitation {

    private var maxStreamNameLength: Int = MAX_STREAM_NAME_LENGTH
    private var maxStreamDescriptionLength: Int = MAX_STREAM_DESCRIPTION_LENGTH
    private var maxTopicLength: Int = MAX_TOPIC_LENGTH
    private var maxMessageLength: Int = MAX_MESSAGE_LENGTH
    private var serverPresencePingIntervalSeconds: Int = SERVER_PRESENCE_PING_INTERVAL_SECONDS
    private var serverPresenceOfflineThresholdSeconds: Int =
        SERVER_PRESENCE_OFFLINE_THRESHOLD_SECONDS

    override fun updateLimitations(
        maxStreamNameLength: Int,
        maxStreamDescriptionLength: Int,
        maxTopicLength: Int,
        maxMessageLength: Int,
        serverPresencePingIntervalSeconds: Int,
        serverPresenceOfflineThresholdSeconds: Int,
    ) {
        this.maxStreamNameLength = maxStreamNameLength
        this.maxStreamDescriptionLength = maxStreamDescriptionLength
        this.maxTopicLength = maxTopicLength
        this.maxMessageLength = maxMessageLength
        this.serverPresencePingIntervalSeconds = serverPresencePingIntervalSeconds
        this.serverPresenceOfflineThresholdSeconds = serverPresenceOfflineThresholdSeconds
    }

    override fun getMaxStreamName(): Int = maxStreamNameLength

    override fun getMaxStreamDescription(): Int = maxStreamDescriptionLength

    override fun getMaxTopicName(): Int = maxTopicLength

    override fun getMaxMessage(): Int = maxMessageLength

    override fun getPresencePingIntervalSeconds(): Int = serverPresencePingIntervalSeconds

    override fun getPresenceOfflineThresholdSeconds(): Int =
        serverPresenceOfflineThresholdSeconds

    private companion object {

        const val MAX_STREAM_NAME_LENGTH = 60
        const val MAX_STREAM_DESCRIPTION_LENGTH = 1024
        const val MAX_TOPIC_LENGTH = 60
        const val MAX_MESSAGE_LENGTH = 10000
        const val SERVER_PRESENCE_PING_INTERVAL_SECONDS = 60
        const val SERVER_PRESENCE_OFFLINE_THRESHOLD_SECONDS = 140
    }
}