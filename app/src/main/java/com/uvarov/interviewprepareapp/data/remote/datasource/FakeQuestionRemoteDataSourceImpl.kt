package com.uvarov.interviewprepareapp.data.remote.datasource

import com.uvarov.interviewprepareapp.data.remote.dto.QuestionDto
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeQuestionRemoteDataSourceImpl @Inject constructor() : QuestionRemoteDataSource {

    override suspend fun fetchQuestions(): List<QuestionDto> {
        // Simulate network latency
        delay(800)

        return listOf(
            QuestionDto(
                id = "q1",
                title = "What is the difference between StateFlow and SharedFlow?",
                category = "COROUTINES",
                difficulty = "MEDIUM",
                answerSummary = "StateFlow is a hot Flow designed to maintain and emit a single current state to collectors. SharedFlow is a configurable hot Flow that can emit events to multiple collectors and optionally replay past values.",
                codeExample = "val state = MutableStateFlow(InitialState)\nval events = MutableSharedFlow<Event>()"
            ),
            QuestionDto(
                id = "q2",
                title = "Explain Recomposition in Jetpack Compose and how to optimize it.",
                category = "COMPOSE",
                difficulty = "HARD",
                answerSummary = "Recomposition is the process where Compose re-executes composables when their state inputs change. Optimize it by using @Immutable / @Stable models, remember {}, derivedStateOf {}, and key() in LazyColumn.",
                codeExample = "@Composable\nfun UserProfile(user: User) {\n    Text(text = user.name)\n}"
            ),
            QuestionDto(
                id = "q3",
                title = "What are Kotlin Inline Functions and reified type parameters?",
                category = "KOTLIN",
                difficulty = "MEDIUM",
                answerSummary = "Inline functions copy their bytecode directly to the call site, eliminating object allocation overhead for higher-order functions. 'reified' allows inspecting type parameters at runtime.",
                codeExample = "inline fun <reified T> printType() {\n    println(T::class.java.name)\n}"
            ),
            QuestionDto(
                id = "q4",
                title = "How does Unidirectional Data Flow (UDF) work in Android Clean Architecture?",
                category = "ARCHITECTURE",
                difficulty = "EASY",
                answerSummary = "In UDF, state flows down from ViewModel to UI, and user events flow up from UI to ViewModel. This ensures predictable state transitions and easy unit testing.",
                codeExample = "// UI observes state and sends events\nval uiState by viewModel.uiState.collectAsStateWithLifecycle()"
            ),
            QuestionDto(
                id = "q5",
                title = "What is the Android Activity Lifecycle and Edge-to-Edge display?",
                category = "ANDROID",
                difficulty = "EASY",
                answerSummary = "The Activity lifecycle manages components from onCreate to onDestroy. Edge-to-edge content draws behind system bars (status & navigation) using enableEdgeToEdge() and WindowInsets.",
                codeExample = "override fun onCreate(savedInstanceState: Bundle?) {\n    super.onCreate(savedInstanceState)\n    enableEdgeToEdge()\n}"
            ),
            QuestionDto(
                id = "q6",
                title = "How does Structured Concurrency prevent memory leaks in Coroutines?",
                category = "COROUTINES",
                difficulty = "HARD",
                answerSummary = "Structured concurrency ensures child coroutines are bound to a CoroutineScope (like viewModelScope). When the scope is cancelled, all running child jobs are cancelled automatically.",
                codeExample = "viewModelScope.launch {\n    // Automatic cancellation on ViewModel cleared\n}"
            ),
            QuestionDto(
                id = "q7",
                title = "What is the difference between remember and rememberSaveable in Compose?",
                category = "COMPOSE",
                difficulty = "EASY",
                answerSummary = "'remember' keeps values across recompositions during the same composition session. 'rememberSaveable' survives configuration changes (e.g., screen rotation) using Bundle state.",
                codeExample = "var text by rememberSaveable { mutableStateOf(\"\") }"
            ),
            QuestionDto(
                id = "q8",
                title = "Explain Dependency Injection with Hilt in Android.",
                category = "ARCHITECTURE",
                difficulty = "MEDIUM",
                answerSummary = "Hilt is a compile-time DI framework built on Dagger. It automatically handles the creation, scope, and destruction of dependencies bound to Android lifecycles (@Singleton, @ViewModelScoped).",
                codeExample = "@HiltViewModel\nclass MyViewModel @Inject constructor(\n    private val repository: Repository\n) : ViewModel()"
            )
        )
    }
}
