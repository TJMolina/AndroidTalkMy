package com.tjm.talkmy.ui.core.extensions

fun String.separateSentences(): List<String> {
    return this
        .replace(Regex("(<([^>]+)>)"), "")
        .split(Regex("(?<=\\.)\\s+"))
}


/*
* var texto:List<String>
    try {
        texto = this
            .replace(Regex("(<([^>]+)>)"), "")
            .split(Regex("[^.]+[.]{0,1}"))
        /*
        .split(Regex("(?<=\\.)(?=[A-Z])|(?<=\\.)\\s{2,}|(?<=\\.)\\n"))
        .map {
            it
                .split(Regex("[^.]+[.]{0,1}"))
                .map { oracion -> oracion }//hacer otra separacion aqui
                .joinToString("")
        }
         */
        Log.i("extension process", texto.toString())
    } catch (e: Exception) {
        Log.i("extension", e.toString())
        return null
    }
    Log.i("extension succes", texto.toString())
* */