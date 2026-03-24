package com.dcsic.qrcode.dao.repository;

import com.dcsic.qrcode.model.entities.Institution;
import com.dcsic.qrcode.model.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRepo extends JpaRepository<Person, Long> {
    Optional<Person> findByUniqueCode(String uniqueCode);
    List<Person> findByInstitutionCodeIgnoreCase(String trim);

    @Query("SELECT DISTINCT p.institution.code FROM Person p")
    List<String> findDistinctInstitutionCodes();

    List<Person> findByInstitution(Institution institution);

    @Query("SELECT p FROM Person p JOIN FETCH p.institution WHERE p.uniqueCode = :code")
    Optional<Person> findByUniqueCodeWithInstitution(@Param("code") String code);

    List<Person> findByInstitutionCode(String institutionCode);
}