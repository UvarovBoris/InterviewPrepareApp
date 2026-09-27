package com.uvarov.interviewprepareapp.practice

enum class ScoreGrade {
    EXCELLENT,
    GOOD,
    PASS,
    FAIL
}

/**
 * Utility class to calculate quiz/practice scores, grades, and streak bonuses.
 */
class PracticeScoreCalculator {

    /**
     * Calculates score percentage (0 to 100) rounded to the nearest integer.
     *
     * @param correctAnswers number of correct answers (must be non-negative)
     * @param totalQuestions total number of questions (must be > 0)
     * @throws IllegalArgumentException if totalQuestions <= 0, or correctAnswers < 0,
     *         or correctAnswers > totalQuestions
     */
    fun calculatePercentage(correctAnswers: Int, totalQuestions: Int): Int {
        require(totalQuestions > 0) { "Total questions must be greater than 0" }
        require(correctAnswers >= 0) { "Correct answers cannot be negative" }
        require(correctAnswers <= totalQuestions) { "Correct answers cannot exceed total questions" }

        val ratio = correctAnswers.toDouble() / totalQuestions.toDouble()
        return Math.round(ratio * 100).toInt()
    }

    /**
     * Determines grade based on percentage:
     * - 90..100: EXCELLENT
     * - 75..89:  GOOD
     * - 60..74:  PASS
     * - 0..59:   FAIL
     *
     * @throws IllegalArgumentException if percentage is not within 0..100
     */
    fun determineGrade(percentage: Int): ScoreGrade {
        require(percentage in 0..100) { "Percentage must be between 0 and 100" }

        return when {
            percentage >= 90 -> ScoreGrade.EXCELLENT
            percentage >= 75 -> ScoreGrade.GOOD
            percentage >= 60 -> ScoreGrade.PASS
            else -> ScoreGrade.FAIL
        }
    }

    /**
     * Calculates bonus points awarded for answer streaks.
     * - 0-2 consecutive correct: 0 bonus points
     * - 3-4 consecutive correct: 10 bonus points
     * - 5 or more consecutive correct: 25 bonus points (capped)
     *
     * @param streak count of consecutive correct answers (must be >= 0)
     * @throws IllegalArgumentException if streak < 0
     */
    fun calculateStreakBonus(streak: Int): Int {
        require(streak >= 0) { "Streak cannot be negative" }

        return when {
            streak >= 5 -> 25
            streak >= 3 -> 10
            else -> 0
        }
    }
}
