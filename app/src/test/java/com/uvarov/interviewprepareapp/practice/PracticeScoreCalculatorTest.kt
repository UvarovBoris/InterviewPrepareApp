package com.uvarov.interviewprepareapp.practice

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PracticeScoreCalculatorTest {

    private lateinit var calculator: PracticeScoreCalculator

    @Before
    fun setUp() {
        calculator = PracticeScoreCalculator()
    }

    // =========================================================================
    // PRACTICE EXERCISE:
    // Implement unit tests below covering different scenarios and edge cases.
    //
    // Suggested ideas to test:
    //
    // 1. calculatePercentage:
    //    - Standard calculation (e.g. 8 out of 10 -> 80)
    //    - Edge cases: 0 out of 5 -> 0, 5 out of 5 -> 100
    //    - Rounding: 1 out of 3 -> 33, 2 out of 3 -> 67
    //    - Exception cases:
    //        * totalQuestions = 0 -> should throw IllegalArgumentException
    //        * correctAnswers < 0 -> should throw IllegalArgumentException
    //        * correctAnswers > totalQuestions -> should throw IllegalArgumentException
    //
    // 2. determineGrade:
    //    - EXCELLENT: 90, 100
    //    - GOOD: 75, 89
    //    - PASS: 60, 74
    //    - FAIL: 0, 59
    //    - Boundary invalid: negative percentage or > 100 throws IllegalArgumentException
    //
    // 3. calculateStreakBonus:
    //    - streak 0, 1, 2 -> 0 bonus
    //    - streak 3, 4 -> 10 bonus
    //    - streak 5, 10 -> 25 bonus
    //    - negative streak -> throws IllegalArgumentException
    // =========================================================================

    @Test
    fun calculatePercentage_severalCorrectAnswers_returnPercentage() {
        val result = calculator.calculatePercentage(correctAnswers = 5, totalQuestions = 10)
        assertEquals(50, result)
    }

    @Test
    fun calculatePercentage_zeroCorrectAnswers_returnZeroPercentage() {
        val result = calculator.calculatePercentage(correctAnswers = 0, totalQuestions = 10)
        assertEquals(0, result)
    }

    @Test
    fun calculatePercentage_allCorrectAnswers_return100Percentage() {
        val result = calculator.calculatePercentage(correctAnswers = 10, totalQuestions = 10)
        assertEquals(100, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculatePercentage_zeroTotalQuestions_throwsIllegalArgumentException() {
        calculator.calculatePercentage(correctAnswers = 10, totalQuestions = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculatePercentage_negativeCorrectAnswers_throwsIllegalArgumentException() {
        calculator.calculatePercentage(correctAnswers = -1, totalQuestions = 10)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculatePercentage_moreCorrectAnswersThanQuestion_throwsIllegalArgumentException() {
        calculator.calculatePercentage(correctAnswers = 11, totalQuestions = 10)
    }

    @Test
    fun determineGrade_correctPercentage_returnGrade() {
        assertEquals(ScoreGrade.FAIL, calculator.determineGrade(0))
        assertEquals(ScoreGrade.FAIL, calculator.determineGrade(59))
        assertEquals(ScoreGrade.PASS, calculator.determineGrade(60))
        assertEquals(ScoreGrade.PASS, calculator.determineGrade(74))
        assertEquals(ScoreGrade.GOOD, calculator.determineGrade(75))
        assertEquals(ScoreGrade.GOOD, calculator.determineGrade(89))
        assertEquals(ScoreGrade.EXCELLENT, calculator.determineGrade(90))
        assertEquals(ScoreGrade.EXCELLENT, calculator.determineGrade(100))
    }

    @Test(expected = IllegalArgumentException::class)
    fun determineGrade_negativePercentage_throwsIllegalArgumentException() {
        calculator.determineGrade(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun determineGrade_percentageMore100_throwsIllegalArgumentException() {
        calculator.determineGrade(101)
    }

    @Test
    fun calculateStreakBonus_correctStreak_returnBonus() {
        for (streak in 0..2) {
            assertEquals(0, calculator.calculateStreakBonus(streak))
        }
        for (streak in 3..4) {
            assertEquals(10, calculator.calculateStreakBonus(streak))
        }
        assertEquals(25, calculator.calculateStreakBonus(5))
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateStreakBonus_negativeStreak_throwsIllegalArgumentException() {
        calculator.calculateStreakBonus(-1)
    }
}
