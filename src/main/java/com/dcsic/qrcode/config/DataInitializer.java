package com.dcsic.qrcode.config;

import com.dcsic.qrcode.service.InstitutionService;
import com.dcsic.qrcode.service.UserService;
import com.dcsic.qrcode.dao.repository.InstitutionRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@AllArgsConstructor
//@NoArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService           userService;
    private final InstitutionService    institutionService;
    private final InstitutionRepo institutionRepository;


    @Override
    public void run(String... args) {
        // 2. Créer l'admin sans institution
        if (!userService.exists("admin")) {
            userService.createUser("admin", "admin123", null);
            System.out.println("[DataInitializer] Admin créé.");
        }

    }

    private void createInstitutionIfAbsent(String code, String nom, String  email) {
        if (!institutionService.existsByCode(code)) {
            institutionService.create(code, nom, email);
            System.out.println("[DataInitializer] Institution créée : " + code);
        }
    }

    private void createUserIfAbsent(String username, String password, String institutionCode) {
        if (!userService.exists(username)) {
            Long institutionId = institutionRepository.findByCode(institutionCode)
                    .map(i -> i.getId())
                    .orElse(null);
            userService.createUser(username, password, institutionId);
            System.out.println("[DataInitializer] User créé : " + username + " -> " + institutionCode);
        }
    }
}