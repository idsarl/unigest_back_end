package gestion.scolaire.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.Classe;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Inscription;
import gestion.scolaire.repository.AnneeScolaireRepository;
import gestion.scolaire.repository.ClasseRepository;
import gestion.scolaire.repository.EtudiantRepository;
import gestion.scolaire.repository.InscriptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final ClasseRepository classeRepository;
    private final AnneeScolaireRepository anneeRepository;
    private final AnneeScolaireService anneeScolaireService;

    // ─────────────────────────────────────────────────────────────────────────
    // Écriture
    // ─────────────────────────────────────────────────────────────────────────

    public Inscription inscrireEtudiant(
            Long etudiantId,
            double montantReduction,
            String motifReduction,
            Long classeId,
            Long anneeId) {

        Etudiant etudiant = etudiantRepository.findById(etudiantId).orElseThrow();
        Classe classe     = classeRepository.findById(classeId).orElseThrow();
        AnneeScolaire annee = anneeRepository.findById(anneeId).orElseThrow();

        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setClasse(classe);
        inscription.setAnneeScolaire(annee);
        inscription.setMontantReduction(montantReduction);
        inscription.setMotifReduction(motifReduction);
        inscription.setDateInscription(LocalDate.now());
        inscription.setStatut("INSCRIT");
        return inscriptionRepository.save(inscription);
    }

    public Inscription modifierInscription(
            Long id, Long classeId, Long anneeId,
            String motifReduction, double montantReduction) {

        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));
        AnneeScolaire annee = anneeRepository.findById(anneeId)
                .orElseThrow(() -> new RuntimeException("Année introuvable"));

        inscription.setClasse(classe);
        inscription.setAnneeScolaire(annee);
        inscription.setMontantReduction(montantReduction);
        inscription.setMotifReduction(motifReduction);
        return inscriptionRepository.save(inscription);
    }

    public void supprimerInscription(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
        inscriptionRepository.delete(inscription);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture — toutes filtrées par année scolaire active
    // ─────────────────────────────────────────────────────────────────────────

    /** Toutes les inscriptions de l'année active. */
    public List<Inscription> getAll() {
        return inscriptionRepository.findByAnneeScolaireId(anneeActiveId());
    }

    /** Toutes les inscriptions d'un étudiant (historique complet). */
    public List<Inscription> getAllEtudiant(Long etudiantId) {
        return inscriptionRepository.findByEtudiantId(etudiantId);
    }

    /** Inscription unique par ID. */
    public Inscription getInscription(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
    }

    /** Étudiants actifs d'une classe (année active, statut INSCRIT). */
    public List<Etudiant> getEtudiantsActifsParClasse(Long classeId) {
        return inscriptionRepository.findEtudiantsActifsByClasse(classeId)
                .stream().map(Inscription::getEtudiant).toList();
    }

    /** Étudiants d'une classe pour l'année active. */
    public List<Etudiant> getEtudiantsParClasse(Long classeId) {
        return inscriptionRepository
                .findByClasseIdAndAnneeScolaireId(classeId, anneeActiveId())
                .stream().map(Inscription::getEtudiant).toList();
    }

    /** Étudiants d'une classe pour une année spécifique (pour rapports historiques). */
    public List<Etudiant> getEtudiantsParClasseEtAnnee(Long classeId, Long anneeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));
        AnneeScolaire annee = anneeRepository.findById(anneeId)
                .orElseThrow(() -> new RuntimeException("Année introuvable"));
        return inscriptionRepository.findByClasseAndAnneeScolaire(classe, annee)
                .stream().map(Inscription::getEtudiant).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Long anneeActiveId() {
        return anneeScolaireService.getAnneeActive().getId();
    }
}
