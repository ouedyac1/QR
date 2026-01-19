package com.dcsic.qrcode.dao.repository;

import com.dcsic.qrcode.model.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepo extends JpaRepository<Event, Long> {
    Optional<Event> findBySlug(String slug);
}
