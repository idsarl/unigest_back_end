package gestion.scolaire.dto;

import lombok.Data;

@Data
public class ClasseAttendanceSummaryDTO {

    private Long classeId;
    private Long seanceId;
    private long effectif;
    private long present;
    private long absent;
    private long retard;
}
