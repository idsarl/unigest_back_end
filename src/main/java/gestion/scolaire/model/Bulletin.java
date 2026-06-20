package gestion.scolaire.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;


import lombok.Data;

@Data
@Entity
public class Bulletin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Etudiant etudiant;

    @ManyToOne(optional = false)
    private Classe classe;

    @ManyToOne(optional = false)
    private AnneeScolaire anneeScolaire;

    private Integer periode;

    @Enumerated(EnumType.STRING)
    private TypePeriode typePeriode;

    private double moyenneGenerale;

    private Integer rang;

    private String appreciation;

    private String pdfUrl;

    private LocalDate dateGeneration;

    /** Note de conduite (optionnelle, sur 20) */
    private Double noteConduite;

    @OneToMany(mappedBy = "bulletin", cascade = CascadeType.ALL)
    private List<LigneBulletin> lignes;
}
