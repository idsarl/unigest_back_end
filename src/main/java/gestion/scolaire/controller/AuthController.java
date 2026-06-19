package gestion.scolaire.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gestion.scolaire.config.LoginRequest;
import gestion.scolaire.dto.UserView;
import gestion.scolaire.model.Utilisateur;
import gestion.scolaire.repository.UtilisateurRepository;
import gestion.scolaire.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private UtilisateurRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    
//     @PostMapping("/login")
//     public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

//         Authentication authentication =
//                 authenticationManager.authenticate(
//                         new UsernamePasswordAuthenticationToken(
//                                 loginRequest.getLogin(),
//                                 loginRequest.getPassword()
//                         )
//                 );

//         Utilisateur user = userRepository
//                 .findByEmailOrTelephone(
//                         loginRequest.getLogin(),
//                         loginRequest.getLogin()
//                 )
//                 .orElseThrow();

//         Map<String, Object> authData = new HashMap<>();
//         authData.put("token", jwtUtil.generateToken(user));
//         authData.put("type", "Bearer");

//         return ResponseEntity.ok(authData);
//     }

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
            try {
                log.info("Tentative de connexion pour: {}", loginRequest.getLogin());
                
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword())
                );

                Utilisateur user = userRepository.findByEmailOrTelephone(
                        loginRequest.getLogin(), loginRequest.getLogin()
                ).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

                log.info("Utilisateur trouvé: {}", user.getEmail());

                Map<String, Object> authData = new HashMap<>();
                authData.put("token", jwtUtil.generateToken(user));
                authData.put("type", "Bearer");
                
                // Ajout des infos utilisateur
                authData.put("id", user.getId());
                authData.put("nom", user.getNom());
                authData.put("prenom", user.getPrenom());
                authData.put("role", user.getRole());

                log.info("Connexion réussie pour: {}", user.getEmail());
                return ResponseEntity.ok(authData);
            } catch (AuthenticationException e) {
                log.error("Échec de l'authentification: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Identifiants incorrects"));
            } catch (Exception e) {
                log.error("Erreur lors de la connexion: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            }
}

   @GetMapping("/me")
    public ResponseEntity<UserView> getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String login = authentication.getName();

        return userRepository
                .findProjectedByEmailOrTelephone(login, login)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }



}
