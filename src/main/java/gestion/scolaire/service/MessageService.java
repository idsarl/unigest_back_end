package gestion.scolaire.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gestion.scolaire.model.Message;
import gestion.scolaire.model.Utilisateur;
import gestion.scolaire.repository.MessageRepository;
import gestion.scolaire.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public List<Message> getChatHistory(Long senderId, Long receiverId) {
        return messageRepository.findChatHistory(senderId, receiverId);
    }

    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        Utilisateur sender = utilisateurRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Expéditeur non trouvé"));
        Utilisateur receiver = utilisateurRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Destinataire non trouvé"));

        Message message = new Message();
        message.setExpediteur(sender);
        message.setDestinataire(receiver);
        message.setContenu(content);
        message.setDateEnvoi(LocalDateTime.now());

        return messageRepository.save(message);
    }
}
