package gestion.scolaire.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.Matiere;
import gestion.scolaire.repository.AffectationRepository;
import gestion.scolaire.repository.ClasseRepository;
import gestion.scolaire.repository.EnseignantRepository;
import gestion.scolaire.repository.MatiereRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final EnseignantRepository enseignantRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final AnneeScolaireService anneeScolaireService;

    public Affectation ajouterAffectation(Long enseignantId, List<Long> matiereIds, Long classeId) {
        var enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));
        List<Matiere> matieres = matiereRepository.findAllById(matiereIds);
        if (matieres.isEmpty()) throw new RuntimeException("Aucune matière trouvée");
        var classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));

        AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
        Affectation affectation = new Affectation();
        affectation.setAnneeScolaire(anneeActive);
        affectation.setEnseignant(enseignant);
        affectation.setMatieres(matieres);
        affectation.setClasse(classe);
        affectation.setDateCreation(LocalDate.now());
        return affectationRepository.save(affectation);
    }

    public Affectation modifierAffectation(Long affectationId, Long enseignantId, List<Long> matiereIds, Long classeId) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable"));

        if (enseignantId != null) {
            affectation.setEnseignant(enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new RuntimeException("Enseignant introuvable")));
        }
        if (matiereIds != null && !matiereIds.isEmpty()) {
            List<Matiere> matieres = matiereRepository.findAllById(matiereIds);
            if (matieres.isEmpty()) throw new RuntimeException("Aucune matière trouvée");
            affectation.setMatieres(matieres);
        }
        if (classeId != null) {
            affectation.setClasse(classeRepository.findById(classeId)
                    .orElseThrow(() -> new RuntimeException("Classe introuvable")));
        }
        affectation.setDateModification(LocalDate.now());
        return affectationRepository.save(affectation);
    }

    public void supprimerAffectation(Long affectationId) {
        affectationRepository.deleteById(affectationId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture — filtrées par année scolaire active
    // ─────────────────────────────────────────────────────────────────────────

    public List<Affectation> getAll() {
        return affectationRepository.findByAnneeScolaireId(anneeActiveId());
    }

    public List<Affectation> getAffectationsParEnseignant(Long enseignantId) {
        enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant introuvable"));
        return affectationRepository.findByEnseignantIdAndAnneeScolaireId(enseignantId, anneeActiveId());
    }

    public List<Affectation> getAffectationsParClasse(Long classeId) {
        classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));
        return affectationRepository.findByClasseIdAndAnneeScolaireId(classeId, anneeActiveId());
    }

    public Affectation getAffectationById(Long affectationId) {
        return affectationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable"));
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Long anneeActiveId() {
        return anneeScolaireService.getAnneeActive().getId();
    }
}
