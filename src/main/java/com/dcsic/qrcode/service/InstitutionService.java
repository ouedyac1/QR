package com.dcsic.qrcode.service;

import com.dcsic.qrcode.model.entities.Institution;
import com.dcsic.qrcode.dao.repository.InstitutionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InstitutionService {

    private final InstitutionRepo institutionRepository;

    public InstitutionService(InstitutionRepo institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    public List<Institution> findAll() {
        return institutionRepository.findAll();
    }

    public boolean existsByCode(String code) {
        return institutionRepository.existsByCode(code);
    }

    @Transactional
    public void create(String code, String nom, String email) {
        // @AllArgsConstructor génère : Institution(Long id, String code, String title, List<User> users)
        // null pour id (auto-généré par JPA), new ArrayList<>() pour la liste users
        institutionRepository.save(new Institution(null, code.toUpperCase().trim(), nom.trim(), email.trim(), new ArrayList<>()));
    }

    @Transactional
    public void delete(Long id) {
        institutionRepository.deleteById(id);
    }

    @Transactional
    public void update(Long id, String code, String title, String email) {
        Institution inst = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution introuvable : " + id));
        inst.setCode(code.toUpperCase().trim());
        inst.setTitle(title.trim());
        institutionRepository.save(inst);
    }
}