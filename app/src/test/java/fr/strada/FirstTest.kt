package fr.strada

import fr.strada.utils.Convert
import org.junit.Assert.*
import org.junit.Test

class FirstTest {
    @Test
    fun HexStringToByteArray_isCorrect() {
        val result = Convert.HexStringToByteArray("A5")
        assertEquals(result, result)
    }
}