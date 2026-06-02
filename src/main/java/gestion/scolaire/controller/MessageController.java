package gestion.scolaire.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gestion.scolaire.model.Message;
import gestion.scolaire.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Gestion de la messagerie parents-enseignants")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Récupérer l'historique de discussion entre deux utilisateurs")
    @GetMapping("/history")
    public ResponseEntity<List<Message>> getChatHistory(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        return ResponseEntity.ok(messageService.getChatHistory(user1Id, user2Id));
    }

    @Operation(summary = "Envoyer un message")
    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @RequestParam Long expediteurId,
            @RequestParam Long destinataireId,
            @RequestParam String contenu) {
        return ResponseEntity.ok(messageService.sendMessage(expediteurId, destinataireId, contenu));
    }
}
