package com.tjm.talkmy.ui.core.extensions

import org.apache.commons.text.StringEscapeUtils

fun String.separateSentences(): List<String> =
    split(Regex("\\n|(?<=\\.)(?=[A-Z])"))
        .flatMap { it.split(Regex("(?<=\\.)(?=\\s+)")) }
        .filter { it.isNotBlank() }

fun String.cleanHtmlTags(): String =
    replace(Regex("""<[^>]+>"""), "")
        .replace(Regex("""\s{2,}"""), " ").trim()

fun String.translateHTMLtoPlain(): String =
    Regex("""<(p|li|h1|h2|h3|h4|h5|h6)\b[^<]*(?:(?!<\/\1>)<[^<]*)*<\/\1>""").findAll(this)
        .mapNotNull { StringEscapeUtils.unescapeHtml4(it.value.cleanHtmlTags()) }
        .filter { it.trim().isNotBlank() }.joinToString("\n\n")

fun String.isURL(): Boolean =
    matches(Regex("(http|https)://(www\\.)?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-zA-Z]{2,}(\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)?)"))
