package com.ssccgl.pinnacle.testcheck_2

// Data classes for index
data class Subject(
    val sb_id: Int,
    val ppr_id: Int,
    val subject_name: String
)

data class Detail(
    val qid: Int,
    val question_id: Int,
    val subject_id: Int,
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val hindi_question: String,
    val positive_marks: String,
    val negative_marks: Double,
    val answered_ques: Int,
    val hrs: String,
    val mins: String,
    val secs: String,
    val answer: String
)

data class ApiResponse(
    val subjects: List<Subject>,
    val details: List<Detail>
)

// data classes for save_next

data class SaveAnswerRequest(
    val paper_code: String,
    val paper_id: String,
    val option: String,
    val answer_status: String,
    val email_id: String,
    val SingleTm: String,
    val rTem: String,
    val test_series_id: String,
    val exam_mode_id: String,
    val subject: Int,
    val CurrentPaperId: Int,
    val SaveType: String
)

data class SaveAnswerResponse(
    val sub_id: Int,
    val answer_status: Int,
    val answer_status_new: Int,
    val answered_count: Int,
    val notanswered_count: Int,
    val marked_count: Int,
    val marked_answered_count: Int,
    val not_visited: Int,
    val paper_ids: Int,
    val status: Int,
    val subjectname: String,
    val choosed_answer: String
)

// This peice has to be modified in the future as it is the request for the index
data class FetchDataRequest(
    val paper_code: String,
    val email_id: String,
    val exam_mode_id: String,
    val test_series_id: String
)


