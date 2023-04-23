package com.spinoza.messenger_tfs.presentation.feature.messages.ui

import android.text.Editable
import android.text.Html
import com.spinoza.messenger_tfs.domain.model.Emoji
import org.xml.sax.XMLReader
import java.lang.reflect.Field
import java.util.regex.Pattern

class MessageTagHandler : Html.TagHandler {

    private val emojiPattern = Pattern.compile(EMOJI_CODE_REGEXP)
    private val attributes = HashMap<String, String>()

    override fun handleTag(
        opening: Boolean,
        tag: String,
        output: Editable,
        xmlReader: XMLReader,
    ) {
        if (tag.equals(EMOJI_TAG, ignoreCase = true)) {
            if (opening) {
                processAttributes(xmlReader)
                val emojiName = attributes[PROPERTY_NAME]
                val classValue = attributes[PROPERTY_CODE]
                if (emojiName != null && classValue != null) {
                    val matcher = emojiPattern.matcher(classValue)
                    if (matcher.find()) {
                        val code = matcher.group(EMOJI_CODE_INDEX) ?: EMPTY_EMOJI_CODE
                        val emoji = Emoji(emojiName, code).toCharacterImage()
                        if (emoji.isNotEmpty()) {
                            output.append(emoji)
                        }
                    }
                }
            } else {
                attributes.clear()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processAttributes(xmlReader: XMLReader) {
        runCatching {
            val elementField: Field = xmlReader.javaClass.getDeclaredField("theNewElement")
            elementField.isAccessible = true
            val element: Any = elementField.get(xmlReader) as Any
            val attsField: Field = element.javaClass.getDeclaredField("theAtts")
            attsField.isAccessible = true
            val atts: Any = attsField.get(element) as Any
            val dataField: Field = atts.javaClass.getDeclaredField("data")
            dataField.isAccessible = true
            val data: Array<String> = dataField.get(atts) as Array<String>
            val lengthField: Field = atts.javaClass.getDeclaredField("length")
            lengthField.isAccessible = true
            val len = lengthField.get(atts) as Int
            for (i in 0 until len) {
                val index = i * ATTR_INDEX_COEFFICIENT
                attributes[data[index + ATTR_KEY_OFFSET]] = data[index + ATTRIBUTES_VALUE_OFFSET]
            }
        }
    }

    companion object {

        private const val ATTR_INDEX_COEFFICIENT = 5
        private const val ATTR_KEY_OFFSET = 1
        private const val ATTRIBUTES_VALUE_OFFSET = 4
        private const val EMOJI_TAG = "emojiSpan"
        private const val PROPERTY_NAME = "title"
        private const val PROPERTY_CODE = "class"
        private const val EMOJI_CODE_REGEXP = "emoji-([\\dA-Fa-f]{4,}(?:-[\\dA-Fa-f]{4,})*)"
        private const val EMOJI_CODE_INDEX = 1
        private const val EMPTY_EMOJI_CODE = ""

        private val emojiTagRegex = Regex("<span(.+?class=\"emoji.+?):\\w+?:</span>")

        // TODO: prepare href -> make full url
        fun prepareTag(source: String): String {
            return source.replace(emojiTagRegex, "<emojiSpan$1</emojiSpan>")
        }
    }
}