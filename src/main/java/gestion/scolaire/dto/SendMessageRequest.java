package gestion.scolaire.dto;

import lombok.Data;

@Data
public class SendMessageRequest {

    private Long destinataireId;
    private String contenu;
}
