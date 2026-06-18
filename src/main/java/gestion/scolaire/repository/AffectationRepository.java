package gestion.scolaire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestion.scolaire.dto.AffectationDTO;
import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.Classe;
import gestion.scolaire.model.Enseignant;
import gestion.scolaire.model.Matiere;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {

    List<Affectation> findByEnseignant(Enseignant enseignant);

    List<Affectation> findByClasse(Classe classe);

    // List<Affectation> findByMatiere(Matiere matiere);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Affectation a JOIN a.matieres m WHERE a.classe.id = :classeId AND a.enseignant.id = :enseignantId AND m.nom = :matiereNom")
    Optional<Affectation> findByClasseAndEnseignantAndMatiere(@Param("classeId") Long classeId, @Param("enseignantId") Long enseignantId, @Param("matiereNom") String matiereNom);

}
