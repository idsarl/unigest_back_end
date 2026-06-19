package gestion.scolaire.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gestion.scolaire.model.Seance;
import gestion.scolaire.model.StatutSeance;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    // ── Sans filtre année (conservés pour compatibilité interne) ──────────────
    List<Seance> findByAffectationId(Long affectationId);
    List<Seance> findByDate(LocalDate date);
    List<Seance> findByAffectationIdAndDate(Long affectationId, LocalDate date);
    List<Seance> findByStatut(StatutSeance statut);

    // ── Filtrés par année scolaire ────────────────────────────────────────────
    List<Seance> findByAnneeScolaireId(Long anneeId);
    List<Seance> findByDateAndAnneeScolaireId(LocalDate date, Long anneeId);
    List<Seance> findByAffectationIdAndAnneeScolaireId(Long affectationId, Long anneeId);
    List<Seance> findByAffectationIdAndDateAndAnneeScolaireId(Long affectationId, LocalDate date, Long anneeId);
    List<Seance> findByStatutAndAnneeScolaireId(StatutSeance statut, Long anneeId);
}
