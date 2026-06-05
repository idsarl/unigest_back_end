package gestion.scolaire.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.http.SessionCreationPolicy;

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
                AuthenticationManagerBuilder authenticationManagerBuilder = http
                                .getSharedObject(AuthenticationManagerBuilder.class);
                authenticationManagerBuilder.userDetailsService(customUserDetailsService)
                                .passwordEncoder(passwordEncoder);
                return authenticationManagerBuilder.build();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .cors(cors -> {
                                })
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/admins/**",
                                                                "/api/seances/**",
                                                                "/api/appels/**",
                                                                "/api/affectations/**",
                                                                "/api/inscriptions/**",
                                                                "/api/notes/**",
                                                                "/api/etudiants/**",
                                                                "/api/annee-scolaire/**",
                                                                "/api/v3/api-docs/**", // Correspond à
                                                               "/api/medias/*/fichier",                     // springdoc.api-docs.path
                                                                "/api/swagger-ui/**", // Correspond au dossier des
                                                                                      // ressources UI
                                                                "/api/swagger-ui.html", // Correspond à
                                                                                        // springdoc.swagger-ui.path
                                                                "/swagger-ui/**", // Sécurité au cas où le mapping par
                                                                                  // défaut persiste
                                                                "/v3/api-docs/**",
                                                                "/ws/**")
                                                .permitAll()

                                                .anyRequest().authenticated())
                                .addFilterBefore(new JwtAuthenticationFilter(customUserDetailsService, jwtUtil),
                                                UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Autorise TOUTES les origines (http, https, local, online)
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(
                                Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
                configuration.setAllowCredentials(true); // Indispensable pour envoyer le Header Authorization
                configuration.setExposedHeaders(Arrays.asList("Authorization")); // Permet au front de lire le token si
                                                                                 // besoin

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

}
