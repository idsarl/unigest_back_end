package gestion.scolaire.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gestion.scolaire.dto.ConversationDTO;
import gestion.scolaire.dto.MessageDTO;
import gestion.scolaire.model.Utilisateur;
import gestion.scolaire.repository.UtilisateurRepository;
import gestion.scolaire.service.MessageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(Authentication authentication) {
        return ResponseEntity.ok(
                messageService.getConversations(resolveCurrentUserId(authentication)));
    }

    @GetMapping("/conversation/{contactId}")
    public ResponseEntity<List<MessageDTO>> getConversation(
            @PathVariable Long contactId,
            Authentication authentication) {
        return ResponseEntity.ok(
                messageService.getMessages(resolveCurrentUserId(authentication), contactId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestParam Long destinataireId,
            @RequestParam(required = false) String contenu,
            @RequestParam(required = false) List<MultipartFile> fichiers,
            Authentication authentication) {
        System.out.println("=== Backend MessageController ===");
        System.out.println("DestinataireId: " + destinataireId);
        System.out.println("Contenu: " + contenu);
        System.out.println("Fichiers count: " + (fichiers != null ? fichiers.size() : 0));
        if (fichiers != null) {
            for (MultipartFile f : fichiers) {
                System.out.println("  - " + f.getOriginalFilename() + " (" + f.getSize() + " bytes)");
            }
        }
        MessageDTO result = messageService.sendMessage(
                resolveCurrentUserId(authentication),
                destinataireId,
                contenu,
                fichiers);
        System.out.println("Result: " + result);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/conversation/{contactId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long contactId,
            Authentication authentication) {
        messageService.markConversationAsRead(resolveCurrentUserId(authentication), contactId);
        return ResponseEntity.ok().build();
    }

    private Long resolveCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        String login = authentication.getName();
        return utilisateurRepository.findByEmailOrTelephone(login, login)
                .map(Utilisateur::getId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }
}
