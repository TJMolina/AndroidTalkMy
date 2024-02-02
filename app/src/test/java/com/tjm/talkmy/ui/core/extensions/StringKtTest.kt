package com.tjm.talkmy.ui.core.extensions

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StringKtTest{

    @Test
    fun `separateSentences should return a list of Strings`() {
        //given
        val texto = "Primero. Segundo. \nTercero.\ncuarto\nquinto\nsexto. septimo. octavo.                     noveno          decimo         onceavo.       \ndoceavo      \ntreceavo.       \ncatorceavo"

        //when
        val result = texto.separateSentences()
        //https://www.lightnovelcave.com/novel/shadow-slave-1365/chapter-1406
        //then
        println(result.toString())

        assertNotNull(result)
        assertTrue(result is List<String>)
    }


}