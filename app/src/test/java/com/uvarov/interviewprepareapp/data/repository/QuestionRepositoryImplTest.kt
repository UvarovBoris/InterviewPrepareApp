package com.uvarov.interviewprepareapp.data.repository

import app.cash.turbine.test
import com.uvarov.interviewprepareapp.data.local.dao.QuestionDao
import com.uvarov.interviewprepareapp.data.local.entity.QuestionEntity
import com.uvarov.interviewprepareapp.data.remote.datasource.QuestionRemoteDataSource
import com.uvarov.interviewprepareapp.data.remote.dto.QuestionDto
import com.uvarov.interviewprepareapp.domain.model.Difficulty
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class QuestionRepositoryImplTest {

    private val questionDao: QuestionDao = mockk(relaxed = true)
    private val remoteDataSource: QuestionRemoteDataSource = mockk()

    private lateinit var repository: QuestionRepositoryImpl

    private val sampleEntity1 = QuestionEntity(
        id = "1",
        title = "Activity Lifecycle",
        category = "ANDROID",
        difficulty = "EASY",
        answerSummary = "onCreate, onStart, onResume...",
        codeExample = "override fun onCreate()"
    )

    private val sampleEntity2 = QuestionEntity(
        id = "2",
        title = "Coroutines Dispatchers",
        category = "KOTLIN",
        difficulty = "MEDIUM",
        answerSummary = "Dispatchers.Main, IO, Default",
        codeExample = null
    )

    private val sampleDto1 = QuestionDto(
        id = "1",
        title = "Activity Lifecycle",
        category = "ANDROID",
        difficulty = "EASY",
        answerSummary = "onCreate, onStart, onResume...",
        codeExample = "override fun onCreate()"
    )

    private val sampleDto2 = QuestionDto(
        id = "2",
        title = "Coroutines Dispatchers",
        category = "KOTLIN",
        difficulty = "MEDIUM",
        answerSummary = "Dispatchers.Main, IO, Default",
        codeExample = null
    )

    @Before
    fun setUp() {
        repository = QuestionRepositoryImpl(
            questionDao = questionDao,
            remoteDataSource = remoteDataSource
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ==========================================
    // Group 1: getQuestions(category)
    // ==========================================

    @Test
    fun `getQuestions with null category observes all questions and maps to domain`() = runTest {
        every { questionDao.observeAllQuestions() } returns flowOf(listOf(sampleEntity1, sampleEntity2))

        repository.getQuestions(null).test {
            val questions = awaitItem()
            assertEquals(2, questions.size)

            val q1 = questions[0]
            assertEquals("1", q1.id)
            assertEquals("Activity Lifecycle", q1.title)
            assertEquals(QuestionCategory.ANDROID, q1.category)
            assertEquals(Difficulty.EASY, q1.difficulty)
            assertEquals("onCreate, onStart, onResume...", q1.answerSummary)
            assertEquals("override fun onCreate()", q1.codeExample)

            val q2 = questions[1]
            assertEquals("2", q2.id)
            assertEquals("Coroutines Dispatchers", q2.title)
            assertEquals(QuestionCategory.KOTLIN, q2.category)
            assertEquals(Difficulty.MEDIUM, q2.difficulty)
            assertEquals("Dispatchers.Main, IO, Default", q2.answerSummary)
            assertEquals(null, q2.codeExample)

            awaitComplete()
        }

        verify(exactly = 1) { questionDao.observeAllQuestions() }
        verify(exactly = 0) { questionDao.observeQuestionsByCategory(any()) }
    }

    @Test
    fun `getQuestions with QuestionCategory ALL observes all questions and maps to domain`() = runTest {
        every { questionDao.observeAllQuestions() } returns flowOf(listOf(sampleEntity1))

        repository.getQuestions(QuestionCategory.ALL).test {
            val questions = awaitItem()
            assertEquals(1, questions.size)
            assertEquals("1", questions[0].id)
            awaitComplete()
        }

        verify(exactly = 1) { questionDao.observeAllQuestions() }
        verify(exactly = 0) { questionDao.observeQuestionsByCategory(any()) }
    }

    @Test
    fun `getQuestions with specific category queries DAO by exact category name`() = runTest {
        every { questionDao.observeQuestionsByCategory("ANDROID") } returns flowOf(listOf(sampleEntity1))

        repository.getQuestions(QuestionCategory.ANDROID).test {
            val questions = awaitItem()
            assertEquals(1, questions.size)
            assertEquals(QuestionCategory.ANDROID, questions[0].category)
            awaitComplete()
        }

        verify(exactly = 1) { questionDao.observeQuestionsByCategory("ANDROID") }
        verify(exactly = 0) { questionDao.observeAllQuestions() }
    }

    @Test
    fun `getQuestions with each specific category queries DAO with respective name`() = runTest {
        val specificCategories = QuestionCategory.entries.filter { it != QuestionCategory.ALL }

        for (category in specificCategories) {
            val entity = sampleEntity1.copy(category = category.name)
            every { questionDao.observeQuestionsByCategory(category.name) } returns flowOf(listOf(entity))

            repository.getQuestions(category).test {
                val questions = awaitItem()
                assertEquals(1, questions.size)
                assertEquals(category, questions[0].category)
                awaitComplete()
            }

            verify(exactly = 1) { questionDao.observeQuestionsByCategory(category.name) }
        }
    }

    @Test
    fun `getQuestions maps unknown category and difficulty to default fallbacks`() = runTest {
        val unknownEntity = QuestionEntity(
            id = "999",
            title = "Unknown Tech",
            category = "FLUTTER_UNKNOWN",
            difficulty = "NIGHTMARE",
            answerSummary = "Fallback summary",
            codeExample = null
        )
        every { questionDao.observeAllQuestions() } returns flowOf(listOf(unknownEntity))

        repository.getQuestions(null).test {
            val questions = awaitItem()
            assertEquals(1, questions.size)
            assertEquals(QuestionCategory.ANDROID, questions[0].category) // fallback from QuestionMappers
            assertEquals(Difficulty.MEDIUM, questions[0].difficulty) // fallback from QuestionMappers
            awaitComplete()
        }
    }

    @Test
    fun `getQuestions emits empty list when DAO returns empty list`() = runTest {
        every { questionDao.observeAllQuestions() } returns flowOf(emptyList())

        repository.getQuestions(null).test {
            val questions = awaitItem()
            assertTrue(questions.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `getQuestions emits new domain items reactively when DAO emits sequential updates`() = runTest {
        val flow = MutableSharedFlow<List<QuestionEntity>>()
        every { questionDao.observeAllQuestions() } returns flow

        repository.getQuestions(null).test {
            // First DB emission
            flow.emit(listOf(sampleEntity1))
            val firstList = awaitItem()
            assertEquals(1, firstList.size)
            assertEquals("1", firstList[0].id)

            // Second DB emission (e.g. after refresh or toggle)
            flow.emit(listOf(sampleEntity1, sampleEntity2))
            val secondList = awaitItem()
            assertEquals(2, secondList.size)
            assertEquals("2", secondList[1].id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getQuestions propagates DAO flow error to collector`() = runTest {
        val expectedException = IllegalStateException("Database read error")
        every { questionDao.observeAllQuestions() } returns flow {
            throw expectedException
        }

        repository.getQuestions(null).test {
            val error = awaitError()
            assertTrue(error is IllegalStateException)
            assertEquals("Database read error", error.message)
        }
    }

    // ==========================================
    // Group 2: refreshQuestions()
    // ==========================================

    @Test
    fun `refreshQuestions fetches from remote, maps to entities, inserts to DAO, and returns success`() = runTest {
        coEvery { remoteDataSource.fetchQuestions() } returns listOf(sampleDto1, sampleDto2)
        coEvery { questionDao.insertQuestions(any()) } returns Unit

        val result = repository.refreshQuestions()

        assertTrue(result.isSuccess)

        val expectedEntities = listOf(
            QuestionEntity(
                id = "1",
                title = "Activity Lifecycle",
                category = "ANDROID",
                difficulty = "EASY",
                answerSummary = "onCreate, onStart, onResume...",
                codeExample = "override fun onCreate()"
            ),
            QuestionEntity(
                id = "2",
                title = "Coroutines Dispatchers",
                category = "KOTLIN",
                difficulty = "MEDIUM",
                answerSummary = "Dispatchers.Main, IO, Default",
                codeExample = null
            )
        )

        coVerify(exactly = 1) { remoteDataSource.fetchQuestions() }
        coVerify(exactly = 1) { questionDao.insertQuestions(expectedEntities) }
    }

    @Test
    fun `refreshQuestions returns failure and does not call insertQuestions when remoteDataSource throws`() = runTest {
        val networkException = IOException("Network connection timed out")
        coEvery { remoteDataSource.fetchQuestions() } throws networkException

        val result = repository.refreshQuestions()

        assertTrue(result.isFailure)
        assertEquals(networkException, result.exceptionOrNull())
        coVerify(exactly = 1) { remoteDataSource.fetchQuestions() }
        coVerify(exactly = 0) { questionDao.insertQuestions(any()) }
    }

    @Test
    fun `refreshQuestions returns failure when questionDao insertQuestions throws`() = runTest {
        coEvery { remoteDataSource.fetchQuestions() } returns listOf(sampleDto1)
        val dbException = IllegalStateException("Disk full / SQLite error")
        coEvery { questionDao.insertQuestions(any()) } throws dbException

        val result = repository.refreshQuestions()

        assertTrue(result.isFailure)
        assertEquals(dbException, result.exceptionOrNull())
        coVerify(exactly = 1) { remoteDataSource.fetchQuestions() }
        coVerify(exactly = 1) { questionDao.insertQuestions(any()) }
    }

    @Test
    fun `refreshQuestions handles empty remote list by inserting empty list and returning success`() = runTest {
        coEvery { remoteDataSource.fetchQuestions() } returns emptyList()
        coEvery { questionDao.insertQuestions(emptyList()) } returns Unit

        val result = repository.refreshQuestions()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { remoteDataSource.fetchQuestions() }
        coVerify(exactly = 1) { questionDao.insertQuestions(emptyList()) }
    }
}
