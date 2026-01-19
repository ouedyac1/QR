package com.dcsic.qrcode.dao.repository;

import com.dcsic.qrcode.model.entities.Response;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponseRepo extends JpaRepository<Response, Long> {
    List<Response> findByQuestionId(Long questionId);
}
