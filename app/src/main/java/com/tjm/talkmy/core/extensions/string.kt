package com.tjm.talkmy.core.extensions
import com.orhanobut.logger.Logger
import org.apache.commons.text.StringEscapeUtils

fun String.separateSentences(): List<String> =
    this.split(Regex("\\n|\\\\n"))
        .flatMap { it.split(Regex("(?<=\\.)(?=\\s+)")) }
        .filter { it.isNotBlank() }

fun String.separateSentencesInsertPTag(): String {
    val text = this.replace(Regex("<"), "&lt").split(Regex("\\n"))
    return text.joinToString("") { paragraph ->
        if (paragraph.trim().isNotBlank()) {
             paragraph.split(Regex("(?<=\\.)(?=\\s+)"))
                .joinToString("") { "<p>${it}</p>" } + "</br>"
        } else {
            "</br>"
        }
    }
}

fun String.separateSentencesInsertPTagWeb(): String =
    this.replace(Regex("<"), "<</>")
        .split(Regex("\\n"))
        .map { paragraph ->
            paragraph.split(Regex("(?<=\\.)(?=\\s+)"))
                .joinToString("") { "<p>${it}</p>" }
        }
        .filter { it.removeSurrounding("<p>", "</p>").trim().isNotBlank() }
        .joinToString("</br></br>")

fun String.cleanHtmlTags(): String =
    replace(Regex("""<[^>]+>"""), "")
        .replace(Regex("""\s{2,}"""), " ").trim()

fun String.translateHTMLtoPlain(): String =
    Regex("""<(p|li|h1|h2|h3|h4|h5|h6)\b[^<]*(?:(?!<\/\1>)<[^<]*)*<\/\1>""").findAll(this)
        .mapNotNull { StringEscapeUtils.unescapeHtml4(it.value.cleanHtmlTags()) }
        .filter { it.trim().isNotBlank() }.joinToString("\n\n")

fun String.translateInnerTextToPlain(): String = this.replace("\\n", "\n").replace(Regex("(\\\\u003C)"), "<")
/*
fun String.translateInnerTextToPlain(): String = this.removeSurrounding("\"", "\"")
    .replace(Regex("(?<![\\\\n])\\\\n\\\\n(?![\\\\nA-Za-z])"), " ")
    .replace(Regex("\\\\n\\\\n"), "\n")
    .replace(Regex("\\\\n"), "\n")
    .replace(Regex("(\\\\u003C)"), "<")
*/

fun String.isURL(): Boolean =
    matches(Regex("(http|https)://(www\\.)?[-a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[-a-zA-Z]{2,}(\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)?)"))
