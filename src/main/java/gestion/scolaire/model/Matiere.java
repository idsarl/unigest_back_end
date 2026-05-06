package gestion.scolaire.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
public class Matiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private boolean statut = true;

    @ManyToMany(mappedBy = "matieres")
    @JsonIgnore
    private List<Affectation> affectations;

    @OneToMany(mappedBy = "matiere")
    @JsonIgnore // 🔥 AJOUTE ÇA
    private List<ClasseMatiere> classeMatieres;

    @OneToMany(mappedBy = "matiere")
    @JsonIgnore // 🔥 AJOUTE ÇA
    private List<Note> note;
}
