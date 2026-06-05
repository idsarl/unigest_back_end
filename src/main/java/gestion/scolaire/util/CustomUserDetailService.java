package gestion.scolaire.util;

import java.util.Collections;

import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import gestion.scolaire.model.Utilisateur;
import gestion.scolaire.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    
    @Autowired
    UtilisateurRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login)
        throws UsernameNotFoundException {

    Utilisateur user = userRepository
            .findByEmailOrTelephone(login, login)
            .orElseThrow(() ->
                    new UsernameNotFoundException("Utilisateur introuvable")
            );

    // 🔒 Sécurisation ABSOLUE
    String principal = null;

    if (user.getEmail() != null && !user.getEmail().isBlank()) {
        principal = user.getEmail();
    } else if (user.getTelephone() != null && !user.getTelephone().isBlank()) {
        principal = user.getTelephone();
    }

    if (principal == null) {
        throw new UsernameNotFoundException(
                "Utilisateur invalide : email et téléphone absents"
        );
    }

    if (user.getPassword() == null || user.getPassword().isBlank()) {
        throw new UsernameNotFoundException(
                "Utilisateur invalide : mot de passe manquant"
        );
    }

    return new org.springframework.security.core.userdetails.User(
            principal,
            user.getPassword(),
            Collections.singletonList(
                    new SimpleGrantedAuthority(user.getRole().name())
            )
    );
}}
