package gestion.scolaire.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MessageDTO {

    private Long id;
    private Long expediteurId;
    private Long destinataireId;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private boolean mine;
    private List<String> fichiers = new ArrayList<>();
}
