package gestion.scolaire.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


import lombok.Data;

@Data
@Entity
public class LigneBulletin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore
    private Bulletin bulletin;

    @ManyToOne(optional = false)
    private Matiere matiere;

    private double moyenneMatiere;

    private double coefficient;

    private String appreciation;
}