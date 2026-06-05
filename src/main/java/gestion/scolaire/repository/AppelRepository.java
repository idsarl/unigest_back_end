package gestion.scolaire.repository;

import java.time.LocalDate;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import gestion.scolaire.model.Appel;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Seance;
import gestion.scolaire.model.StatutPresence;

public interface AppelRepository extends JpaRepository<Appel, Long> {

    List<Appel> findBySeance(Seance seance);
        List<Appel> findBySeanceId(Long seanceId);

    List<Appel> findByEtudiantAndSeance(Long idEtudiant,  Long seanceId);
    Optional<Appel> findBySeanceIdAndEtudiantId(Long seanceId, Long etudiantId);
    List<Appel> findByEtudiantId(Long etudiantId);

    List<Appel> findBySeance(Long seanceId);
    List<Appel> findByStatut(StatutPresence statut);
    void deleteBySeanceIdAndEtudiantId(Long seanceId, Long etudiantId);

    void deleteBySeanceId(Long seanceId);

    List<Appel> findBySeanceAnneeScolaireId(Long anneeId);

     List<Appel> findByEtudiantIdAndSeanceAnneeScolaireIdAndStatut(
            Long etudiantId,
            Long anneeId,
            StatutPresence statut
    );

    @Query("SELECT count(a) FROM Appel a WHERE a.seance.affectation.enseignant.id = :enseignantId AND a.seance.date = :date AND a.statut = :statut")
    long countBySeanceAffectationEnseignantIdAndSeanceDateAndStatut(
            @Param("enseignantId") Long enseignantId,
            @Param("date") LocalDate date,
            @Param("statut") StatutPresence statut
    );

    @Query("SELECT count(a) FROM Appel a WHERE a.seance.affectation.classe.id = :classeId AND a.seance.anneeScolaire.active = true AND a.statut = :statut")
    long countByClasseIdAndStatut(
            @Param("classeId") Long classeId,
            @Param("statut") StatutPresence statut
    );

    @Query("SELECT count(a) FROM Appel a WHERE a.seance.id = :seanceId AND a.statut = :statut")
    long countBySeanceIdAndStatut(
            @Param("seanceId") Long seanceId,
            @Param("statut") StatutPresence statut
    );
}
