package com.tjm.talkmy.ui.core.extensions


fun String.separateSentences(): List<String> {
    return this
        .split(Regex("(?<=\\.)(?=[A-Z])|(?<=\\.)\\n|\\n"))
        .map { it.split(Regex("(?<=\\.)\\s+")) }
        .flatten()
}
fun String.translateHTMLtoPlain():String{
    val regex = Regex("""<(p|li|h1)\b[^<]*(?:(?!<\/\1>)<[^<]*)*<\/\1>""")
    val paragraphs = regex.findAll(this).map { it.value }
    val filteredParagraphs = paragraphs.map { it.replace(Regex("""<[^>]+>"""), "") }
        .filter { it != "" }
    return filteredParagraphs.joinToString("\n\n")
}
fun String.isURL():Boolean{
    val regex = Regex("((http|https)://)(www\\.)?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)")
    return regex.matches(this)
}