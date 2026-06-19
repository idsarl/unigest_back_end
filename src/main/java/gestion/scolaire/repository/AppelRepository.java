package gestion.scolaire.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

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

    List<Appel> findByEtudiantIdAndSeanceAnneeScolaireId(Long etudiantId, Long anneeId);

    List<Appel> findByEtudiantIdAndSeanceAnneeScolaireIdAndStatut(
            Long etudiantId, Long anneeId, StatutPresence statut);
}
