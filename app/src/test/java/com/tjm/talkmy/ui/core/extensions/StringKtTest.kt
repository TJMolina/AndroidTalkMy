package com.tjm.talkmy.ui.core.extensions

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StringKtTest{

    @Test
    fun `separateSentences should return a list of Strings`() {
        //given
        val texto = "Primero. Segundo. \nTercero.\ncuarto\nquinto\nsexto. septimo. octavo."

        //when
        val result = texto.separateSentences()

        //then
        println(result.toString())

        assertNotNull(result)
        assertTrue(result is List<String>)
    }


}