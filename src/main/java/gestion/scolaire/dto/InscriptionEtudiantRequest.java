package gestion.scolaire.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class InscriptionEtudiantRequest {

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String password;
    private LocalDate dateNaissance;
    private Long classeId;
    private Long anneeId;
    private Long parentId;
    private double montantReduction;
    private String motifReduction;
}
