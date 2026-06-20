package gestion.scolaire.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gestion.scolaire.dto.InscriptionEtudiantRequest;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.Classe;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Inscription;
import gestion.scolaire.model.Parent;
import gestion.scolaire.repository.AnneeScolaireRepository;
import gestion.scolaire.repository.ClasseRepository;
import gestion.scolaire.repository.EtudiantRepository;
import gestion.scolaire.repository.InscriptionRepository;
import gestion.scolaire.repository.ParentRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import gestion.scolaire.model.Role;

@Service
public class EtudiantService {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private AnneeScolaireRepository anneeScolaireRepository;

    @Autowired
    private InscriptionRepository inscriptionRepository;

    public Etudiant creerEtudiant(Etudiant etudiant) {
        // if (etudiant.getPassword() == null || etudiant.getPassword().isBlank()) {
        //     throw new IllegalArgumentException("Le mot de passe de l'étudiant est obligatoire");
        // }
        // if ((etudiant.getEmail() == null || etudiant.getEmail().isBlank()) && 
        //     (etudiant.getTelephone() == null || etudiant.getTelephone().isBlank())) {
        //     throw new IllegalArgumentException("L'identifiant de connexion (email ou téléphone) est obligatoire");
        // }

         String encodedPassword = passwordEncoder.encode("123456");
        etudiant.setPassword(encodedPassword);
        etudiant.setRole(Role.ETUDIANT);
        etudiant.setActif(true);

        // Générer le matricule automatiquement
        String matricule = genererMatricule();
        etudiant.setMatricule(matricule);

        Etudiant saved = etudiantRepository.save(etudiant);
        
        if (etudiant.getEmail() != null && !etudiant.getEmail().isBlank()) {
            try {
                envoyerMailCreation(saved, "123456");
            } catch (Exception ex) {
                // Ne pas bloquer la transaction si l'envoi de mail échoue (ex. pas de config SMTP en dev)
                System.err.println("Erreur d'envoi de mail à l'étudiant : " + ex.getMessage());
            }
        }

        return saved;
    }

    private void envoyerMailCreation(Etudiant e, String rawPassword) {
        String htmlContent = """
                <div style="font-family: sans-serif; padding: 20px;">
                    <h2>Bienvenue sur Lyuni-Gest</h2>
                    <p>Bonjour %s %s,</p>
                    <p>Votre compte étudiant a été créé avec succès.</p>
                    <div style="background: #f4f4f4; padding: 15px; border-radius: 5px;">
                        <p><strong>Identifiant :</strong> %s</p>
                        <p><strong>Mot de passe :</strong> <span style="color: #007bff;">%s</span></p>
                        <p><strong>Matricule :</strong> %s</p>
                    </div>
                    <p><a href="https://lyuni-gest.com/" style="padding: 10px 20px; background: #007bff; color: white; text-decoration: none;">Accéder à la plateforme</a></p>
                    <p>⚠️ <i>Veuillez changer votre mot de passe à la première connexion.</i></p>
                </div>
                """
                .formatted(e.getPrenom(), e.getNom(), e.getEmail() != null && !e.getEmail().isBlank() ? e.getEmail() : e.getTelephone(), rawPassword, e.getMatricule());

        emailService.envoyerEmailHtml(e.getEmail(), "Création de votre compte Étudiant - Accès plateforme mobile", htmlContent);
    }

    public String genererMatricule() {
    int annee = LocalDate.now().getYear();

    // Compter le nombre d'étudiants déjà existants
    long count = etudiantRepository.count();

    // Incrément
    long numero = count + 1;

    // Formatage avec 4 chiffres
    return "ETU-" + annee + "-" + String.format("%04d", numero);
}

    public List<Etudiant> listerEtudiants(){
        return etudiantRepository.findAll();
    }

    public Etudiant getEtudiant(Long id){
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etudiant non trouvé"));
    }

    public Etudiant modifierEtudiant(Long id, Etudiant data){

        Etudiant e = getEtudiant(id);

        e.setNom(data.getNom());
        e.setPrenom(data.getPrenom());
        e.setEmail(data.getEmail());
        e.setTelephone(data.getTelephone());
        e.setDateNaissance(data.getDateNaissance());
        e.setParent(data.getParent());

        return etudiantRepository.save(e);
    }

    public List<Etudiant> getEtudiantsParClasse(Long classeId){
        return etudiantRepository.findByInscriptionClasseId(classeId);
    }

     public Etudiant getEtudiantParMatricule(String matricule){

        return etudiantRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Etudiant introuvable"));
    }

    public Etudiant creerEtudiantAvecInscription(
            InscriptionEtudiantRequest request) {

        Etudiant etudiant = new Etudiant();
        etudiant.setNom(request.getNom());
        etudiant.setPrenom(request.getPrenom());
        etudiant.setEmail(request.getEmail());
        etudiant.setTelephone(request.getTelephone());
        etudiant.setPassword(request.getPassword());
        etudiant.setDateNaissance(request.getDateNaissance());

        return creerEtudiantAvecInscription(
                etudiant,
                request.getClasseId(),
                request.getAnneeId(),
                request.getParentId(),
                request.getMontantReduction(),
                request.getMotifReduction());
    }

    public Etudiant creerEtudiantAvecInscription(
            Etudiant etudiant,
            Long classeId,
            Long anneeId,
            Long parentId,
            double montantReduction,
            String motifReduction) {

        // Valider l'étudiant
        if (etudiant.getPassword() == null || etudiant.getPassword().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe de l'étudiant est obligatoire");
        }
        if ((etudiant.getEmail() == null || etudiant.getEmail().isBlank()) &&
            (etudiant.getTelephone() == null || etudiant.getTelephone().isBlank())) {
            throw new IllegalArgumentException("L'identifiant de connexion (email ou téléphone) est obligatoire");
        }

        // Récupérer le parent
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        // Récupérer la classe
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));

        // Récupérer l'année scolaire
        AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(anneeId)
                .orElseThrow(() -> new RuntimeException("Année scolaire introuvable"));

        // Encoder le mot de passe
        String rawPassword = etudiant.getPassword();
        etudiant.setPassword(passwordEncoder.encode(rawPassword));
        etudiant.setRole(Role.ETUDIANT);
        etudiant.setActif(true);
        etudiant.setParent(parent);

        // Générer le matricule automatiquement
        String matricule = genererMatricule();
        etudiant.setMatricule(matricule);

        // Sauvegarder l'étudiant
        Etudiant savedEtudiant = etudiantRepository.save(etudiant);

        // Créer l'inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(savedEtudiant);
        inscription.setClasse(classe);
        inscription.setAnneeScolaire(anneeScolaire);
        inscription.setDateInscription(LocalDate.now());
        inscription.setMontantReduction(montantReduction);
        inscription.setMotifReduction(motifReduction);
        inscription.setStatut("INSCRIT");

        inscriptionRepository.save(inscription);

        // Envoyer l'email de création
        if (etudiant.getEmail() != null && !etudiant.getEmail().isBlank()) {
            try {
                envoyerMailCreation(savedEtudiant, rawPassword);
            } catch (Exception ex) {
                System.err.println("Erreur d'envoi de mail à l'étudiant : " + ex.getMessage());
            }
        }

        return savedEtudiant;
    }

    public void supprimerEtudiant(Long id){
        etudiantRepository.deleteById(id);
    }
}