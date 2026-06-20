package gestion.scolaire.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.Enseignant;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Parent;
import gestion.scolaire.model.Role;
import gestion.scolaire.model.Utilisateur;
import gestion.scolaire.repository.EnseignantRepository;
import gestion.scolaire.repository.ParentRepository;
import gestion.scolaire.repository.UtilisateurRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Ajouter un utilisateur générique
 public Utilisateur ajouterUtilisateur(Utilisateur user){

    if(utilisateurRepository.existsByEmail(user.getEmail())){
        throw new RuntimeException("Un utilisateur avec cet email existe déjà");
    }

    if(utilisateurRepository.existsByTelephone(user.getTelephone())){
        throw new RuntimeException("Un utilisateur avec ce téléphone existe déjà");
    }

    return utilisateurRepository.save(user);
}

    // Modifier un utilisateur
    public Utilisateur modifierUtilisateur(Long id, String nom, String prenom,
                                           String email, String telephone,
                                           String password, Boolean actif){
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if(nom != null) user.setNom(nom);
        if(prenom != null) user.setPrenom(prenom);
        if(email != null) user.setEmail(email);
        if(telephone != null) user.setTelephone(telephone);
        if(actif != null) user.setActif(actif);
        if(password != null && !password.isEmpty()) {
            log.info("Modification du mot de passe pour l'utilisateur ID {} ({})", id, user.getEmail());
            user.setPassword(passwordEncoder.encode(password));
        }

        Utilisateur savedUser = utilisateurRepository.save(user);
        log.info("Utilisateur mis à jour avec succès !");
        return savedUser;
    }

    // Supprimer un utilisateur
    public void supprimerUtilisateur(Long id){
        utilisateurRepository.deleteById(id);
    }

    // Récupérer un utilisateur par ID
    public Utilisateur getUtilisateurById(Long id){
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // Récupérer tous les utilisateurs par rôle
    public List<Utilisateur> getUtilisateursParRole(Role role){
        return utilisateurRepository.findByRole(role);
    }

    // Récupérer un utilisateur par email
    public Utilisateur getUtilisateurByEmail(String email){
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // --- Parent spécifique ---
    public List<Etudiant> getEnfants(Long parentId){
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent introuvable"));
        return parent.getEnfants();
    }

    // --- Enseignant spécifique ---
    public List<Affectation> getAffectations(Long enseignantId){
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));
        return enseignant.getAffectations();
    }
}
