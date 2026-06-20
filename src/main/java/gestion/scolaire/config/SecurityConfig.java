package gestion.scolaire.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import gestion.scolaire.util.CustomUserDetailService;
import gestion.scolaire.util.JwtUtil;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailService customUserDetailsService;
    private final JwtUtil jwtUtil;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder);
        return authManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public: login + Swagger
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/api/v3/api-docs/**",
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/medias/*/fichier"
                        ).permitAll()

                        // ADMIN only: gestion des comptes admin (n'est plus public)
                        .requestMatchers("/api/admins/**").hasAuthority("ADMIN")

                        // ADMIN only: paramètres école et seuils d'appréciation
                        .requestMatchers("/api/parametres/**").hasAuthority("ADMIN")

                        // ADMIN + COMPTABLE: finance
                        .requestMatchers("/api/paiements/**").hasAnyAuthority("ADMIN", "COMPTABLE")
                        .requestMatchers("/api/depenses/**").hasAnyAuthority("ADMIN", "COMPTABLE")
                        .requestMatchers("/api/categorie-depense/**").hasAnyAuthority("ADMIN", "COMPTABLE")
                        .requestMatchers("/api/bulletins/**").hasAnyAuthority("ADMIN", "COMPTABLE")

                        // ADMIN only: mutations sensibles sur les donnees de base
                        .requestMatchers(HttpMethod.POST, "/api/enseignants/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/enseignants/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/enseignants/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/affectations").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/affectations/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/affectations/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/classes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/classes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/classes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/matieres/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/matieres/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/matieres/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/inscriptions/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/etudiants/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/parents/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/annees-scolaires/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/filieres/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/niveaux/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/emplois-du-temps/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/emplois-du-temps/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/emplois-du-temps/**").hasAuthority("ADMIN")

                        // ADMIN + ENSEIGNANT: tout le reste authentifie
                        // (inclut GET affectations, GET classes, GET emplois, seances, appels, notes)
                        .anyRequest().hasAnyAuthority("ADMIN", "ENSEIGNANT", "COMPTABLE")
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(customUserDetailsService, jwtUtil),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
        );
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}