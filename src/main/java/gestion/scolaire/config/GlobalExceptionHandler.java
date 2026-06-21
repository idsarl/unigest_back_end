package gestion.scolaire.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = friendlyConstraintMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", message));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        HttpStatus status = resolveStatus(ex.getMessage());
        return ResponseEntity.status(status).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Une erreur inattendue est survenue."));
    }

    private String friendlyConstraintMessage(String raw) {
        if (raw == null) return "Opération impossible : contrainte de données.";
        if (raw.contains("uk_note_unique") || raw.contains("Duplicate entry"))
            return "Cette note existe déjà pour cet étudiant sur cette période.";
        if (raw.contains("uk_bulletin") || raw.contains("bulletin"))
            return "Un bulletin existe déjà pour cet étudiant sur cette période.";
        if (raw.contains("Duplicate entry"))
            return "Cette entrée existe déjà.";
        return "Opération impossible : une donnée identique existe déjà.";
    }

    private HttpStatus resolveStatus(String msg) {
        if (msg == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        if (msg.contains("introuvable") || msg.contains("Introuvable")) return HttpStatus.NOT_FOUND;
        if (msg.contains("existe déjà") || msg.contains("déjà"))        return HttpStatus.CONFLICT;
        if (msg.contains("Aucun") || msg.contains("inscription"))       return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
