package gestion.scolaire.dto;

import gestion.scolaire.model.EmploiDuTemps;
import gestion.scolaire.model.Seance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDuTempsAvecSeance {
    private EmploiDuTemps emploiDuTemps;
    private Seance seance; // Peut être null si pas encore créée
    private Long affectationId; // ID de l'affectation correspondante
}
