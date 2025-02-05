package com.project.exam_point.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.exam_point.model.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    Optional<Exam> findByTitle(String title);
    @EntityGraph(attributePaths = "questions")
    Optional<Exam> findById(Long id);
}
