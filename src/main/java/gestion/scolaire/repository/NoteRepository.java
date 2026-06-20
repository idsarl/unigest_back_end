package gestion.scolaire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Note;
import gestion.scolaire.model.TypeNote;
import gestion.scolaire.model.TypePeriode;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

        List<Note> findByEtudiantId(Long etudiantId);

        List<Note> findByAffectationId(Long affectationId);

        boolean existsByEtudiantIdAndAffectationIdAndAnneeScolaireIdAndMatiereIdAndPeriodeAndTypeAndTypePeriode(
            Long etudiantId,
            Long affectationId,
            Long anneeScolaireId,
            Long matiereId,
            Integer periode,
            TypeNote type,
            TypePeriode typePeriode
    );

    List<Note> findByEtudiantIdAndAnneeScolaireId(
            Long etudiantId,
            Long anneeScolaireId
    );

    List<Note> findByEtudiantIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
            Long etudiantId,
            Long anneeScolaireId,
            Integer periode,
            TypePeriode typePeriode
    );

    List<Note> findByAffectationIdAndAnneeScolaireId(
            Long affectationId,
            Long anneeScolaireId
    );

    List<Note> findByAnneeScolaireIdAndPeriodeAndTypePeriode(
            Long anneeScolaireId,
            Integer periode,
            TypePeriode typePeriode
    );

    List<Note> findByAffectationIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
            Long affectationId,
            Long anneeScolaireId,
            Integer periode,
            TypePeriode typePeriode
    );

    List<Note> findByAffectationClasseIdAndAnneeScolaireIdAndPeriodeAndTypePeriode(
        Long classeId,
        Long anneeScolaireId,
        Integer periode,
        TypePeriode typePeriode
    );

    @Query("""
                            SELECT n
                            FROM Note n
                            WHERE n.affectation.classe.id = :classeId
                            AND n.anneeScolaire.id = :anneeScolaireId
                            AND n.periode = :periode
                            AND n.typePeriode = :typePeriode
                        """)
    List<Note> findByClasseAndPeriode(
                    @Param("classeId") Long classeId,
                    @Param("anneeScolaireId") Long anneeScolaireId,
                    @Param("periode") Integer periode,
                    @Param("typePeriode") TypePeriode typePeriode);

    @Query("""
                            SELECT AVG(n.valeur)
                            FROM Note n
                            WHERE n.affectation.enseignant.id = :enseignantId
                            AND n.matiere.nom = :matiereNom
                        """)
    Double findAverageByAffectationEnseignantIdAndMatiereNom(
                    @Param("enseignantId") Long enseignantId,
                    @Param("matiereNom") String matiereNom);

    @Query("""
                            SELECT n.affectation.classe.id, AVG(n.valeur)
                            FROM Note n
                            WHERE n.affectation.enseignant.id = :enseignantId
                            AND n.matiere.nom = :matiereNom
                            GROUP BY n.affectation.classe.id
                        """)
    List<Object[]> findAverageByAffectationEnseignantIdAndMatiereNomGroupByClasse(
                    @Param("enseignantId") Long enseignantId,
                    @Param("matiereNom") String matiereNom);

    @Query("""
            SELECT n FROM Note n
            WHERE n.affectation.enseignant.id = :enseignantId
            AND n.matiere.nom = :matiereNom
            AND n.affectation.classe.id = :classeId
           """)
    List<Note> findByEnseignantMatiereClasse(
            @Param("enseignantId") Long enseignantId,
            @Param("matiereNom") String matiereNom,
            @Param("classeId") Long classeId
    );
}