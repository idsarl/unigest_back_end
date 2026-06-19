package gestion.scolaire.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.CategorieDepense;
import gestion.scolaire.model.Depense;
import gestion.scolaire.repository.CategorieDepenseRepository;
import gestion.scolaire.repository.DepenseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepenseService {

    private final DepenseRepository depenseRepository;
    private final CategorieDepenseRepository categorieRepository;
    private final AnneeScolaireService anneeScolaireService;

    // ─────────────────────────────────────────────────────────────────────────
    // Écriture
    // ─────────────────────────────────────────────────────────────────────────

    /** Crée une dépense et l'associe automatiquement à l'année scolaire active. */
    public Depense createDepense(Depense depense) {
        if (depense.getCategorieDepense() != null && depense.getCategorieDepense().getId() != null) {
            CategorieDepense categorie = categorieRepository.findById(
                    depense.getCategorieDepense().getId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            depense.setCategorieDepense(categorie);
        }

        depense.setAnneeScolaire(anneeScolaireService.getAnneeActive());
        depense.setDateCreation(LocalDate.now());
        return depenseRepository.save(depense);
    }

    public Depense updateDepense(Long id, Depense depenseDetails) {
        Depense depense = depenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépense introuvable avec l'id : " + id));

        depense.setLibelle(depenseDetails.getLibelle());
        depense.setMontant(depenseDetails.getMontant());
        depense.setDateDepense(depenseDetails.getDateDepense());
        depense.setDescription(depenseDetails.getDescription());
        depense.setModePaiement(depenseDetails.getModePaiement());

        if (depenseDetails.getCategorieDepense() != null && depenseDetails.getCategorieDepense().getId() != null) {
            CategorieDepense categorie = categorieRepository.findById(
                    depenseDetails.getCategorieDepense().getId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            depense.setCategorieDepense(categorie);
        }
        return depenseRepository.save(depense);
    }

    public void deleteDepense(Long id) {
        depenseRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture — toutes filtrées par année scolaire active
    // ─────────────────────────────────────────────────────────────────────────

    /** Toutes les dépenses de l'année scolaire active. */
    public List<Depense> getAllDepenses() {
        return depenseRepository.findByAnneeScolaireId(anneeActiveId());
    }

    public Depense getDepenseById(Long id) {
        return depenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépense introuvable"));
    }

    /** Dépenses d'une catégorie pour l'année active. */
    public List<Depense> getDepensesByCategorie(Long categorieId) {
        return depenseRepository.findByCategorieDepenseIdAndAnneeScolaireId(categorieId, anneeActiveId());
    }

    /** Dépenses dans une plage de dates, pour l'année active. */
    public List<Depense> getDepensesByDateRange(LocalDate dateDebut, LocalDate dateFin) {
        return depenseRepository.findByDateDepenseBetweenAndAnneeScolaireId(dateDebut, dateFin, anneeActiveId());
    }

    /** Total des dépenses de l'année active. */
    public Double getTotalDepenses() {
        return depenseRepository.sumMontantByAnnee(anneeActiveId());
    }

    /** Total des dépenses d'une catégorie pour l'année active. */
    public Double getTotalByCategorie(Long categorieId) {
        return depenseRepository.sumMontantByCategorieAndAnnee(categorieId, anneeActiveId());
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Long anneeActiveId() {
        return anneeScolaireService.getAnneeActive().getId();
    }
}
