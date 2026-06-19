package gestion.scolaire.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestion.scolaire.model.Depense;

@Repository
public interface DepenseRepository extends JpaRepository<Depense, Long> {

    // ── Sans filtre année (conservés pour compatibilité) ──────────────────────
    List<Depense> findByCategorieDepenseId(Long categorieId);
    List<Depense> findByDateDepenseBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(d.montant),0) FROM Depense d")
    Double sumMontant();

    @Query("SELECT COALESCE(SUM(d.montant),0) FROM Depense d WHERE d.categorieDepense.id = :categorieId")
    Double sumMontantByCategorieDepense(@Param("categorieId") Long categorieId);

    // ── Filtrés par année scolaire ────────────────────────────────────────────
    List<Depense> findByAnneeScolaireId(Long anneeId);

    List<Depense> findByCategorieDepenseIdAndAnneeScolaireId(Long categorieId, Long anneeId);

    List<Depense> findByDateDepenseBetweenAndAnneeScolaireId(LocalDate start, LocalDate end, Long anneeId);

    @Query("SELECT COALESCE(SUM(d.montant),0) FROM Depense d WHERE d.anneeScolaire.id = :anneeId")
    Double sumMontantByAnnee(@Param("anneeId") Long anneeId);

    @Query("SELECT COALESCE(SUM(d.montant),0) FROM Depense d WHERE d.categorieDepense.id = :categorieId AND d.anneeScolaire.id = :anneeId")
    Double sumMontantByCategorieAndAnnee(@Param("categorieId") Long categorieId, @Param("anneeId") Long anneeId);
}
