package com.ssccgl.pinnacle.testcheck_2

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class MainViewModel : ViewModel() {

    private val _data = MutableStateFlow<List<IndexResponse>>(emptyList())
    val data: LiveData<List<IndexResponse>> = _data.asLiveData()

    private val _error = MutableStateFlow<String?>(null)
    val error: LiveData<String?> = _error.asLiveData()

    private val _saveAnswerResponse = MutableStateFlow<SaveAnswerResponse?>(null)
    val saveAnswerResponse: LiveData<SaveAnswerResponse?> = _saveAnswerResponse.asLiveData()

    private val _currentQuestionId = MutableStateFlow(1)
    val currentQuestionId: LiveData<Int> = _currentQuestionId.asLiveData()

    private val _selectedOption = MutableStateFlow("")
    val selectedOption: LiveData<String> = _selectedOption.asLiveData()

    private val _isDataDisplayed = MutableStateFlow(false)
    val isDataDisplayed: LiveData<Boolean> = _isDataDisplayed.asLiveData()

    private val _remainingCountdown = MutableStateFlow(3600L)
    val remainingCountdown: LiveData<Long> = _remainingCountdown.asLiveData()

    private val _countdownStarted = MutableStateFlow(false)
    val countdownStarted: LiveData<Boolean> = _countdownStarted.asLiveData()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: LiveData<Long> = _elapsedTime.asLiveData()

    private val _displayElapsedTime = MutableStateFlow("00:00:00")
    val displayElapsedTime: LiveData<String> = _displayElapsedTime.asLiveData()

    private val _displayCountdownTime = MutableStateFlow("00:00:00")
    val displayCountdownTime: LiveData<String> = _displayCountdownTime.asLiveData()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex.asLiveData()

    // Adds Title in DataScreen
    private val _title = MutableStateFlow("")
    val title: LiveData<String> = _title.asLiveData()

    // Adds values of answered, not answered etc.
    private val _paperCodeDetails = MutableStateFlow<PaperCodeDetailsResponse?>(null)
    val paperCodeDetails: LiveData<PaperCodeDetailsResponse?> = _paperCodeDetails.asLiveData()

    private val _markedForReviewMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val markedForReviewMap: LiveData<Map<Int, Boolean>> = _markedForReviewMap.asLiveData()

    val selectedOptions = mutableMapOf<Int, String>()
    val elapsedTimeMap = mutableMapOf<Int, Long>()
    val startTimeMap = mutableMapOf<Int, Long>()
    val answerTyp = MutableStateFlow<Map<Int, Int>>(emptyMap())


    private val paperCode = "3227"
    private val emailId = "anshulji100@gmail.com"
    private val examModeId = "1"
    private val testSeriesId = "2"

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.fetchData(
                    FetchDataRequest(
                        paper_code = paperCode,
                        email_id = emailId,
                        exam_mode_id = examModeId,
                        test_series_id = testSeriesId
                    )
                )

                _data.value = response

                // Initialize selectedOptions and answerTyp
                val initialAnswerTyp = mutableMapOf<Int, Int>()
                val initialMarkedForReviewMap = mutableMapOf<Int, Boolean>()
                response.flatMap { it.details }.forEach { detail ->
                    selectedOptions[detail.qid] = detail.answer
                    initialAnswerTyp[detail.qid] = detail.answered_ques

                    // Set initial marked for review based on answerTyp
                    initialMarkedForReviewMap[detail.qid] = detail.answered_ques == 3 || detail.answered_ques == 4
                }
                answerTyp.value = initialAnswerTyp
                _markedForReviewMap.value = initialMarkedForReviewMap

                // Ensure the first question ID is set correctly
                if (response.isNotEmpty() && response[0].details.isNotEmpty()) {
                    _currentQuestionId.value = response[0].details[0].qid
                    setSelectedOption(response[0].details[0].qid)
                    initializeElapsedTime(response[0].details[0].qid) // Initialize elapsed time for the first question
                }
                _error.value = null

                fetchPaperCodeDetails()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun fetchPaperCodeDetails() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.fetchPaperCodeDetails(
                    FetchDataRequest(
                        paper_code = paperCode,
                        email_id = emailId,
                        exam_mode_id = examModeId,
                        test_series_id = testSeriesId
                    )
                )
                val totalSeconds = response.hrs * 3600 + response.mins * 60 + response.secs
                _remainingCountdown.value = totalSeconds.toLong()
                _displayCountdownTime.value = formatTime(totalSeconds.toLong())
                _title.value = response.title // Update the title
                _paperCodeDetails.value = response // Fetches the values of answered, not answered etc, from api.

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveCurrentQuestionState(questionId: Int, option: String, elapsedTime: Long) {
        selectedOptions[questionId] = option
        elapsedTimeMap[questionId] = elapsedTime
    }

    fun setSelectedOption(questionId: Int) {
        _selectedOption.value = selectedOptions[questionId] ?: ""
    }

    fun initializeElapsedTime(questionId: Int) {
    val previousElapsedTime = elapsedTimeMap[questionId] ?: run {
        val questionDetail = _data.value.flatMap { it.details }.find { it.qid == questionId }
        val initialElapsedTime = questionDetail?.let {
            val hours = it.hrs.toLongOrNull() ?: 0L
            val minutes = it.mins.toLongOrNull() ?: 0L
            val seconds = it.secs.toLongOrNull() ?: 0L
            hours * 3600 + minutes * 60 + seconds
        } ?: 0L
        elapsedTimeMap[questionId] = initialElapsedTime // Ensure it is stored in the map
        initialElapsedTime
    }
    _elapsedTime.value = previousElapsedTime
    _displayElapsedTime.value = formatTime(previousElapsedTime)
    startTimeMap[questionId] = System.currentTimeMillis()
    }

    fun updateElapsedTime(questionId: Int) {
        val currentTime = System.currentTimeMillis()
        val startTime = startTimeMap[questionId] ?: currentTime
        _elapsedTime.value = (elapsedTimeMap[questionId] ?: 0L) + (currentTime - startTime) / 1000
        _displayElapsedTime.value = formatTime(_elapsedTime.value)
    }

    fun startCountdown() {
        viewModelScope.launch {
            _countdownStarted.value = true
            while (_remainingCountdown.value > 0) {
                delay(1000L)
                _remainingCountdown.value--
                _displayCountdownTime.value = formatTime(_remainingCountdown.value)
            }
        }
    }

    fun moveToQuestion(questionId: Int) {
        saveCurrentQuestionState(_currentQuestionId.value, _selectedOption.value, _elapsedTime.value) // Save current question state
        _currentQuestionId.value = questionId
        initializeElapsedTime(questionId)
        setSelectedOption(questionId)
    }

    fun moveToSection(index: Int) {
        val selectedSubject = _data.value.flatMap { it.subjects }[index]
        val newQuestionId = _data.value.flatMap { it.details }
            .find { it.subject_id == selectedSubject.sb_id && it.qid == selectedSubject.ppr_id }?.qid ?: 1

        // Save the current state with SaveType = "nav"
        saveAnswer(
            paperId = _currentQuestionId.value,
            option = validateOption(_selectedOption.value),
            subject = selectedSubject.sb_id,
            currentPaperId = newQuestionId,
            remainingTime = formatTime(_remainingCountdown.value),
            singleTm = formatTime(_elapsedTime.value),
            saveType = "nav",
            answerStatus = if (isMarkedForReview(_currentQuestionId.value)) "4" else "1"
        )
        moveToQuestion(newQuestionId)
        _selectedTabIndex.value = index
    }

    fun updateSelectedOption(option: String) {
        val validatedOption = validateOption(option)
        _selectedOption.value = validatedOption
    }


    fun updateAnswerTyp(qid: Int, option: String) {
        val isMarkedForReview = markedForReviewMap.value?.get(qid) ?: false
        val newAnswerType = when {
            option.isBlank() -> {
                if (isMarkedForReview) 3 else 2
            }
            option in listOf("a", "b", "c", "d") -> {
                if (isMarkedForReview) 4 else 1
            }
            else -> 0
        }
        answerTyp.value = answerTyp.value.toMutableMap().apply { put(qid, newAnswerType) }
    }


    fun moveToPreviousQuestion() {
        val previousQuestionId = _currentQuestionId.value - 1
        moveToQuestion(previousQuestionId)
    }

    fun moveToNextQuestion() {
        val nextQuestionId = _currentQuestionId.value + 1
        moveToQuestion(nextQuestionId)
    }

    fun setIsDataDisplayed(isDisplayed: Boolean) {
        _isDataDisplayed.value = isDisplayed
    }

    fun validateOption(option: String): String {
        val validateOption = if (option in listOf("a", "b", "c", "d")) option else ""
        updateAnswerTyp(_currentQuestionId.value, validateOption)
        return validateOption
    }

    fun toggleMarkForReview(questionId: Int) {
        val currentMarkedStatus = _markedForReviewMap.value[questionId] ?: false
        _markedForReviewMap.value = _markedForReviewMap.value.toMutableMap().apply {
            put(questionId, !currentMarkedStatus)
        }
    }

    fun isMarkedForReview(questionId: Int): Boolean {
        return _markedForReviewMap.value[questionId] ?: false
    }

    fun saveAnswer(
        paperId: Int,
        option: String,
        subject: Int,
        currentPaperId: Int,
        remainingTime: String,
        singleTm: String,
        saveType: String,
        answerStatus: String
    ) {
        val validatedOption = validateOption(option)
        Log.d("MainViewModel", "Saving answer: saveType = $saveType, paperId=$paperId, option=$option, subject=$subject, currentPaperId=$currentPaperId, remainingTime=$remainingTime, singleTm=$singleTm")
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.saveAnswer(
                    SaveAnswerRequest(
                        paper_code = paperCode,
                        paper_id = paperId.toString(),
                        option = validatedOption,
                        email_id = emailId,
                        test_series_id = testSeriesId,
                        exam_mode_id = examModeId,
                        subject = subject,
                        CurrentPaperId = currentPaperId,
                        SaveType = saveType,
                        answer_status = answerStatus,
                        rTem = remainingTime,
                        SingleTm = singleTm
                    )
                )
                _saveAnswerResponse.value = response
                _error.value = null

                // Update the selectedOptions map with the new answer
                selectedOptions[currentPaperId] = validatedOption

                // Update answerTyp
                updateAnswerTyp(currentPaperId, validatedOption)

                // Fetch the updated PaperCodeDetailsResponse after saving the answer
                fetchPaperCodeDetails()

            } catch (e: SocketTimeoutException) {
                _error.value = "Network timeout. Please try again later. (By saveAnswer)"
                Log.e("MainViewModel", "SocketTimeoutException: ${e.message}")
            } catch (e: IOException) {
                _error.value = "Network error. Please check your connection. (By saveAnswer)"
                Log.e("MainViewModel", "IOException: ${e.message}")
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.message}"
                Log.e("MainViewModel", "HttpException: ${e.message}")
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error (By saveAnswer)"
                Log.e("MainViewModel", "Exception: ${e.message}")
            }
        }
    }

    fun clearResponse() {
        val currentQid = _currentQuestionId.value
        selectedOptions[currentQid] = ""
        _selectedOption.value = ""
    }

    fun submit() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.submit(
                    SubmitRequest(
                        email_id = emailId,
                        paper_code = paperCode,
                        exam_mode_id = examModeId,
                        test_series_id = testSeriesId,
                        rTem = formatTime(_remainingCountdown.value)
                    )
                )
                // Handle response if needed
            } catch (e: SocketTimeoutException) {
                _error.value = "Network timeout. Please try again later. (By submit)"
                Log.e("MainViewModel", "SocketTimeoutException: ${e.message}")
            } catch (e: IOException) {
                _error.value = "Network error. Please check your connection. (By submit)"
                Log.e("MainViewModel", "IOException: ${e.message}")
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.message}"
                Log.e("MainViewModel", "HttpException: ${e.message}")
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error (By submit)"
                Log.e("MainViewModel", "Exception: ${e.message}")
            }
        }
    }
}
