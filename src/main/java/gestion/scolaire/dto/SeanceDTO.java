package gestion.scolaire.dto;

import lombok.Data;

@Data
public class SeanceDTO {

    private Long id;
    private String professeur;
    private String matiere;
    private String classe;
    private Long classeId;
    private String filiere;
    private String heureDebut;
    private String heureFin;
    private String statut;
}
