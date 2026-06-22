package gestion.scolaire.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;


import lombok.Data;

@Data
@Entity
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime heureDebut;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime heureFin;

    @Enumerated(EnumType.STRING)
    private StatutSeance statut;
    // PLANIFIEE, EN_COURS, TERMINEE

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    private String matiere;
    @ManyToOne
    private Affectation affectation;

    @ManyToOne
    private AnneeScolaire anneeScolaire;

    @OneToMany(mappedBy = "seance")
    @JsonIgnore
    private List<Appel> appels; 
}
