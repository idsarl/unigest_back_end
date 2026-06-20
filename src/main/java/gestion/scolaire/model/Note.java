package gestion.scolaire.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(
        name = "note",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_note_unique",
                        columnNames = {
                                "etudiant_id",
                                "affectation_id",
                                "annee_scolaire_id",
                                "matiere_id",
                                "periode",
                                "type",
                                "type_periode",
                                "date_evaluation"
                        }
                )
        }
)
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "affectation_id")
    private Affectation affectation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "annee_scolaire_id")
    private AnneeScolaire anneeScolaire;

    @JsonIgnoreProperties({"affectations", "classeMatieres"})
    @ManyToOne(optional = false)
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;

    @Column(nullable = false)
    private double valeur;

    /**
     * coefficient figé au moment de la notation
     */
    @Column(nullable = false)
    private double coefficient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNote type;

    @Column(nullable = false)
    private Integer periode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_periode", nullable = false)
    private TypePeriode typePeriode;

    private LocalDate dateEvaluation;
}