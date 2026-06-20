package gestion.scolaire.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestion.scolaire.model.Seance;
import gestion.scolaire.model.StatutSeance;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    // ── Sans filtre année (conservés pour compatibilité interne) ──────────────
    List<Seance> findByAffectationId(Long affectationId);
    List<Seance> findByDate(LocalDate date);
    List<Seance> findByAffectationIdAndDate(Long affectationId, LocalDate date);

    // Séances par enseignant et date
    @Query("SELECT s FROM Seance s WHERE s.affectation.enseignant.id = :enseignantId AND s.date = :date")
    List<Seance> findByAffectationEnseignantIdAndDate(@Param("enseignantId") Long enseignantId, @Param("date") LocalDate date);

    // Séances en cours
    List<Seance> findByStatut(StatutSeance statut);

    @Query("SELECT s FROM Seance s WHERE s.affectation.classe.id = :classeId AND s.date = :date")
    List<Seance> findByAffectationClasseIdAndDate(
            @Param("classeId") Long classeId,
            @Param("date") LocalDate date);

    // ── Filtrés par année scolaire ────────────────────────────────────────────
    List<Seance> findByAnneeScolaireId(Long anneeId);

    List<Seance> findByStatutAndAnneeScolaireId(StatutSeance statut, Long anneeId);
}
