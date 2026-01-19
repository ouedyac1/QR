package com.dcsic.qrcode.dao.repository;

import com.dcsic.qrcode.model.entities.UserQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserQuestionRepo extends JpaRepository<UserQuestion, Long> {
    List<UserQuestion> findByEventIdOrderBySubmittedAtDesc(Long eventId);
    List<UserQuestion> findAllByOrderBySubmittedAtDesc();
}