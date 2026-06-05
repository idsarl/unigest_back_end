package gestion.scolaire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.Classe;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Inscription;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    List<Inscription> findByClasseAndAnneeScolaire(Classe classe, AnneeScolaire anneeScolaire);

    List<Inscription> findByEtudiant(Etudiant etudiant);

    List<Inscription> findByClasseId(Long classeId);

    long countByAnneeScolaireActiveTrue();

    long countByAnneeScolaireId(Long anneeId);

    List<Inscription> findByEtudiantId(Long etudiantId);

    @Query("""
            SELECT i
            FROM Inscription i
            WHERE i.classe.id = :classeId
            AND i.anneeScolaire.active = true
            AND i.statut = 'INSCRIT'
            """)
    List<Inscription> findEtudiantsActifsByClasse(Long classeId);

    @Query("""
            SELECT count(i)
            FROM Inscription i
            WHERE i.classe.id = :classeId
            AND i.anneeScolaire.active = true
            AND i.statut = 'INSCRIT'
            """)
    long countEtudiantsActifsByClasse(Long classeId);
}
