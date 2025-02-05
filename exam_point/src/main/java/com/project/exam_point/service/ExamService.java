package com.project.exam_point.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.project.exam_point.dto.ExamSubmissionRequest;
import com.project.exam_point.model.Exam;
import com.project.exam_point.model.ExamSubmission;
import com.project.exam_point.model.Question;
import com.project.exam_point.repository.ExamRepository;
import com.project.exam_point.repository.ExamSubmissionRepository;
import com.project.exam_point.repository.QuestionRepository;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final QuestionRepository questionRepository;

    public ExamService(ExamRepository examRepository, 
                       ExamSubmissionRepository examSubmissionRepository, 
                       QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.examSubmissionRepository = examSubmissionRepository;
        this.questionRepository = questionRepository;
    }

    public Exam saveExam(Exam exam) {
        exam.getQuestions().forEach(question -> question.setExam(exam));
        return examRepository.save(exam);
    }

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public void deleteExamByTitle(String title) {
        Exam exam = examRepository.findByTitle(title)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));
        examRepository.delete(exam);
    }

    public Exam deleteQuestionFromExam(Long examId, Long questionId) {
        Exam exam = findById(examId);
        if (exam != null) {
            Question questionToRemove = exam.getQuestions().stream()
                .filter(question -> question.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            exam.getQuestions().remove(questionToRemove);
            return saveExam(exam);
        }
        throw new IllegalArgumentException("Exam not found");
    }

    public Exam updateExamByTitle(String title, Exam updatedExam) {
        Exam exam = examRepository.findByTitle(title)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        exam.setTitle(updatedExam.getTitle());
        exam.setQuestions(updatedExam.getQuestions());
        return saveExam(exam);
    }

    public Optional<Exam> getExamByTitle(String title) {
        return examRepository.findByTitle(title);
    }

    @Transactional(readOnly = true)
    public Exam findById(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));
        exam.getQuestions().size(); // Initialize lazy-loaded questions
        return exam;
    }

    public Exam updateExam(Long examId, Exam exam) {
        Optional<Exam> existingExamOpt = examRepository.findById(examId);
        if (existingExamOpt.isPresent()) {
            Exam existingExam = existingExamOpt.get();
            existingExam.getQuestions().clear();
            existingExam.getQuestions().addAll(exam.getQuestions());

            for (Question q : existingExam.getQuestions()) {
                q.setExam(existingExam);
            }

            return examRepository.save(existingExam);
        } else {
            throw new RuntimeException("Exam not found");
        }
    }

    public Exam addQuestionToExam(Long examId, Question question) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new IllegalArgumentException("Exam not found"));
        question.setExam(exam); // Associate the question with the exam
        exam.getQuestions().add(question); // Add the question to the exam's questions list
        return examRepository.save(exam); // Save the updated exam
    }

    public Exam updateQuestionInExam(Long examId, Long questionId, Question question) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new IllegalArgumentException("Exam not found"));
        Question existingQuestion = exam.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        existingQuestion.setQuestionText(question.getQuestionText());
        existingQuestion.setQuestionType(question.getQuestionType());
        existingQuestion.setOptions(question.getOptions());
        existingQuestion.setAnswer(question.getAnswer());

        return examRepository.save(exam);
    }

    // Convert ExamSubmissionRequest to ExamSubmission entities
    public List<ExamSubmission> convertToExamSubmissions(ExamSubmissionRequest request) {
        // Fetch the Exam entity based on the provided examId
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        // Convert the answers from the request into ExamSubmission entities
        return request.getAnswers().stream().map(answerDto -> {
            Question question = questionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            ExamSubmission submission = new ExamSubmission();
            submission.setExam(exam);
            submission.setQuestion(question);
            submission.setAnswer(answerDto.getAnswer());
            return submission;
        }).collect(Collectors.toList());
    }

    // Process the answers and optionally save them to the database
    public void processAnswers(ExamSubmissionRequest request) {
        System.out.println("Processing answers for exam ID: " + request.getExamId());
        request.getAnswers().forEach(answer -> {
            System.out.println("Question ID: " + answer.getQuestionId() + ", Answer: " + answer.getAnswer());
        });

        // Convert to ExamSubmission entities and save them
        List<ExamSubmission> submissions = convertToExamSubmissions(request);
        examSubmissionRepository.saveAll(submissions); // Save to DB
    }
}
