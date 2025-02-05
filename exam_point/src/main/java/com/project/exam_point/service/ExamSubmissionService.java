package com.project.exam_point.service;

import com.project.exam_point.model.ExamSubmission;
import com.project.exam_point.model.Question;
import com.project.exam_point.repository.ExamSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamSubmissionService {

    private final ExamSubmissionRepository examSubmissionRepository;
    private final AnswerEvaluationService answerEvaluationService;

    public ExamSubmissionService(ExamSubmissionRepository examSubmissionRepository, 
                                  AnswerEvaluationService answerEvaluationService) {
        this.examSubmissionRepository = examSubmissionRepository;
        this.answerEvaluationService = answerEvaluationService;
    }

    public void saveExamSubmissions(List<ExamSubmission> examSubmissions) {
        for (ExamSubmission submission : examSubmissions) {
            Question question = submission.getQuestion();
            if (question != null) {
                boolean isCorrect = validateAnswer(question, submission.getAnswer());
                submission.setCorrect(isCorrect); // Mark the submission as correct or incorrect
            } else {
                throw new IllegalArgumentException("Question not found for submission ID: " + submission.getId());
            }
        }
        examSubmissionRepository.saveAll(examSubmissions); // Save all updated submissions
    }


    private boolean validateAnswer(Question question, String studentAnswer) {
        if ("LONG_ANSWER".equalsIgnoreCase(question.getQuestionType())) {
            // Use Ollama for validating long answers
            return answerEvaluationService.validateLongAnswer(question.getAnswer(), studentAnswer);
        } else {
            // Validate simple answers (MCQs, fill-in-the-blanks)
            return answerEvaluationService.validateSimpleAnswer(question.getAnswer(), studentAnswer);
        }
    }
    public List<ExamSubmission> findSubmissionsByExamId(Long examId) {
        return examSubmissionRepository.findByExamId(examId);
    }
    
}
