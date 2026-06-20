package gestion.scolaire.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.*;

import gestion.scolaire.model.EmploiDuTemps;

public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {

    List<EmploiDuTemps> findByClasseId(Long classeId);

    List<EmploiDuTemps> findByEnseignantId(Long enseignantId);

    List<EmploiDuTemps> findByActifTrue();

      @Query("SELECT e FROM EmploiDuTemps e WHERE e.classe IS NULL AND e.actif = true")
    List<EmploiDuTemps> findRecreationsGlobales();

    
   @Query("""
    SELECT e
    FROM EmploiDuTemps e
    WHERE e.actif = true
    AND (e.dateDebut IS NULL OR e.dateDebut <= :today)
    AND (e.dateFin IS NULL OR e.dateFin >= :today)
""")
List<EmploiDuTemps> findAllValides(@Param("today") LocalDate today);

   @Query("""
    SELECT e FROM EmploiDuTemps e 
    JOIN e.jours j 
    WHERE e.enseignant.id = :enseignantId 
    AND e.actif = true 
    AND (e.dateDebut IS NULL OR e.dateDebut <= :date) 
    AND (e.dateFin IS NULL OR e.dateFin >= :date) 
    AND j = :jour
""")
List<EmploiDuTemps> findAllValidesByDateAndJour(
        @Param("enseignantId") Long enseignantId,
        @Param("date") LocalDate date,
        @Param("jour") gestion.scolaire.dto.JourSemaine jour);

    List<EmploiDuTemps> findByActifTrueAndAnneeScolaireId(Long anneeId);

    List<EmploiDuTemps> findByClasseIdAndAnneeScolaireId(Long classeId, Long anneeId);
}