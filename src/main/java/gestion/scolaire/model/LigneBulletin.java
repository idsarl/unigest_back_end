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

    private double noteClasse;
    private double noteComposition;
    private double quotaClasse;
    private double quotaComposition;
    private double moyenneMatiere;
    private double coefficient;
    private String appreciation;
}