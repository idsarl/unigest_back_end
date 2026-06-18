package gestion.scolaire.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EmploiDuTempsResponse {

    private Long id;

    private String classe;

    private String matiere;

    private String enseignant;

    private String salle;

    private String jour;

    private String heureDebut;

    private String heureFin;

    private String couleur;

    private String periodicite;

    private LocalDate dateDebut;

    private LocalDate dateFin;
}