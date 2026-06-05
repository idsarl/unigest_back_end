package gestion.scolaire.dto;

import gestion.scolaire.model.TypeNote;
import gestion.scolaire.model.TypePeriode;
import lombok.Data;

@Data
public class NoteBatchRequest {

    private Long etudiantId;
    private Long affectationId;
    private Long matiereId;
    private Double valeur;
    private TypeNote type;
    private Integer periode;
    private TypePeriode typePeriode;
    private java.time.LocalDate dateEvaluation;
}