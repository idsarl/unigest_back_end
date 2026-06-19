package gestion.scolaire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestion.scolaire.model.Inscription;
import gestion.scolaire.model.Paiement;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Somme des montants pour une inscription
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.inscription = :inscription")
    Double sumMontantByInscription(@Param("inscription") Inscription inscription);

    // Paiements d'un étudiant
    @Query("SELECT p FROM Paiement p WHERE p.inscription.etudiant.id = :etudiantId")
    List<Paiement> findByEtudiantId(@Param("etudiantId") Long etudiantId);

    // Historique des paiements par classe et année scolaire
    @Query("SELECT p FROM Paiement p " +
           "WHERE p.inscription.classe.id = :classeId " +
           "AND p.inscription.anneeScolaire.id = :anneeId")
    List<Paiement> findByClasseAndAnnee(@Param("classeId") Long classeId,
                                        @Param("anneeId") Long anneeId);

     // Historique des paiements pour un étudiant dans une classe et année scolaire spécifique
    @Query("SELECT p FROM Paiement p " +
           "WHERE p.inscription.etudiant.id = :etudiantId " +
           "AND p.inscription.classe.id = :classeId " +
           "AND p.inscription.anneeScolaire.id = :anneeId " +
           "ORDER BY p.datePaiement ASC")
    List<Paiement> findByEtudiantClasseAnnee(@Param("etudiantId") Long etudiantId,
                                             @Param("classeId") Long classeId,
                                             @Param("anneeId") Long anneeId);

    // Tous les paiements d'une année scolaire
    @Query("SELECT p FROM Paiement p WHERE p.inscription.anneeScolaire.id = :anneeId")
    List<Paiement> findByAnneeId(@Param("anneeId") Long anneeId);

    // Paiements d'un étudiant pour une année scolaire
    @Query("SELECT p FROM Paiement p " +
           "WHERE p.inscription.etudiant.id = :etudiantId " +
           "AND p.inscription.anneeScolaire.id = :anneeId " +
           "ORDER BY p.datePaiement ASC")
    List<Paiement> findByEtudiantIdAndAnneeId(@Param("etudiantId") Long etudiantId,
                                              @Param("anneeId") Long anneeId);

}