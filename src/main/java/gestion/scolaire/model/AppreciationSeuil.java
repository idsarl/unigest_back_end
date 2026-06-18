package gestion.scolaire.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Seuil d'appréciation configurable par l'établissement.
 * Ex : seuilMin=14.0 → libelle="Bien"
 * Les seuils sont évalués par ordre décroissant de seuilMin.
 */
@Data
@Entity
@Table(name = "appreciation_seuil")
public class AppreciationSeuil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Moyenne minimale (incluse) pour déclencher cette appréciation */
    private double seuilMin;

    /** Texte affiché (ex: "Très bien", "Bien", "Insuffisant"…) */
    private String libelle;
}
