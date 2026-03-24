package com.dcsic.qrcode.service;

import com.dcsic.qrcode.model.entities.Institution;
import com.dcsic.qrcode.model.entities.User;
import com.dcsic.qrcode.dao.repository.InstitutionRepo;
import com.dcsic.qrcode.dao.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository        userRepository;
    private final InstitutionRepo institutionRepository;
    private final PasswordEncoder       passwordEncoder;



    public void createUser(String username, String password, Long institutionId) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // N'oublie pas l'encodage !

        if (institutionId != null) {
            Institution inst = institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new RuntimeException("Institution non trouvée"));
            user.setInstitution(inst);
        } else {
            user.setInstitution(null); // C'est ici que se définit ton ADMIN global
        }

        userRepository.save(user);
    }

    public boolean exists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }






    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(String username) {
        userRepository.deleteByUsername(username);
    }

    @Transactional
    public void changePassword(String username, String newRawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + username));
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }



}