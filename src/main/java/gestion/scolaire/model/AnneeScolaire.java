package gestion.scolaire.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;


import lombok.Data;

@Data
@Entity
public class AnneeScolaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String libelle; // 2024-2025

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDate dateAjout;

    private boolean active = true;

    @OneToMany(mappedBy = "anneeScolaire")
    @JsonIgnore
    private List<Inscription> inscriptions;

    @OneToMany(mappedBy = "anneeScolaire")
    @JsonIgnore
    private List<Depense> depenses;

    @OneToMany(mappedBy = "anneeScolaire")
    @JsonIgnore
    private List<Seance> seance;
    
    @OneToMany(mappedBy = "anneeScolaire")
    @JsonIgnore
    private List<EmploiDuTemps> emploiDuTemps;
  
    @OneToMany(mappedBy = "anneeScolaire")
    @JsonIgnore
    private List<Affectation> affectation;
}