package com.dcsic.qrcode.dao.repository;

import com.dcsic.qrcode.model.entities.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThemeRepo extends JpaRepository<Theme, Long> {
    List<Theme> findByEventId(Long eventId);
}
