package com.dcsic.qrcode.dao.repository;

import com.dcsic.qrcode.model.entities.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRepo extends JpaRepository<Institution, Long> {
    Optional<Institution> findByCode(String code);

    boolean existsByCode(String code);
}
