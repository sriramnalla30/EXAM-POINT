package com.project.exam_point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.exam_point.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
