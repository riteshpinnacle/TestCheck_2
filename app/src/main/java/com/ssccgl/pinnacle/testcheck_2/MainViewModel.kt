//package com.ssccgl.pinnacle.testcheck_2
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.launch
//import retrofit2.HttpException
//import java.io.IOException
//import java.net.SocketTimeoutException
//
//class MainViewModel : ViewModel() {
//
//    private val _data = MutableLiveData<List<ApiResponse>>()
//    val data: LiveData<List<ApiResponse>> = _data
//
//    private val _error = MutableLiveData<String?>()
//    val error: LiveData<String?> = _error
//
//    private val _saveAnswerResponse = MutableLiveData<SaveAnswerResponse?>()
//    val saveAnswerResponse: LiveData<SaveAnswerResponse?> = _saveAnswerResponse
//
//    private val paperCode = "3227"
//    private val emailId = "anshulji100@gmail.com"
//    private val examModeId = "1"
//    private val testSeriesId = "2"
//
//    init {
//        fetchData()
//    }
//
//    private fun fetchData() {
//        viewModelScope.launch {
//            try {
//                val response = RetrofitInstance.api.fetchData(
//                    FetchDataRequest(
//                        paper_code = paperCode,
//                        email_id = emailId,
//                        exam_mode_id = examModeId,
//                        test_series_id = testSeriesId
//                    )
//                )
//                _data.value = response
//                _error.value = null
//            } catch (e: SocketTimeoutException) {
//                _error.value = "Network timeout. Please try again later (By fetch Data)."
//                Log.e("MainViewModel", "SocketTimeoutException: ${e.message}")
//            } catch (e: IOException) {
//                _error.value = "Network error. Please check your connection. (By fetch Data)"
//                Log.e("MainViewModel", "IOException: ${e.message}")
//            } catch (e: HttpException) {
//                _error.value = "Server error: ${e.message}"
//                Log.e("MainViewModel", "HttpException: ${e.message}")
//            } catch (e: Exception) {
//                _error.value = e.message ?: "Unknown error (By fetch Data)"
//                Log.e("MainViewModel", "Exception: ${e.message}")
//            }
//        }
//    }
//
//    fun saveAnswer(
//        paperId: Int,
//        option: String,
//        subject: Int,
//        currentPaperId: Int,
//        remainingTime: String, // Added remainingTime parameter
//        singleTm: String
//    ) {
//        viewModelScope.launch {
//            try {
//                val response = RetrofitInstance.api.saveAnswer(
//                    SaveAnswerRequest(
//                        paper_code = paperCode,
//                        paper_id = paperId.toString(),
//                        option = option,
//                        email_id = emailId,
//                        test_series_id = testSeriesId,
//                        exam_mode_id = examModeId,
//                        subject = subject,
//                        CurrentPaperId = currentPaperId,
//                        SaveType = "nxt",
//                        answer_status = "1",
//                        rTem = remainingTime, // Set remaining time
//                        SingleTm = singleTm
//                    )
//                )
//                _saveAnswerResponse.value = response
//                _error.value = null
//            } catch (e: SocketTimeoutException) {
//                _error.value = "Network timeout. Please try again later. (By saveAnswer)"
//                Log.e("MainViewModel", "SocketTimeoutException: ${e.message}")
//            } catch (e: IOException) {
//                _error.value = "Network error. Please check your connection. (By saveAnswer)"
//                Log.e("MainViewModel", "IOException: ${e.message}")
//            } catch (e: HttpException) {
//                _error.value = "Server error: ${e.message}"
//                Log.e("MainViewModel", "HttpException: ${e.message}")
//            } catch (e: Exception) {
//                _error.value = e.message ?: "Unknown error (By saveAnswer)"
//                Log.e("MainViewModel", "Exception: ${e.message}")
//            }
//        }
//    }
//}

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssccgl.pinnacle.testcheck_2.ApiResponse
import com.ssccgl.pinnacle.testcheck_2.FetchDataRequest
import com.ssccgl.pinnacle.testcheck_2.RetrofitInstance
import com.ssccgl.pinnacle.testcheck_2.SaveAnswerRequest
import com.ssccgl.pinnacle.testcheck_2.SaveAnswerResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class MainViewModel : ViewModel() {

    private val _data = MutableLiveData<List<ApiResponse>>()
    val data: LiveData<List<ApiResponse>> = _data

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveAnswerResponse = MutableLiveData<SaveAnswerResponse?>()
    val saveAnswerResponse: LiveData<SaveAnswerResponse?> = _saveAnswerResponse

    private val paperCode = "3227"
    private val emailId = "anshulji100@gmail.com"
    private val examModeId = "1"
    private val testSeriesId = "2"

    // Temporary storage for answers
    private val answersMap = HashMap<Int, String>()

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
            } catch (e: SocketTimeoutException) {
                _error.value = "Network timeout. Please try again later (By fetch Data)."
                Log.e("MainViewModel", "SocketTimeoutException: ${e.message}")
            } catch (e: IOException) {
                _error.value = "Network error. Please check your connection. (By fetch Data)"
                Log.e("MainViewModel", "IOException: ${e.message}")
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.message}"
                Log.e("MainViewModel", "HttpException: ${e.message}")
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error (By fetch Data)"
                Log.e("MainViewModel", "Exception: ${e.message}")
            }
        }
    }

    fun saveAnswer(
        paperId: Int,
        option: String,
        subject: Int,
        currentPaperId: Int,
        remainingTime: String, // Added remainingTime parameter
        singleTm: String
    ) {
        // Save answer locally
        answersMap[currentPaperId] = option

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
                        rTem = remainingTime, // Set remaining time
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

    fun getSavedAnswer(questionId: Int): String? {
        return answersMap[questionId]
    }
}
