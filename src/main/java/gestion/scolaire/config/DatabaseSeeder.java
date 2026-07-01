package gestion.scolaire.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gestion.scolaire.model.*;
import gestion.scolaire.dto.*;
import gestion.scolaire.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private AnneeScolaireRepository anneeScolaireRepository;

    @Autowired
    private NiveauRepository niveauRepository;

    @Autowired
    private FiliereRepository filiereRepository;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private MatiereRepository matiereRepository;

    @Autowired
    private ClasseMatiereRepository classeMatiereRepository;

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private AffectationRepository affectationRepository;

    @Autowired
    private EmploiDuTempsRepository emploiDuTempsRepository;

    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private AppelRepository appelRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private BulletinRepository bulletinRepository;

    @Autowired
    private CategorieDepenseRepository categorieDepenseRepository;

    @Autowired
    private DepenseRepository depenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (utilisateurRepository.count() > 0) {
            System.out.println("=== Base de données déjà alimentée (utilisateurs présents). Fin du seeding. ===");
            return;
        }

        System.out.println("=== Début du seeding de la base de données ===");

        String encodedPassword = passwordEncoder.encode("123456");

        // 1. Année Scolaire
        AnneeScolaire annee = new AnneeScolaire();
        annee.setLibelle("2025-2026");
        annee.setDateDebut(LocalDate.of(2025, 9, 1));
        annee.setDateFin(LocalDate.of(2026, 8, 31));
        annee.setDateAjout(LocalDate.now());
        annee.setActive(true);
        annee = anneeScolaireRepository.save(annee);

        // 2. Niveaux
        Niveau n1 = niveauRepository.save(new Niveau(null, "Licence 1"));
        Niveau n2 = niveauRepository.save(new Niveau(null, "Licence 2"));
        Niveau n3 = niveauRepository.save(new Niveau(null, "Licence 3"));

        // 3. Filières
        Filiere filiere = new Filiere();
        filiere.setNom("Génie Logiciel");
        filiere.setActif(true);
        filiere.setFraisInscription(50000);
        filiere.setFraisScolarite(500000);
        filiere.setNiveau(n3);
        filiere = filiereRepository.save(filiere);

        // 4. Classes
        Classe classe = new Classe();
        classe.setNom("L3-GL");
        classe.setFiliere(filiere);
        classe = classeRepository.save(classe);

        // 5. Matières
        Matiere mat1 = new Matiere();
        mat1.setNom("Algèbre");
        mat1.setStatut(true);
        mat1 = matiereRepository.save(mat1);

        Matiere mat2 = new Matiere();
        mat2.setNom("Conception Logicielle");
        mat2.setStatut(true);
        mat2 = matiereRepository.save(mat2);

        Matiere mat3 = new Matiere();
        mat3.setNom("Base de données");
        mat3.setStatut(true);
        mat3 = matiereRepository.save(mat3);

        // 6. ClasseMatiere (Coefficients)
        ClasseMatiere cm1 = new ClasseMatiere();
        cm1.setClasse(classe);
        cm1.setMatiere(mat1);
        cm1.setCoefficient(3.0);
        classeMatiereRepository.save(cm1);

        ClasseMatiere cm2 = new ClasseMatiere();
        cm2.setClasse(classe);
        cm2.setMatiere(mat2);
        cm2.setCoefficient(4.0);
        classeMatiereRepository.save(cm2);

        ClasseMatiere cm3 = new ClasseMatiere();
        cm3.setClasse(classe);
        cm3.setMatiere(mat3);
        cm3.setCoefficient(3.0);
        classeMatiereRepository.save(cm3);

        // 7. Utilisateurs
        // Admin
        Admin admin = new Admin();
        admin.setNom("Super");
        admin.setPrenom("Admin");
        admin.setEmail("admin@unigest.com");
        admin.setTelephone("770000001");
        admin.setPassword(encodedPassword);
        admin.setRole(Role.ADMIN);
        admin.setActif(true);
        admin = adminRepository.save(admin);

        // Enseignant
        Enseignant prof = new Enseignant();
        prof.setNom("Diop");
        prof.setPrenom("Abdou");
        prof.setEmail("prof@unigest.com");
        prof.setTelephone("770000002");
        prof.setPassword(encodedPassword);
        prof.setRole(Role.ENSEIGNANT);
        prof.setActif(true);
        prof.setSpecialite("Informatique");
        prof.setAdresse("Dakar Plateau");
        prof = enseignantRepository.save(prof);

        // Parent
        Parent parent = new Parent();
        parent.setNom("Ndiaye");
        parent.setPrenom("Modou");
        parent.setEmail("parent@unigest.com");
        parent.setTelephone("770000003");
        parent.setPassword(encodedPassword);
        parent.setRole(Role.PARENT);
        parent.setActif(true);
        parent.setAdresse("Dakar Fann");
        parent = parentRepository.save(parent);

        // Etudiants
        Etudiant etu1 = new Etudiant();
        etu1.setNom("Sall");
        etu1.setPrenom("Moussa");
        etu1.setEmail("student1@unigest.com");
        etu1.setTelephone("770000004");
        etu1.setPassword(encodedPassword);
        etu1.setRole(Role.ETUDIANT);
        etu1.setActif(true);
        etu1.setMatricule("MAT001");
        etu1.setDateNaissance(LocalDate.of(2002, 5, 10));
        etu1.setParent(parent);
        etu1 = etudiantRepository.save(etu1);

        Etudiant etu2 = new Etudiant();
        etu2.setNom("Gueye");
        etu2.setPrenom("Fatou");
        etu2.setEmail("student2@unigest.com");
        etu2.setTelephone("770000005");
        etu2.setPassword(encodedPassword);
        etu2.setRole(Role.ETUDIANT);
        etu2.setActif(true);
        etu2.setMatricule("MAT002");
        etu2.setDateNaissance(LocalDate.of(2003, 8, 15));
        etu2.setParent(parent);
        etu2 = etudiantRepository.save(etu2);

        // 8. Inscriptions
        Inscription ins1 = new Inscription();
        ins1.setEtudiant(etu1);
        ins1.setClasse(classe);
        ins1.setAnneeScolaire(annee);
        ins1.setDateInscription(LocalDate.now().minusDays(10));
        ins1.setMontantReduction(0.0);
        ins1.setMotifReduction("");
        ins1.setStatut("INSCRIT");
        ins1 = inscriptionRepository.save(ins1);

        Inscription ins2 = new Inscription();
        ins2.setEtudiant(etu2);
        ins2.setClasse(classe);
        ins2.setAnneeScolaire(annee);
        ins2.setDateInscription(LocalDate.now().minusDays(9));
        ins2.setMontantReduction(50000.0);
        ins2.setMotifReduction("Bourse d'excellence");
        ins2.setStatut("INSCRIT");
        ins2 = inscriptionRepository.save(ins2);

        // 9. Affectation (Prof - Classe - Matières)
        Affectation aff = new Affectation();
        aff.setEnseignant(prof);
        aff.setClasse(classe);
        aff.setAnneeScolaire(annee);
        aff.setMatieres(Arrays.asList(mat1, mat2, mat3));
        aff.setDateCreation(LocalDate.now().minusDays(15));
        aff.setDateModification(LocalDate.now().minusDays(15));
        aff = affectationRepository.save(aff);

        // 10. Emploi du Temps
        // Lundi : Algèbre 8h30 - 10h30
        EmploiDuTemps edt1 = new EmploiDuTemps();
        edt1.setClasse(classe);
        edt1.setEnseignant(prof);
        edt1.setMatiere(mat1);
        edt1.setJours(new HashSet<>(Arrays.asList(JourSemaine.LUNDI)));
        edt1.setHeureDebut(LocalTime.of(8, 30));
        edt1.setHeureFin(LocalTime.of(10, 30));
        edt1.setDateDebut(LocalDate.of(2025, 9, 1));
        edt1.setDateFin(LocalDate.of(2026, 8, 31));
        edt1.setCouleur("blue");
        edt1.setType(TypeEmploi.COURS);
        edt1.setActif(true);
        edt1.setPeriodicite(Periodicite.HEBDOMADAIRE);
        edt1.setDescription("Cours d'Algèbre linéaire");
        edt1.setAnneeScolaire(annee);
        emploiDuTempsRepository.save(edt1);

        // Mardi : Conception Logicielle 10h45 - 12h45
        EmploiDuTemps edt2 = new EmploiDuTemps();
        edt2.setClasse(classe);
        edt2.setEnseignant(prof);
        edt2.setMatiere(mat2);
        edt2.setJours(new HashSet<>(Arrays.asList(JourSemaine.MARDI)));
        edt2.setHeureDebut(LocalTime.of(10, 45));
        edt2.setHeureFin(LocalTime.of(12, 45));
        edt2.setDateDebut(LocalDate.of(2025, 9, 1));
        edt2.setDateFin(LocalDate.of(2026, 8, 31));
        edt2.setCouleur("orange");
        edt2.setType(TypeEmploi.COURS);
        edt2.setActif(true);
        edt2.setPeriodicite(Periodicite.HEBDOMADAIRE);
        edt2.setDescription("Cours de Conception UML et Design Patterns");
        edt2.setAnneeScolaire(annee);
        emploiDuTempsRepository.save(edt2);

        // Mercredi : Base de données 14h00 - 16h00
        EmploiDuTemps edt3 = new EmploiDuTemps();
        edt3.setClasse(classe);
        edt3.setEnseignant(prof);
        edt3.setMatiere(mat3);
        edt3.setJours(new HashSet<>(Arrays.asList(JourSemaine.MERCREDI)));
        edt3.setHeureDebut(LocalTime.of(14, 0));
        edt3.setHeureFin(LocalTime.of(16, 0));
        edt3.setDateDebut(LocalDate.of(2025, 9, 1));
        edt3.setDateFin(LocalDate.of(2026, 8, 31));
        edt3.setCouleur("green");
        edt3.setType(TypeEmploi.COURS);
        edt3.setActif(true);
        edt3.setPeriodicite(Periodicite.HEBDOMADAIRE);
        edt3.setDescription("Cours de Base de Données Relationnelles et SQL");
        edt3.setAnneeScolaire(annee);
        emploiDuTempsRepository.save(edt3);

        // 11. Séances
        // Séance 1 : Passée (Hier) - Algèbre
        Seance seance1 = new Seance();
        seance1.setDate(LocalDate.now().minusDays(1));
        seance1.setHeureDebut(LocalTime.of(8, 30));
        seance1.setHeureFin(LocalTime.of(10, 30));
        seance1.setStatut(StatutSeance.TERMINEE);
        seance1.setDateCreation(LocalDateTime.now().minusDays(1));
        seance1.setDateModification(LocalDateTime.now().minusDays(1));
        seance1.setMatiere(mat1.getNom());
        seance1.setAffectation(aff);
        seance1.setAnneeScolaire(annee);
        seance1 = seanceRepository.save(seance1);

        // Séance 2 : Planifiée (Aujourd'hui) - Conception Logicielle
        Seance seance2 = new Seance();
        seance2.setDate(LocalDate.now());
        seance2.setHeureDebut(LocalTime.of(10, 45));
        seance2.setHeureFin(LocalTime.of(12, 45));
        seance2.setStatut(StatutSeance.PLANIFIEE);
        seance2.setDateCreation(LocalDateTime.now());
        seance2.setDateModification(LocalDateTime.now());
        seance2.setMatiere(mat2.getNom());
        seance2.setAffectation(aff);
        seance2.setAnneeScolaire(annee);
        seance2 = seanceRepository.save(seance2);

        // 12. Appels (Présence sur la séance passée)
        Appel ap1 = new Appel();
        ap1.setSeance(seance1);
        ap1.setEtudiant(etu1);
        ap1.setStatut(StatutPresence.PRESENT);
        ap1.setMinutesRetard(0);
        ap1.setJustifie(false);
        appelRepository.save(ap1);

        Appel ap2 = new Appel();
        ap2.setSeance(seance1);
        ap2.setEtudiant(etu2);
        ap2.setStatut(StatutPresence.ABSENT);
        ap2.setMinutesRetard(0);
        ap2.setJustifie(false);
        appelRepository.save(ap2);

        // 13. Notes (Hier sur Algèbre)
        Note note1 = new Note();
        note1.setEtudiant(etu1);
        note1.setAffectation(aff);
        note1.setAnneeScolaire(annee);
        note1.setMatiere(mat1);
        note1.setValeur(15.5);
        note1.setCoefficient(1.0);
        note1.setType(TypeNote.DEVOIR);
        note1.setPeriode(1);
        note1.setTypePeriode(TypePeriode.SEMESTRE);
        note1.setDateEvaluation(LocalDate.now().minusDays(1));
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setEtudiant(etu2);
        note2.setAffectation(aff);
        note2.setAnneeScolaire(annee);
        note2.setMatiere(mat1);
        note2.setValeur(11.0);
        note2.setCoefficient(1.0);
        note2.setType(TypeNote.DEVOIR);
        note2.setPeriode(1);
        note2.setTypePeriode(TypePeriode.SEMESTRE);
        note2.setDateEvaluation(LocalDate.now().minusDays(1));
        noteRepository.save(note2);

        // 14. Paiements (Frais d'inscription de Sall Moussa)
        Paiement pai = new Paiement();
        pai.setInscription(ins1);
        pai.setMontant(50000.0);
        pai.setDatePaiement(LocalDate.now().minusDays(5));
        pai.setModePaiement(ModePaiement.WAVE);
        pai.setReference("TXN984719842");
        paiementRepository.save(pai);

        // 15. Messages
        Message msg1 = new Message();
        msg1.setExpediteur(prof);
        msg1.setDestinataire(parent);
        msg1.setContenu("Bonjour, je vous informe que le cours d'Algèbre de lundi s'est bien déroulé.");
        msg1.setDateEnvoi(LocalDateTime.now().minusDays(2));
        msg1.setLu(true);
        messageRepository.save(msg1);

        Message msg2 = new Message();
        msg2.setExpediteur(parent);
        msg2.setDestinataire(prof);
        msg2.setContenu("Merci beaucoup pour l'information, Monsieur.");
        msg2.setDateEnvoi(LocalDateTime.now().minusDays(1));
        msg2.setLu(false);
        messageRepository.save(msg2);

        // 16. Bulletins
        Bulletin bul = new Bulletin();
        bul.setEtudiant(etu1);
        bul.setClasse(classe);
        bul.setAnneeScolaire(annee);
        bul.setPeriode(1);
        bul.setTypePeriode(TypePeriode.SEMESTRE);
        bul.setMoyenneGenerale(15.5);
        bul.setRang(1);
        bul.setAppreciation("Excellent travail, continuez ainsi.");
        bul.setDateGeneration(LocalDate.now().minusDays(1));
        bul = bulletinRepository.save(bul);

        // 17. Categorie de dépense et dépense
        CategorieDepense cat = new CategorieDepense();
        cat.setNom("Fournitures");
        cat.setDescription("Achat de matériel de bureau et craies");
        cat = categorieDepenseRepository.save(cat);

        Depense dep = new Depense();
        dep.setLibelle("Achat de feutres pour tableaux");
        dep.setMontant(15000.0);
        dep.setDateDepense(LocalDate.now().minusDays(4));
        dep.setDescription("Lot de 50 feutres effaçables");
        dep.setModePaiement("ESPECES");
        dep.setDateCreation(LocalDate.now().minusDays(4));
        dep.setUtilisateur(admin);
        dep.setCategorieDepense(cat);
        dep.setAnneeScolaire(annee);
        depenseRepository.save(dep);

        System.out.println("=== Base de données alimentée avec succès ! ===");
    }
}
