package gestion.scolaire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import gestion.scolaire.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
            WHERE (m.expediteur.id = :userId OR m.destinataire.id = :userId)
            ORDER BY m.dateEnvoi DESC
            """)
    List<Message> findAllByUserIdOrderByDateDesc(@Param("userId") Long userId);

    @Query("""
            SELECT m FROM Message m
            WHERE (m.expediteur.id = :userId1 AND m.destinataire.id = :userId2)
               OR (m.expediteur.id = :userId2 AND m.destinataire.id = :userId1)
            ORDER BY m.dateEnvoi ASC
            """)
    List<Message> findConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.expediteur.id = :contactId AND m.destinataire.id = :userId AND m.lu = false
            """)
    int countUnreadMessages(@Param("userId") Long userId, @Param("contactId") Long contactId);

    @Modifying
    @Query("""
            UPDATE Message m SET m.lu = true
            WHERE m.expediteur.id = :contactId AND m.destinataire.id = :userId AND m.lu = false
            """)
    void markAsRead(@Param("userId") Long userId, @Param("contactId") Long contactId);
}
