package gestion.scolaire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestion.scolaire.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.expediteur.id = :u1Id AND m.destinataire.id = :u2Id) " +
           "OR (m.expediteur.id = :u2Id AND m.destinataire.id = :u1Id) ORDER BY m.dateEnvoi ASC")
    List<Message> findChatHistory(@Param("u1Id") Long u1Id, @Param("u2Id") Long u2Id);
}
