package gestion.scolaire.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import gestion.scolaire.model.Enseignant;
import gestion.scolaire.model.Parent;
import gestion.scolaire.model.Role;
import gestion.scolaire.repository.ParentRepository;

@Service
public class ParentService {

    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Créer un parent
    public Parent creerParent(Parent parent) {
        if (parent.getPassword() == null || parent.getPassword().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe du parent est obligatoire");
        }
        if ((parent.getEmail() == null || parent.getEmail().isBlank()) && 
            (parent.getTelephone() == null || parent.getTelephone().isBlank())) {
            throw new IllegalArgumentException("L'identifiant de connexion (email ou téléphone) est obligatoire");
        }

        String rawPassword = parent.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        parent.setRole(Role.PARENT);
        parent.setActif(true);
        parent.setTelephone(parent.getTelephone());
        parent.setAdresse(parent.getAdresse());
        parent.setNom(parent.getNom());
        parent.setPrenom(parent.getPrenom());
        parent.setPassword(encodedPassword);

        Parent saved = parentRepository.save(parent);
        if (parent.getEmail() != null && !parent.getEmail().isBlank()) {
            envoyerMailCreation(saved, rawPassword);
        }

        return saved;
    }

    private void envoyerMailCreation(Parent e, String rawPassword) {
        // 2. Construction du HTML
        String htmlContent = """
                <div style="font-family: sans-serif; padding: 20px;">
                    <h2>Bienvenue sur Lyuni-Gest</h2>
                    <p>Bonjour %s %s,</p>
                    <p>Votre compte parent a été créé avec succès.</p>
                    <div style="background: #f4f4f4; padding: 15px; border-radius: 5px;">
                        <p><strong>Identifiant :</strong> %s</p>
                        <p><strong>Mot de passe :</strong> <span style="color: #007bff;">%s</span></p>
                    </div>
                    <p><a href="https://lyuni-gest.com/" style="padding: 10px 20px; background: #007bff; color: white; text-decoration: none;">Accéder à la plateforme</a></p>
                    <p>⚠️ <i>Veuillez changer votre mot de passe à la première connexion.</i></p>
                </div>
                """
                .formatted(e.getPrenom(), e.getNom(), e.getEmail() != null && !e.getEmail().isBlank() ? e.getEmail() : e.getTelephone(), rawPassword);

        // 3. Appel du service
        emailService.envoyerEmailHtml(e.getEmail(), "Création de votre compte - Accès plateforme mobile", htmlContent);
    }

    // Modifier un parent
    public Parent modifierParent(Long id, Parent data) {

        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));

        parent.setNom(data.getNom());
        parent.setPrenom(data.getPrenom());
        parent.setEmail(data.getEmail());
        parent.setTelephone(data.getTelephone());
        parent.setAdresse(data.getAdresse());

        return parentRepository.save(parent);
    }

    // Supprimer
    public void supprimerParent(Long id) {
        parentRepository.deleteById(id);
    }

    // Trouver par ID
    public Parent getParentById(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));
    }

    // Lister tous les parents
    public List<Parent> getAllParents() {
        return parentRepository.findAll();
    }
}
