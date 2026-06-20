package gestion.scolaire.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConversationDTO {

    private Long contactId;
    private String contactNom;
    private String contactPrenom;
    private String lastMessage;
    private LocalDateTime dateEnvoi;
    private int unreadCount;
    private Long studentId;
    private String studentName;
}
