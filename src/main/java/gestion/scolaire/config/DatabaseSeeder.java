package gestion.scolaire.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gestion.scolaire.model.*;
import gestion.scolaire.repository.*;
import gestion.scolaire.service.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final ParentRepository parentRepository;
    private final EnseignantRepository enseignantRepository;
    private final EtudiantRepository etudiantRepository;
    private final EtudiantService etudiantService;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final FiliereRepository filiereRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final InscriptionRepository inscriptionRepository;
    private final AffectationRepository affectationRepository;
    private final NoteRepository noteRepository;
    private final SeanceRepository seanceRepository;
    private final AppelRepository appelRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (utilisateurRepository.count() > 0) {
            log.info("La base de données contient déjà des données. Seeding annulé.");
            return;
        }

        log.info("Début du seeding de la base de données...");

        // 1. Créer une année scolaire active
        AnneeScolaire annee = new AnneeScolaire();
        annee.setLibelle("2025-2026");
        annee.setDateDebut(LocalDate.of(2025, 9, 1));
        annee.setDateFin(LocalDate.of(2026, 6, 30));
        annee.setDateAjout(LocalDate.now());
        annee.setActive(true);
        annee = anneeScolaireRepository.save(annee);

        // 2. Créer une filière
        Filiere filiere = new Filiere();
        filiere.setNom("Générale");
        filiere.setActif(true);
        filiere = filiereRepository.save(filiere);

        // 3. Créer des classes
        Classe classe3 = new Classe();
        classe3.setNom("3ème");
        classe3.setFiliere(filiere);
        classe3 = classeRepository.save(classe3);

        Classe classe5 = new Classe();
        classe5.setNom("5ème");
        classe5.setFiliere(filiere);
        classe5 = classeRepository.save(classe5);

        // 4. Créer des matières
        Matiere maths = new Matiere();
        maths.setNom("Mathématiques");
        maths = matiereRepository.save(maths);

        Matiere francais = new Matiere();
        francais.setNom("Français");
        francais = matiereRepository.save(francais);

        Matiere physique = new Matiere();
        physique.setNom("Physique-Chimie");
        physique = matiereRepository.save(physique);

        Matiere histoire = new Matiere();
        histoire.setNom("Histoire-Géo");
        histoire = matiereRepository.save(histoire);

        // 5. Créer des enseignants
        Enseignant profMaths = new Enseignant();
        profMaths.setNom("Dupont");
        profMaths.setPrenom("Jean");
        profMaths.setEmail("jean.dupont@ecole.fr");
        profMaths.setTelephone("77123456");
        profMaths.setPassword(passwordEncoder.encode("password"));
        profMaths.setRole(Role.ENSEIGNANT);
        profMaths.setSpecialite("Mathématiques");
        profMaths.setAdresse("Bamako, Mali");
        profMaths = enseignantRepository.save(profMaths);

        Enseignant profFrancais = new Enseignant();
        profFrancais.setNom("Martin");
        profFrancais.setPrenom("Marie");
        profFrancais.setEmail("marie.martin@ecole.fr");
        profFrancais.setTelephone("77123457");
        profFrancais.setPassword(passwordEncoder.encode("password"));
        profFrancais.setRole(Role.ENSEIGNANT);
        profFrancais.setSpecialite("Français");
        profFrancais.setAdresse("Bamako, Mali");
        profFrancais = enseignantRepository.save(profFrancais);

        // 6. Créer un parent
        Parent parent = new Parent();
        parent.setNom("Diallo");
        parent.setPrenom("Moussa");
        parent.setEmail("parent@unigest.com");
        parent.setTelephone("76432109");
        parent.setPassword(passwordEncoder.encode("password"));
        parent.setRole(Role.PARENT);
        parent.setAdresse("Bamako, Mali");
        parent = parentRepository.save(parent);

        // 6b. Créer un admin
        Admin admin = new Admin();
        admin.setNom("Admin");
        admin.setPrenom("Super");
        admin.setEmail("admin@unigest.com");
        admin.setTelephone("76000000");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole(Role.ADMIN);
        admin = (Admin) utilisateurRepository.save(admin);

        // 7. Créer des étudiants (enfants)
        Etudiant aboubacra = new Etudiant();
        aboubacra.setNom("Diallo");
        aboubacra.setPrenom("Mariam");
        aboubacra.setEmail("mariam.diallo@ecole.fr");
        aboubacra.setTelephone("76432110");
        aboubacra.setPassword("password");
        aboubacra.setDateNaissance(LocalDate.of(2012, 3, 15));
        aboubacra.setParent(parent);
        aboubacra = etudiantService.creerEtudiant(aboubacra);

        Etudiant aicha = new Etudiant();
        aicha.setNom("Diallo");
        aicha.setPrenom("Sékou");
        aicha.setEmail("sekou.diallo@ecole.fr");
        aicha.setTelephone("76432111");
        aicha.setPassword("password");
        aicha.setDateNaissance(LocalDate.of(2014, 9, 22));
        aicha.setParent(parent);
        aicha = etudiantService.creerEtudiant(aicha);

        // 8. Inscrire les étudiants dans leurs classes respectives
        Inscription inscAboubacra = new Inscription();
        inscAboubacra.setEtudiant(aboubacra);
        inscAboubacra.setClasse(classe3);
        inscAboubacra.setAnneeScolaire(annee);
        inscAboubacra.setDateInscription(LocalDate.now());
        inscAboubacra.setStatut("INSCRIT");
        inscriptionRepository.save(inscAboubacra);

        Inscription inscAicha = new Inscription();
        inscAicha.setEtudiant(aicha);
        inscAicha.setClasse(classe5);
        inscAicha.setAnneeScolaire(annee);
        inscAicha.setDateInscription(LocalDate.now());
        inscAicha.setStatut("INSCRIT");
        inscriptionRepository.save(inscAicha);

        // 9. Créer des affectations d'enseignants
        Affectation affMaths3 = new Affectation();
        affMaths3.setEnseignant(profMaths);
        affMaths3.setClasse(classe3);
        affMaths3.setAnneeScolaire(annee);
        affMaths3.setMatieres(Arrays.asList(maths));
        affMaths3.setDateCreation(LocalDate.now());
        affMaths3 = affectationRepository.save(affMaths3);

        Affectation affFrancais3 = new Affectation();
        affFrancais3.setEnseignant(profFrancais);
        affFrancais3.setClasse(classe3);
        affFrancais3.setAnneeScolaire(annee);
        affFrancais3.setMatieres(Arrays.asList(francais));
        affFrancais3.setDateCreation(LocalDate.now());
        affFrancais3 = affectationRepository.save(affFrancais3);

        // 10. Créer des séances de cours (Emploi du temps)
        // Lundi Mathématiques 08:00 - 10:00 (Classe 3ème)
        Seance seance1 = new Seance();
        seance1.setDate(LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY)));
        seance1.setHeureDebut(LocalTime.of(8, 0));
        seance1.setHeureFin(LocalTime.of(10, 0));
        seance1.setStatut(StatutSeance.TERMINEE);
        seance1.setMatiere("Mathématiques");
        seance1.setAffectation(affMaths3);
        seance1.setAnneeScolaire(annee);
        seance1.setDateCreation(LocalDateTime.now());
        seance1 = seanceRepository.save(seance1);

        // Lundi Français 10:15 - 12:15
        Seance seance2 = new Seance();
        seance2.setDate(LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY)));
        seance2.setHeureDebut(LocalTime.of(10, 15));
        seance2.setHeureFin(LocalTime.of(12, 15));
        seance2.setStatut(StatutSeance.TERMINEE);
        seance2.setMatiere("Français");
        seance2.setAffectation(affFrancais3);
        seance2.setAnneeScolaire(annee);
        seance2.setDateCreation(LocalDateTime.now());
        seance2 = seanceRepository.save(seance2);

        // Mardi Mathématiques 08:00 - 10:00
        Seance seance3 = new Seance();
        seance3.setDate(LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.TUESDAY)));
        seance3.setHeureDebut(LocalTime.of(8, 0));
        seance3.setHeureFin(LocalTime.of(10, 0));
        seance3.setStatut(StatutSeance.PLANIFIEE);
        seance3.setMatiere("Mathématiques");
        seance3.setAffectation(affMaths3);
        seance3.setAnneeScolaire(annee);
        seance3.setDateCreation(LocalDateTime.now());
        seance3 = seanceRepository.save(seance3);

        // 11. Notes d'évaluations
        // Aboubacra
        Note n1 = new Note();
        n1.setEtudiant(aboubacra);
        n1.setAffectation(affMaths3);
        n1.setAnneeScolaire(annee);
        n1.setMatiere(maths);
        n1.setValeur(15.5);
        n1.setCoefficient(2.0);
        n1.setType(TypeNote.DEVOIR);
        n1.setPeriode(1);
        n1.setTypePeriode(TypePeriode.TRIMESTRE);
        n1.setDateEvaluation(LocalDate.now().minusDays(10));
        noteRepository.save(n1);

        Note n2 = new Note();
        n2.setEtudiant(aboubacra);
        n2.setAffectation(affFrancais3);
        n2.setAnneeScolaire(annee);
        n2.setMatiere(francais);
        n2.setValeur(14.0);
        n2.setCoefficient(2.0);
        n2.setType(TypeNote.DEVOIR);
        n2.setPeriode(1);
        n2.setTypePeriode(TypePeriode.TRIMESTRE);
        n2.setDateEvaluation(LocalDate.now().minusDays(8));
        noteRepository.save(n2);

        Note n3 = new Note();
        n3.setEtudiant(aboubacra);
        n3.setAffectation(affMaths3);
        n3.setAnneeScolaire(annee);
        n3.setMatiere(maths);
        n3.setValeur(16.5);
        n3.setCoefficient(1.0);
        n3.setType(TypeNote.INTERROGATION);
        n3.setPeriode(1);
        n3.setTypePeriode(TypePeriode.TRIMESTRE);
        n3.setDateEvaluation(LocalDate.now().minusDays(5));
        noteRepository.save(n3);

        // Aicha (Mêmes matières, mais dans sa classe de 5ème - on simule quelques notes directement)
        // Pour Aicha, on peut utiliser les mêmes affectations simplifiées ou d'autres
        Note n4 = new Note();
        n4.setEtudiant(aicha);
        n4.setAffectation(affMaths3); // affectation simplifiée pour le seeder
        n4.setAnneeScolaire(annee);
        n4.setMatiere(maths);
        n4.setValeur(12.8);
        n4.setCoefficient(2.0);
        n4.setType(TypeNote.DEVOIR);
        n4.setPeriode(1);
        n4.setTypePeriode(TypePeriode.TRIMESTRE);
        n4.setDateEvaluation(LocalDate.now().minusDays(7));
        noteRepository.save(n4);

        // 12. Appels et Absences
        Appel a1 = new Appel();
        a1.setSeance(seance1);
        a1.setEtudiant(aboubacra);
        a1.setStatut(StatutPresence.ABSENT);
        a1.setMinutesRetard(0);
        a1.setMotif("Maladie");
        a1.setJustifie(true);
        a1.setDateJustification(LocalDateTime.now().minusDays(1));
        appelRepository.save(a1);

        Appel a2 = new Appel();
        a2.setSeance(seance2);
        a2.setEtudiant(aboubacra);
        a2.setStatut(StatutPresence.RETARD);
        a2.setMinutesRetard(15);
        a2.setMotif("Problème de transport");
        a2.setJustifie(true);
        a2.setDateJustification(LocalDateTime.now().minusDays(1));
        appelRepository.save(a2);

        // Une absence non justifiée pour Aicha
        Appel a3 = new Appel();
        a3.setSeance(seance1);
        a3.setEtudiant(aicha);
        a3.setStatut(StatutPresence.ABSENT);
        a3.setMinutesRetard(0);
        a3.setJustifie(false);
        appelRepository.save(a3);

        log.info("Seeding de la base de données terminé avec succès !");
        log.info("Identifiants Parent de test: parent@unigest.com / password ou Téléphone: 76432109 / password");
    }
}