package com.adaldosso.spendy

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test_forLoop() {
        val numbers = listOf(1, 2, 3, 4, 5)
        var sum = 0
        for (number in numbers) {
            sum += number
        }
        assertEquals(15, sum)
    }

    @Test
    fun test_ifElse() {
        val number = 7
        val result = if (number % 2 == 0) "pari" else "dispari"
        assertEquals("dispari", result)
    }

    @Test
    fun test_whenExpression() {
        val day = 3
        val dayName = when (day) {
            1 -> "Lunedì"
            2 -> "Martedì"
            3 -> "Mercoledì"
            4 -> "Giovedì"
            5 -> "Venerdì"
            6, 7 -> "Weekend"
            else -> "Non valido"
        }
        assertEquals("Mercoledì", dayName)
    }


}