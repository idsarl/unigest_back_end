package gestion.scolaire.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import gestion.scolaire.dto.ConversationDTO;
import gestion.scolaire.dto.DocumentType;
import gestion.scolaire.dto.MessageDTO;
import gestion.scolaire.model.Affectation;
import gestion.scolaire.model.Enseignant;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.model.Medias;
import gestion.scolaire.model.Message;
import gestion.scolaire.model.Parent;
import gestion.scolaire.model.Utilisateur;
import gestion.scolaire.repository.AffectationRepository;
import gestion.scolaire.repository.EnseignantRepository;
import gestion.scolaire.repository.EtudiantRepository;
import gestion.scolaire.repository.MessageRepository;
import gestion.scolaire.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MediasService mediasService;

    public List<ConversationDTO> getConversations(Long userId) {
        List<Message> messages = messageRepository.findAllByUserIdOrderByDateDesc(userId);
        Map<Long, ConversationDTO> byContact = new LinkedHashMap<>();

        for (Message message : messages) {
            Utilisateur contact = message.getExpediteur().getId().equals(userId)
                    ? message.getDestinataire()
                    : message.getExpediteur();

            if (contact == null || byContact.containsKey(contact.getId())) {
                continue;
            }

            ConversationDTO dto = new ConversationDTO();
            dto.setContactId(contact.getId());
            dto.setContactNom(contact.getNom());
            dto.setContactPrenom(contact.getPrenom());
            dto.setLastMessage(message.getContenu() != null && !message.getContenu().isBlank()
                    ? message.getContenu()
                    : "📎 Fichier(s)");
            dto.setDateEnvoi(message.getDateEnvoi());
            int unread = messageRepository.countUnreadMessages(userId, contact.getId());
            dto.setUnreadCount(unread);
            byContact.put(contact.getId(), dto);
        }


        return byContact.values().stream()
                .sorted(Comparator.comparing(
                        ConversationDTO::getDateEnvoi,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<MessageDTO> getMessages(Long userId, Long contactId) {
        return messageRepository.findConversation(userId, contactId).stream()
                .map(m -> toDto(m, userId))
                .toList();
    }

    public void markConversationAsRead(Long userId, Long contactId) {
        messageRepository.markAsRead(userId, contactId);
    }

    public MessageDTO sendMessage(Long expediteurId, Long destinataireId, String contenu, List<MultipartFile> fichiers) {
        System.out.println("=== MessageService.sendMessage ===");
        System.out.println("ExpediteurId: " + expediteurId);
        System.out.println("DestinataireId: " + destinataireId);

        Utilisateur expediteur = utilisateurRepository.findById(expediteurId)
                .orElseThrow(() -> new RuntimeException("Expéditeur introuvable"));
        Utilisateur destinataire = utilisateurRepository.findById(destinataireId)
                .orElseThrow(() -> new RuntimeException("Destinataire introuvable"));

        Message message = new Message();
        message.setExpediteur(expediteur);
        message.setDestinataire(destinataire);
        message.setContenu(contenu != null ? contenu.trim() : "");
        message.setDateEnvoi(LocalDateTime.now());
        message.setFichiers(new ArrayList<>());

        System.out.println("Saving initial message...");
        Message saved = messageRepository.save(message);
        System.out.println("Initial message saved with ID: " + saved.getId());

        if (fichiers != null && !fichiers.isEmpty()) {
            System.out.println("Processing " + fichiers.size() + " files...");
            for (MultipartFile fichier : fichiers) {
                if (fichier != null && !fichier.isEmpty()) {
                    System.out.println("  Processing file: " + fichier.getOriginalFilename());
                    Medias media = new Medias();
                    media.setType(DocumentType.MESSAGE);
                    media.setReferenceId(saved.getId());
                    try {
                        Medias savedMedia = mediasService.create(media, fichier);
                        System.out.println("  Saved media with ID: " + savedMedia.getIdMedia() + ", URL: " + savedMedia.getFichierUrl());
                        saved.getFichiers().add(savedMedia);
                    } catch (Exception e) {
                        System.out.println("  Error saving media: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("Erreur lors de l'upload du fichier : " + e.getMessage(), e);
                    }
                }
            }
            System.out.println("Saving message with files...");
            saved = messageRepository.save(saved);
            System.out.println("Message with files saved, files count: " + saved.getFichiers().size());
        }

        MessageDTO dtoDestinataire = toDto(saved, destinataireId);
        messagingTemplate.convertAndSend(
                "/topic/messages/" + destinataireId.toString(),
                dtoDestinataire
        );

        return toDto(saved, expediteurId);
    }



    private MessageDTO toDto(Message message, Long currentUserId) {
        System.out.println("=== MessageService.toDto ===");
        System.out.println("Message ID: " + message.getId());
        System.out.println("Files count: " + (message.getFichiers() != null ? message.getFichiers().size() : 0));

        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setExpediteurId(message.getExpediteur().getId());
        dto.setDestinataireId(message.getDestinataire().getId());
        dto.setContenu(message.getContenu());
        dto.setDateEnvoi(message.getDateEnvoi());
        dto.setMine(message.getExpediteur().getId().equals(currentUserId));

        if (message.getFichiers() != null) {
            List<String> fileUrls = new ArrayList<>();
            for (Medias media : message.getFichiers()) {
                System.out.println("  Media: " + media.getIdMedia() + ", fichierUrl: " + media.getFichierUrl());
                if (media.getFichierUrl() != null) {
                    fileUrls.add(media.getFichierUrl());
                }
            }
            System.out.println("  Setting " + fileUrls.size() + " file URLs in DTO");
            dto.setFichiers(fileUrls);
        }

        return dto;
    }
}
