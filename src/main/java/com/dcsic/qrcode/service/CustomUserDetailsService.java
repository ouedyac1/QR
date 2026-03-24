package com.dcsic.qrcode.service;

import com.dcsic.qrcode.dao.repository.UserRepository;
import com.dcsic.qrcode.model.entities.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Récupération de l'utilisateur depuis la DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable avec le username : " + username));

        // 2. Mapping vers l'objet UserDetails de Spring Security
        // Note : Si vous n'avez pas encore de champ 'role' dans votre entité,
        // on en durcit un par défaut ou on utilise une logique basée sur l'institution.

        String userRole = (user.getInstitution() == null) ? "ADMIN" : "USER";

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole)))
                .disabled(false) // ou user.isEnabled() si vous ajoutez le champ
                .build();
    }
}