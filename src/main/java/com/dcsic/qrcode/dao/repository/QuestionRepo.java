package com.dcsic.qrcode.dao.repository;

import com.dcsic.qrcode.model.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepo extends JpaRepository<Question, Long> {
    List<Question> findByThemeId(Long themeId);
}
