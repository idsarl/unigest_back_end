package gestion.scolaire.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom; // ex: L1 Informatique

    @ManyToOne
    private Filiere filiere;

    @OneToMany(mappedBy = "classe")
    @JsonIgnore
    private List<Affectation> affectations;

    @OneToMany(mappedBy = "classe")
    @JsonIgnore
    private List<Inscription> inscriptions;

    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL)
    @JsonIgnore // 🔥 AJOUTE ÇA
    private List<ClasseMatiere> classeMatieres;
}
