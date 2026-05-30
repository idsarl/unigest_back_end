package gestion.scolaire.model;

import java.time.LocalDate;
import java.time.LocalTime;

import gestion.scolaire.dto.JourSemaine;
import gestion.scolaire.dto.Periodicite;
import gestion.scolaire.dto.TypeEmploi;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class EmploiDuTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Classe concernée
    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;

    // Enseignant
    @ManyToOne
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Enseignant enseignant;

    // Matière
    @ManyToOne
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    // Salle
    private String salle;

    // Jour
    @Enumerated(EnumType.STRING)
    private JourSemaine jour;

    // Heure début
    private LocalTime heureDebut;

    // Heure fin
    private LocalTime heureFin;

    // Début validité emploi du temps
    private LocalDate dateDebut;

    // Fin validité
    private LocalDate dateFin;

    // Couleur affichage calendrier
    private String couleur;

    @Enumerated(EnumType.STRING)
private TypeEmploi type = TypeEmploi.COURS;

    // Actif ou non
    private boolean actif = true;

    // Type périodicité
    @Enumerated(EnumType.STRING)
    private Periodicite periodicite;

    // Description
    @Column(columnDefinition = "TEXT")
    private String description;
}