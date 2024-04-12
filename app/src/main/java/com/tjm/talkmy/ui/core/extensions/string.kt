package com.tjm.talkmy.ui.core.extensions

import org.apache.commons.text.StringEscapeUtils

fun String.separateSentences(): List<String> =
    split(Regex("\\n|\\\\n|(?<=\\.)(?=[A-Z])"))
        .flatMap { it.split(Regex("(?<=\\.)(?=\\s+)")) }
        .filter { it.isNotBlank() }

fun String.separateSentencesInsertPTag(): String {
    return this.split(Regex("\\n|(?<=\\.)(?=[A-Z])"))
        .map { paragraph ->
            paragraph.split(Regex("(?<=\\.)(?=\\s+)")).map { centence -> "<p>$centence</p>" }
                .joinToString("")
        }
        .filter { it.removeSurrounding("<p>", "</p>").trim().isNotBlank() }
        .joinToString("</br></br>")
}

fun String.cleanHtmlTags(): String =
    replace(Regex("""<[^>]+>"""), "")
        .replace(Regex("""\s{2,}"""), " ").trim()

fun String.translateHTMLtoPlain(): String =
    Regex("""<(p|li|h1|h2|h3|h4|h5|h6)\b[^<]*(?:(?!<\/\1>)<[^<]*)*<\/\1>""").findAll(this)
        .mapNotNull { StringEscapeUtils.unescapeHtml4(it.value.cleanHtmlTags()) }
        .filter { it.trim().isNotBlank() }.joinToString("\n\n")
fun String.translateInnerTextToPlain():String = this.removeSurrounding("\"", "\"")
.replace(Regex("(?<![\\\\n])\\\\n\\\\n(?![\\\\n])"), " ")
.replace(Regex("(\\\\n){3,}"), "\n")
fun String.isURL(): Boolean =
    matches(Regex("(http|https)://(www\\.)?[-a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[-a-zA-Z]{2,}(\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)?)"))
