package gestion.scolaire.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestion.scolaire.model.Seance;
import gestion.scolaire.model.StatutSeance;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    // Séances par affectation
    List<Seance> findByAffectationId(Long affectationId);

    // Séances par date
    List<Seance> findByDate(LocalDate date);

    // Séances par affectation et date
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

    @Query("SELECT s FROM Seance s WHERE s.statut = :statut AND (s.date < :date OR (s.date = :date AND s.heureFin < :time))")
    List<Seance> findPastSeancesByStatut(
            @Param("statut") StatutSeance statut,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time);
}
