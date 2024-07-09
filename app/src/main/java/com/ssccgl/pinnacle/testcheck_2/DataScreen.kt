package com.ssccgl.pinnacle.testcheck_2

import MainViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(viewModel: MainViewModel = viewModel()) {
    val data by viewModel.data.observeAsState(emptyList())
    val error by viewModel.error.observeAsState()

    val details = data.flatMap { it.details }

    var currentQuestionId by remember { mutableStateOf(1) } // Start with the first question_id
    var selectedOption by remember { mutableStateOf("") } // To keep track of the selected option
    var isDataDisplayed by remember { mutableStateOf(false) } // To track if data is displayed

    // Countdown state
    var remainingCountdown by remember { mutableStateOf(3600L) }
    var countdownStarted by remember { mutableStateOf(false) } // Track if the timer has started

    // Timer for individual questions
    val startTimeMap = remember { mutableMapOf<Int, Long>() }
    val elapsedTimeMap = remember { mutableMapOf<Int, Long>() }
    var elapsedTime by remember { mutableStateOf(0L) } // To track the elapsed time for the current question
    var displayTime by remember { mutableStateOf("00:00") } // To display the elapsed time

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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

    // Start the countdown only when the first question is displayed
    LaunchedEffect(isDataDisplayed) {
        if (isDataDisplayed && !countdownStarted) {
            countdownStarted = true
            while (remainingCountdown > 0) {
                delay(1000L)
                remainingCountdown--
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Cyan.copy(alpha = 0.5f),
        gesturesEnabled = true,
        modifier = Modifier.fillMaxWidth(),
        drawerContent = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                val buttonRows = details.chunked(5)
                items(buttonRows) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { detail ->
                            CircularButton(
                                onClick = {
                                    // Pause the timer for the current question
                                    val currentTime = System.currentTimeMillis()
                                    val startTime = startTimeMap[currentQuestionId] ?: currentTime
                                    val elapsed = elapsedTimeMap[currentQuestionId] ?: 0L
                                    val newElapsedTime = elapsed + (currentTime - startTime) / 1000
                                    elapsedTimeMap[currentQuestionId] = newElapsedTime

                                    viewModel.saveAnswer(
                                        paperId = currentQuestionId,
                                        option = selectedOption.ifEmpty { "" },
                                        subject = detail.subject_id,
                                        currentPaperId = currentQuestionId,
                                        remainingTime = formatTime(remainingCountdown),
                                        singleTm = formatTime(newElapsedTime) // Save time in seconds
                                    )

                                    // Update to the new question
                                    currentQuestionId = detail.question_id
                                    selectedOption = viewModel.getSavedAnswer(currentQuestionId) ?: ""
                                    elapsedTime = elapsedTimeMap[currentQuestionId] ?: 0L
                                    startTimeMap[currentQuestionId] = System.currentTimeMillis()
                                    coroutineScope.launch { drawerState.close() }
                                },
                                text = detail.question_id.toString()
                            )
                        }
                    }
                    Spacer(Modifier.padding(4.dp))
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Pinnacle SSC CGL Tier I") },
                        actions = {
                            IconButton(onClick = {
                                coroutineScope.launch { drawerState.open() }
                            }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Countdown: ${formatTime(remainingCountdown)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Time: $displayTime",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (error != null) {
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        val currentQuestion = details.find { it.question_id == currentQuestionId }

                        if (currentQuestion != null) {
                            isDataDisplayed = true
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    item {
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
                                                            remainingTime = formatTime(remainingCountdown),
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
    )
}

@Composable
fun CircularButton(onClick: () -> Unit, text: String) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(4.dp),
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
