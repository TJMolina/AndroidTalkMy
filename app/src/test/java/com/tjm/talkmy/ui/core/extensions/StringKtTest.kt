package com.tjm.talkmy.ui.core.extensions

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StringKtTest{

    /*
    @RelaxedMock
    @Mock
    private lateinit var miRepositorio: Repositorio
    @Before
    fun onBefore(){
        MockkAnnotations.init(this)
    }
     */

    @Test
    fun `separateSentences should return a list of Strings`() {
        //given
        val texto = "Primero. Segundo. \nTercero."

        //when
        val result = texto.separateSentences()

        //then
        println("Oracion: ${result[0]}")
        println(result.toString())

        assertNotNull(result)
        assertTrue(result is List<String>)
    }


}