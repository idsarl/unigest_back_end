package gestion.scolaire;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import gestion.scolaire.dto.SeanceDTO;
import gestion.scolaire.dto.UserView;
import gestion.scolaire.model.*;
import gestion.scolaire.repository.*;
import gestion.scolaire.service.SeanceService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.properties.hibernate.hbm2ddl.auto=none"
})
class ScolaireApplicationTests {

    @Autowired
    private UtilisateurRepository utilisateurRepository;


    @Autowired
    private SeanceService seanceService;

    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private FiliereRepository filiereRepository;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private AffectationRepository affectationRepository;

    @Autowired
    private AnneeScolaireRepository anneeScolaireRepository;

    @Autowired
    private MatiereRepository matiereRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void testUserViewProjection() {
        // Avoid findAll() on the abstract Utilisateur — Hibernate 6 JOINED inheritance triggers
        // an AssertionError in EntityInitializerImpl when resolving the CASE-based discriminator.
        String email = jdbcTemplate.queryForObject("SELECT email FROM utilisateur LIMIT 1", String.class);
        String telephone = jdbcTemplate.queryForObject("SELECT telephone FROM utilisateur LIMIT 1", String.class);
        System.out.println("=== TESTING USERVIEW PROJECTION ===");
        System.out.println("Email from DB: " + email);

        UserView view = utilisateurRepository.findProjectedByEmailOrTelephone(email, telephone).orElseThrow();
        System.out.println("Projection ID: " + view.getIdUser());
        System.out.println("Projection Email: " + view.getEmail());
        System.out.println("Projection Username: " + view.getUsername());

        assertNotNull(view.getIdUser());
        assertNotNull(view.getEmail());

        System.out.println("MY_BCRYPT_HASH: " + new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("123456"));
    }





}

