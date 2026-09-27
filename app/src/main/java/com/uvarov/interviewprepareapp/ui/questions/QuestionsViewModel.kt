package com.uvarov.interviewprepareapp.ui.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.usecase.GetQuestionsUseCase
import com.uvarov.interviewprepareapp.domain.usecase.RefreshQuestionsUseCase
import com.uvarov.interviewprepareapp.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val refreshQuestionsUseCase: RefreshQuestionsUseCase,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<QuestionCategory?>(QuestionCategory.ALL)

    private val _uiState = MutableStateFlow(QuestionsUiState(isLoading = true))
    val uiState: StateFlow<QuestionsUiState> = _uiState.asStateFlow()

    init {
        observeQuestions()
        refreshQuestions()
    }

    fun onEvent(event: QuestionsUiEvent) {
        when (event) {
            is QuestionsUiEvent.SelectCategory -> {
                _selectedCategory.value = event.category
                _uiState.update { it.copy(selectedCategory = event.category) }
            }

            is QuestionsUiEvent.ToggleBookmark -> {
                toggleBookmark(event.id)
            }

            QuestionsUiEvent.Refresh -> {
                refreshQuestions()
            }

            QuestionsUiEvent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun observeQuestions() {
        _selectedCategory
            .flatMapLatest { category ->
                getQuestionsUseCase(category)
            }
            .onEach { questions ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        questions = questions
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun refreshQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = refreshQuestionsUseCase()
            result.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = error.localizedMessage ?: "Failed to fetch questions from network"
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun toggleBookmark(id: String) {
        viewModelScope.launch {
            toggleBookmarkUseCase(id)
        }
    }
}
