package gestion.scolaire.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Enseignant enseignant;

    @ManyToMany
    @JoinTable(name = "affectation_matiere", joinColumns = @JoinColumn(name = "affectation_id"), inverseJoinColumns = @JoinColumn(name = "matiere_id"))
    private List<Matiere> matieres;

    @ManyToOne
    private Classe classe;

    private LocalDate dateCreation;
    private LocalDate dateModification;

    @OneToMany(mappedBy = "affectation")
    @JsonIgnore
    private List<Seance> seances;

    @OneToMany(mappedBy = "affectation")
    @JsonIgnore
    private List<Note> notes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "annee_scolaire_id")
    private AnneeScolaire anneeScolaire;
}
