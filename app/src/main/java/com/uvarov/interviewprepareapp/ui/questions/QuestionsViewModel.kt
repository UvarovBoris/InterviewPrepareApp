package com.uvarov.interviewprepareapp.ui.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.usecase.GetQuestionsUseCase
import com.uvarov.interviewprepareapp.domain.usecase.RefreshQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val refreshQuestionsUseCase: RefreshQuestionsUseCase,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<QuestionCategory?>(QuestionCategory.ALL)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _questions = _selectedCategory.flatMapLatest { category ->
        getQuestionsUseCase(category)
    }

    val uiState: StateFlow<QuestionsUiState> = combine(
        _selectedCategory,
        _questions,
        _isRefreshing,
        _errorMessage
    ) { category, questions, isRefreshing, errorMessage ->
        QuestionsUiState(
            isLoading = false,
            isRefreshing = isRefreshing,
            questions = questions,
            selectedCategory = category,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuestionsUiState(isLoading = true)
    )

    init {
        refreshQuestions()
    }

    fun onEvent(event: QuestionsUiEvent) {
        when (event) {
            is QuestionsUiEvent.SelectCategory -> {
                _selectedCategory.value = event.category
            }

            QuestionsUiEvent.Refresh -> {
                refreshQuestions()
            }

            QuestionsUiEvent.DismissError -> {
                _errorMessage.value = null
            }
        }
    }

    private fun refreshQuestions() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            val result = refreshQuestionsUseCase()
            result.onFailure { error ->
                _errorMessage.value = error.localizedMessage ?: "Failed to fetch questions from network"
            }
            _isRefreshing.value = false
        }
    }
}
