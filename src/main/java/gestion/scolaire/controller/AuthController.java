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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword())
        );

        Utilisateur user = userRepository.findByEmailOrTelephone(
                loginRequest.getLogin(), loginRequest.getLogin()
        ).orElseThrow();

        Map<String, Object> authData = new HashMap<>();
        authData.put("token", jwtUtil.generateToken(user));
        authData.put("type", "Bearer");
        
        // Ajout des infos utilisateur
        authData.put("nom", user.getNom());
        authData.put("prenom", user.getPrenom());
        authData.put("role", user.getRole());
        authData.put("id", user.getId());

        return ResponseEntity.ok(authData);
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
