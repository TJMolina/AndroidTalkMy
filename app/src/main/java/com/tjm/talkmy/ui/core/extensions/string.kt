package com.tjm.talkmy.ui.core.extensions


fun String.separateSentences(): List<String> =
    split(Regex("\\n|(?<=\\.)(?=[A-Z])"))
        .flatMap { it.split(Regex("(?<=\\.)(?=\\s+)")) }
        .filter { it.isNotBlank() }


fun String.translateHTMLtoPlain(): String =
    Regex("""<(p|li|h1)\b[^<]*(?:(?!<\/\1>)<[^<]*)*<\/\1>""")
        .findAll(this)
        .map { it.value.replace(Regex("""<[^>]+>"""), "").replace(Regex("""\s{2,}""")," ").trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n\n")

fun String.isURL() =
    Regex("((http|https)://)(www\\.)?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)")
        .matches(this)
