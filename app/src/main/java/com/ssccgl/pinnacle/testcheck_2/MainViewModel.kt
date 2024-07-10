package com.ssccgl.pinnacle.testcheck_2

import android.util.Log
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

    private val _data = MutableStateFlow<List<ApiResponse>>(emptyList())
    val data: LiveData<List<ApiResponse>> = _data.asLiveData()

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

    private val _displayTime = MutableStateFlow("00:00")
    val displayTime: LiveData<String> = _displayTime.asLiveData()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex.asLiveData()

    val selectedOptions = mutableMapOf<Int, String>()
    val elapsedTimeMap = mutableMapOf<Int, Long>()
    val startTimeMap = mutableMapOf<Int, Long>()

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
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveCurrentQuestionState(questionId: Int, option: String, elapsedTime: Long) {
        selectedOptions[questionId] = option
        elapsedTimeMap[questionId] = elapsedTime
    }

    fun initializeElapsedTime(questionId: Int) {
        _elapsedTime.value = elapsedTimeMap[questionId] ?: 0L
        startTimeMap[questionId] = System.currentTimeMillis()
    }

    fun setSelectedOption(questionId: Int) {
        _selectedOption.value = selectedOptions[questionId] ?: ""
    }

    fun updateElapsedTime(questionId: Int) {
        val currentTime = System.currentTimeMillis()
        val startTime = startTimeMap[questionId] ?: currentTime
        _elapsedTime.value = (elapsedTimeMap[questionId] ?: 0L) + (currentTime - startTime) / 1000
        _displayTime.value = formatTime(_elapsedTime.value)
    }

    fun startCountdown() {
        viewModelScope.launch {
            _countdownStarted.value = true
            while (_remainingCountdown.value > 0) {
                delay(1000L)
                _remainingCountdown.value--
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
        _selectedTabIndex.value = index
        val newQuestionId = when (index) {
            0 -> 1
            1 -> 26
            2 -> 51
            3 -> 76
            else -> 1
        }
        moveToQuestion(newQuestionId)
    }

    fun updateSelectedOption(option: String) {
        _selectedOption.value = option
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

    fun saveAnswer(
        paperId: Int,
        option: String,
        subject: Int,
        currentPaperId: Int,
        remainingTime: String,
        singleTm: String
    ) {
        Log.d("MainViewModel", "Saving answer: paperId=$paperId, option=$option, subject=$subject, currentPaperId=$currentPaperId, remainingTime=$remainingTime, singleTm=$singleTm")
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.saveAnswer(
                    SaveAnswerRequest(
                        paper_code = paperCode,
                        paper_id = paperId.toString(),
                        option = option,
                        email_id = emailId,
                        test_series_id = testSeriesId,
                        exam_mode_id = examModeId,
                        subject = subject,
                        CurrentPaperId = currentPaperId,
                        SaveType = "nxt",
                        answer_status = "1",
                        rTem = remainingTime,
                        SingleTm = singleTm
                    )
                )
                _saveAnswerResponse.value = response
                _error.value = null
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
