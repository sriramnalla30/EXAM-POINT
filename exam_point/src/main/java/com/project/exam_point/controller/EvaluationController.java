package com.project.exam_point.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.exam_point.model.Exam;
import com.project.exam_point.model.ExamSubmission;
import com.project.exam_point.model.Question;
import com.project.exam_point.service.AnswerEvaluationService;
import com.project.exam_point.service.ExamService;
import com.project.exam_point.service.ExamSubmissionService;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin(origins = "http://localhost:3000")
public class EvaluationController {

    private final AnswerEvaluationService answerEvaluationService;
    private final ExamService examService;
    private final ExamSubmissionService examSubmissionService;


    public EvaluationController(AnswerEvaluationService answerEvaluationService, 
            ExamService examService, 
            ExamSubmissionService examSubmissionService) {
this.answerEvaluationService = answerEvaluationService;
this.examService = examService;
this.examSubmissionService = examSubmissionService;
}

   
    @GetMapping("/{examId}")
    public ResponseEntity<List<Map<String, Object>>> getEvaluationDetails(@PathVariable Long examId) {
        try {
            // Fetch the exam details
            Exam exam = examService.findById(examId);
            if (exam == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(List.of(Map.of("message", "Exam not found")));
            }

            // Fetch submissions for the given exam
            List<ExamSubmission> examSubmissions = examSubmissionService.findSubmissionsByExamId(examId);
            if (examSubmissions == null || examSubmissions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(List.of(Map.of("message", "No submissions found for this exam")));
            }

            // Process evaluation details
            List<Map<String, Object>> evaluationDetails = examSubmissions.stream().map(submission -> {
                Question question = submission.getQuestion();
                String teacherAnswer = question.getAnswer() != null ? question.getAnswer().trim() : "";
                String studentAnswer = submission.getAnswer() != null ? submission.getAnswer().trim() : "";
                String ollamaResponse;

                // Call the AI service to evaluate the answer
                if ("LONG_ANSWER".equalsIgnoreCase(question.getQuestionType())) {
                    boolean result = answerEvaluationService.validateLongAnswer(teacherAnswer, studentAnswer);
                    ollamaResponse = result ? "true" : "false";
                    System.out.println("LONG_ANSWER Validation - Teacher: " + teacherAnswer + ", Student: " + studentAnswer + ", Result: " + ollamaResponse);
                } else {
                    boolean result = answerEvaluationService.validateSimpleAnswer(teacherAnswer, studentAnswer);
                    ollamaResponse = result ? "true" : "false";
                    System.out.println("SIMPLE_ANSWER Validation - Teacher: " + teacherAnswer + ", Student: " + studentAnswer + ", Result: " + ollamaResponse);
                }


                return Map.<String, Object>of(
                        "question", question.getQuestionText(),
                        "studentAnswer", studentAnswer,
                        "teacherAnswer", teacherAnswer,
                        "ollamaResponse", ollamaResponse
                );
            }).toList();

            return ResponseEntity.ok(evaluationDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("message", "Error fetching evaluation details: " + e.getMessage())));
        }
    }

}
