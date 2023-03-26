package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.*
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
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
    ChannelDto(0L, "general", topics1),
    ChannelDto(1L, "Development", topics2),
    ChannelDto(2L, "Design", topics3),
    ChannelDto(3L, "PR", topics1),
)

val testUserDto = UserDto(
    100,
    true,
    "TestUser@mail.com",
    "Test User",
    "https://cs11.pikabu.ru/post_img/2020/04/12/9/158670440816531661.png",
    "Working..."
)


val usersDto = listOf(
    UserDto(
        1,
        true,
        "fakeemail@mail.com",
        "Darrel Steward",
        "https://cs11.pikabu.ru/post_img/big/2020/04/12/9/1586704514168132921.png",
        "In a meeting"
    ),
    UserDto(
        2,
        false,
        "vasyapupkin@mail.ru",
        "Vasya Pupkin",
        null,
        "On vacation"
    ),
    UserDto(
        3,
        true,
        "max@mail.ru",
        "Maxim Ivanov",
        "https://cs14.pikabu.ru/post_img/big/2023/02/13/8/1676295806139337963.png",
        null
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