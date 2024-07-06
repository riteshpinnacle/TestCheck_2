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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(viewModel: MainViewModel = viewModel()) {
    val data by viewModel.data.observeAsState(emptyList())
    val error by viewModel.error.observeAsState()

    val details = data.flatMap { it.details }

    var currentQuestionId by remember { mutableStateOf(1) } // Start with the first question_id
    var selectedOption by remember { mutableStateOf("") } // To keep track of the selected option

    // Check for a saved answer when the question ID changes
    LaunchedEffect(currentQuestionId) {
        viewModel.getSavedAnswer(currentQuestionId)?.let {
            selectedOption = it
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
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
                                                currentQuestionId--
                                            },
                                        ) {
                                            Text("Previous")
                                        }
                                    }

                                    if (currentQuestionId < details.maxOf { it.question_id }) {
                                        Button(
                                            onClick = {
                                                viewModel.saveAnswer(
                                                    paperId = currentQuestion.question_id,
                                                    option = selectedOption.ifEmpty { "" },
                                                    subject = currentQuestion.subject_id,
                                                    currentPaperId = currentQuestionId,
                                                    remainingTime = "",
//                                                    singleTm = formatTime(timeTaken / 1000)
                                                    singleTm = ""
                                                )
                                                currentQuestionId++
                                                selectedOption = "" // Reset selected option for next question
//                                                startTime = System.currentTimeMillis() // Reset start time for the next question
                                            },
                                        ) {
                                            Text("Save and Next")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
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
