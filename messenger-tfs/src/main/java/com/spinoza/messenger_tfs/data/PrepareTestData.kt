package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.*
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.utils.emojiSet
import kotlin.random.Random

// TODO: for testing purpose

val topics1 = listOf(
    TopicDto("jokes", Message.UNDEFINED_ID),
    TopicDto("weather", Message.UNDEFINED_ID),
)

val topics2 = listOf(
    TopicDto("testing", Message.UNDEFINED_ID),
    TopicDto("development", Message.UNDEFINED_ID),
)

val topics3 = listOf(
    TopicDto("figma", Message.UNDEFINED_ID),
    TopicDto("hard", Message.UNDEFINED_ID),
    TopicDto("soft", Message.UNDEFINED_ID),
)

val channelsDto = listOf(
    ChannelDto(0L, "general", topics1, true),
    ChannelDto(1L, "Development", topics2, true),
    ChannelDto(2L, "Design", topics3, true),
    ChannelDto(3L, "PR", topics1, true),
    ChannelDto(4L, "Events", topics2, false),
)

val usersDto = listOf(
    UserDto(
        1,
        "fakeemail@mail.com",
        "Darrel Steward",
        "https://cs11.pikabu.ru/post_img/big/2020/04/12/9/1586704514168132921.png",
        User.Presence.ONLINE
    ),
    UserDto(
        2,
        "vasyapupkin@mail.ru",
        "Vasya Pupkin",
        null,
        User.Presence.OFFLINE
    ),
    UserDto(
        3,
        "max@mail.ru",
        "Maxim Ivanov",
        "https://cs14.pikabu.ru/post_img/big/2023/02/13/8/1676295806139337963.png",
        User.Presence.IDLE
    )
)

fun prepareTestData(): List<MessageDto> {
    val countOfReactions = 5
    val testUserId = 1L
    val messages = mutableListOf<MessageDto>()

    val reactions = emojiSet.take(countOfReactions).associate { emoji ->
        emoji.toString() to ReactionParamDto(listOf(testUserId))
    }

    repeat(20) { index ->
        val stream = channelsDto[Random.nextInt(channelsDto.size)]
        val topic = stream.topics[Random.nextInt(stream.topics.size)]
        val user = usersDto[Random.nextInt(usersDto.size)]
        val message = MessageDto(
            index.toLong(),
            MessageDate("${index % 2 + 1} марта 2023"),
            user,
            "Message $index text",
            if (index % 3 == 0) reactions else emptyMap(),
            stream.id,
            topic.name
        )
        messages.add(message)
    }
    return messages
}

private var errorGenerator = 0

const val errorText = "Test Error Text"

fun isErrorInRepository(): Boolean {
    errorGenerator++
    return errorGenerator % 15 == 0
}