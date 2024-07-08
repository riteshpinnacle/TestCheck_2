//package com.ssccgl.pinnacle.testcheck_2
//
//import MainViewModel
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.Button
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.RadioButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import kotlinx.coroutines.delay
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DataScreen(viewModel: MainViewModel = viewModel()) {
//    val data by viewModel.data.observeAsState(emptyList())
//    val error by viewModel.error.observeAsState()
//
//    val details = data.flatMap { it.details }
//
//    var currentQuestionId by remember { mutableStateOf(1) } // Start with the first question_id
//    var selectedOption by remember { mutableStateOf("") } // To keep track of the selected option
//
//    var isDataLoaded by remember {mutableStateOf(false)} // To track if data is loaded
//
//    // Timer for individual questions
//    val startTimeMap = remember { mutableMapOf<Int, Long>() }
//    var elapsedTime by remember { mutableStateOf(0L) } // To track the elapsed time for the current question
//    var displayTime by remember { mutableStateOf("00:00") } // To display the elapsed time
//
//
//    // Start the timer only when data is displayed
//    LaunchedEffect(isDataLoaded, currentQuestionId) {
//        if (isDataLoaded) {
//            val currentQuestion = details.find { it.question_id == currentQuestionId }
//            if (currentQuestion != null) {
//                viewModel.getSavedAnswer(currentQuestionId)?.let {
//                    selectedOption = it
//                }
//                startTimeMap[currentQuestionId]?.let {
//                    elapsedTime = (System.currentTimeMillis() - it) / 1000
//                    startTimeMap[currentQuestionId] = System.currentTimeMillis() - elapsedTime * 1000
//                } ?: run {
//                    startTimeMap[currentQuestionId] = System.currentTimeMillis()
//                }
//            }
//        }
//    }
//
//    // Update the elapsed time every second
//    LaunchedEffect(currentQuestionId, isDataLoaded) {
//        if (isDataLoaded) {
//            while (true) {
//                elapsedTime = (System.currentTimeMillis() - (startTimeMap[currentQuestionId] ?: 0L)) / 1000
//                displayTime = formatTime(elapsedTime)
//                delay(1000L)
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Pinnacle SSC CGL Tier I") }
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            if (error != null) {
//                Text(
//                    text = error ?: "Unknown error",
//                    color = MaterialTheme.colorScheme.error,
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(16.dp)
//                )
//            } else {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp)
//                ) {
//
//                    val currentQuestion = details.find { it.question_id == currentQuestionId }
//
//                    if (currentQuestion != null) {
//                        isDataLoaded = true
//                        LazyColumn(
//                            modifier = Modifier.fillMaxSize(),
//                            verticalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            item {
//                                Text(
//                                    text = "Time: $displayTime",
//                                    style = MaterialTheme.typography.bodyLarge,
//                                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                                )
//                                Spacer(modifier = Modifier.height(16.dp))
//
//                                HtmlText(html = currentQuestion.question)
//                                Spacer(modifier = Modifier.height(16.dp))
//
//                                OptionItem(
//                                    option = currentQuestion.option1,
//                                    optionValue = "a",
//                                    selectedOption = selectedOption,
//                                    onSelectOption = { selectedOption = it }
//                                )
//                                OptionItem(
//                                    option = currentQuestion.option2,
//                                    optionValue = "b",
//                                    selectedOption = selectedOption,
//                                    onSelectOption = { selectedOption = it }
//                                )
//                                OptionItem(
//                                    option = currentQuestion.option3,
//                                    optionValue = "c",
//                                    selectedOption = selectedOption,
//                                    onSelectOption = { selectedOption = it }
//                                )
//                                OptionItem(
//                                    option = currentQuestion.option4,
//                                    optionValue = "d",
//                                    selectedOption = selectedOption,
//                                    onSelectOption = { selectedOption = it }
//                                )
//
//                                Spacer(modifier = Modifier.height(16.dp))
//                            }
//
//                            item {
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween
//                                ) {
//                                    if (currentQuestionId > 1) {
//                                        Button(
//                                            onClick = {
//                                                currentQuestionId--
//                                                selectedOption = viewModel.getSavedAnswer(currentQuestionId) ?: ""
//                                                startTimeMap[currentQuestionId]?.let {
//                                                    elapsedTime = (System.currentTimeMillis() - it) / 1000
//                                                } ?: run {
//                                                    startTimeMap[currentQuestionId] = System.currentTimeMillis()
//                                                }
//                                            },
//                                        ) {
//                                            Text("Previous")
//                                        }
//                                    }
//
//                                    if (currentQuestionId < details.maxOf { it.question_id }) {
//                                        Button(
//                                            onClick = {
//                                                elapsedTime = (System.currentTimeMillis() - (startTimeMap[currentQuestionId] ?: 0L)) / 1000 // Calculate elapsed time
//                                                viewModel.saveAnswer(
//                                                    paperId = currentQuestion.question_id,
//                                                    option = selectedOption.ifEmpty { "" },
//                                                    subject = currentQuestion.subject_id,
//                                                    currentPaperId = currentQuestionId,
//                                                    remainingTime = "",
//                                                    singleTm = (elapsedTime) // Save time in seconds
//                                                )
//                                                currentQuestionId++
//                                                selectedOption = viewModel.getSavedAnswer(currentQuestionId) ?: ""
//                                                startTimeMap[currentQuestionId] = System.currentTimeMillis() // Save start time for the current question
//                                            },
//                                        ) {
//                                            Text("Save and Next")
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        Text(
//                            text = "Questions are loading...",
//                            style = MaterialTheme.typography.bodyLarge,
//                            modifier = Modifier.align(Alignment.CenterHorizontally)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun OptionItem(option: String, optionValue: String, selectedOption: String, onSelectOption: (String) -> Unit) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        RadioButton(
//            selected = selectedOption == optionValue,
//            onClick = { onSelectOption(optionValue) }
//        )
//        HtmlText(html = option)
//    }
//}
//
//fun formatTime(seconds: Long): String {
//    val minutes = seconds / 60
//    val remainingSeconds = seconds % 60
//    return String.format("%02d:%02d", minutes, remainingSeconds)
//}
//
//


package com.ssccgl.pinnacle.testcheck_2

import MainViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(viewModel: MainViewModel = viewModel()) {
    val data by viewModel.data.observeAsState(emptyList())
    val error by viewModel.error.observeAsState()

    val details = data.flatMap { it.details }

    var currentQuestionId by remember { mutableStateOf(1) } // Start with the first question_id
    var selectedOption by remember { mutableStateOf("") } // To keep track of the selected option
    var isDataDisplayed by remember { mutableStateOf(false) } // To track if data is displayed

    // Timer for individual questions
    val startTimeMap = remember { mutableMapOf<Int, Long>() }
    val elapsedTimeMap = remember { mutableMapOf<Int, Long>() }
    var elapsedTime by remember { mutableStateOf(0L) } // To track the elapsed time for the current question
    var displayTime by remember { mutableStateOf("00:00") } // To display the elapsed time

    // Start or resume the timer when the question is displayed
    LaunchedEffect(Pair(isDataDisplayed, currentQuestionId)) {
        if (isDataDisplayed) {
            val currentQuestion = details.find { it.question_id == currentQuestionId }
            if (currentQuestion != null) {
                // Initialize or resume the elapsed time
                elapsedTime = elapsedTimeMap[currentQuestionId] ?: 0L
                startTimeMap[currentQuestionId] = System.currentTimeMillis()
            }
        }
    }

    // Update the elapsed time every second
    LaunchedEffect(currentQuestionId, isDataDisplayed) {
        if (isDataDisplayed) {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val startTime = startTimeMap[currentQuestionId] ?: currentTime
                elapsedTime = (elapsedTimeMap[currentQuestionId] ?: 0L) + (currentTime - startTime) / 1000
                displayTime = formatTime(elapsedTime)
                delay(1000L)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pinnacle SSC CGL Tier I") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (error != null) {
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    val currentQuestion = details.find { it.question_id == currentQuestionId }

                    if (currentQuestion != null) {
                        isDataDisplayed = true
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            item {
                                Text(
                                    text = "Time: $displayTime",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                HtmlText(html = currentQuestion.question)
                                Spacer(modifier = Modifier.height(16.dp))

                                OptionItem(
                                    option = currentQuestion.option1,
                                    optionValue = "a",
                                    selectedOption = selectedOption,
                                    onSelectOption = { selectedOption = it }
                                )
                                OptionItem(
                                    option = currentQuestion.option2,
                                    optionValue = "b",
                                    selectedOption = selectedOption,
                                    onSelectOption = { selectedOption = it }
                                )
                                OptionItem(
                                    option = currentQuestion.option3,
                                    optionValue = "c",
                                    selectedOption = selectedOption,
                                    onSelectOption = { selectedOption = it }
                                )
                                OptionItem(
                                    option = currentQuestion.option4,
                                    optionValue = "d",
                                    selectedOption = selectedOption,
                                    onSelectOption = { selectedOption = it }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (currentQuestionId > 1) {
                                        Button(
                                            onClick = {
                                                // Calculate and save elapsed time for the current question
                                                val currentTime = System.currentTimeMillis()
                                                val startTime = startTimeMap[currentQuestionId] ?: currentTime
                                                val elapsed = elapsedTimeMap[currentQuestionId] ?: 0L
                                                val newElapsedTime = elapsed + (currentTime - startTime) / 1000
                                                elapsedTimeMap[currentQuestionId] = newElapsedTime

                                                viewModel.saveAnswer(
                                                    paperId = currentQuestion.question_id,
                                                    option = selectedOption.ifEmpty { "" },
                                                    subject = currentQuestion.subject_id,
                                                    currentPaperId = currentQuestionId,
                                                    remainingTime = "",
                                                    singleTm = formatTime(newElapsedTime) // Save time in seconds
                                                )

                                                // Move to the previous question
                                                currentQuestionId--
                                                selectedOption = viewModel.getSavedAnswer(currentQuestionId) ?: ""
                                                elapsedTime = elapsedTimeMap[currentQuestionId] ?: 0L
                                                startTimeMap[currentQuestionId] = System.currentTimeMillis()
                                            },
                                        ) {
                                            Text("Previous")
                                        }
                                    }

                                    if (currentQuestionId < details.maxOf { it.question_id }) {
                                        Button(
                                            onClick = {
                                                // Calculate and save elapsed time for the current question
                                                val currentTime = System.currentTimeMillis()
                                                val startTime = startTimeMap[currentQuestionId] ?: currentTime
                                                val elapsed = elapsedTimeMap[currentQuestionId] ?: 0L
                                                val newElapsedTime = elapsed + (currentTime - startTime) / 1000
                                                elapsedTimeMap[currentQuestionId] = newElapsedTime

                                                viewModel.saveAnswer(
                                                    paperId = currentQuestion.question_id,
                                                    option = selectedOption.ifEmpty { "" },
                                                    subject = currentQuestion.subject_id,
                                                    currentPaperId = currentQuestionId,
                                                    remainingTime = "",
                                                    singleTm = formatTime(newElapsedTime)  // Save time in seconds
                                                )

                                                // Move to the next question
                                                currentQuestionId++
                                                selectedOption = viewModel.getSavedAnswer(currentQuestionId) ?: ""
                                                elapsedTime = elapsedTimeMap[currentQuestionId] ?: 0L
                                                startTimeMap[currentQuestionId] = System.currentTimeMillis()
                                            },
                                        ) {
                                            Text("Save and Next")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        isDataDisplayed = false
                        Text(
                            text = "Questions are loading...",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptionItem(option: String, optionValue: String, selectedOption: String, onSelectOption: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = selectedOption == optionValue,
            onClick = { onSelectOption(optionValue) }
        )
        HtmlText(html = option)
    }
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val tSecond = seconds % 60
    return String.format("%02d:%02d", minutes, tSecond)
}


// Test this code if it does not work then revert to commented version. There are many useful insights in commented version.