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
    "spring.jpa.properties.hibernate.hbm2ddl.auto=none",
    "spring.sql.init.mode=never"
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
        Utilisateur user = utilisateurRepository.findAll().get(0); // get a user
        System.out.println("=== TESTING USERVIEW PROJECTION ===");
        System.out.println("User ID in database: " + user.getId() + ", Email: " + user.getEmail());

        UserView view = utilisateurRepository.findProjectedByEmailOrTelephone(user.getEmail(), user.getEmail()).orElseThrow();
        System.out.println("Projection ID: " + view.getIdUser());
        System.out.println("Projection Email: " + view.getEmail());
        System.out.println("Projection Username: " + view.getUsername());
        
        System.out.println("MY_BCRYPT_HASH: " + new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("123456"));
    }





}

