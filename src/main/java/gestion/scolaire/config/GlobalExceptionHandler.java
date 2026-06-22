package gestion.scolaire.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParse(HttpMessageNotReadableException ex) {
        String message = "Format de données invalide dans le corps de la requête.";
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().isEmpty() ? "inconnu"
                    : ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            Class<?> target = ife.getTargetType();

            if (target == LocalTime.class) {
                message = "Format d'heure invalide pour le champ \"" + fieldName
                        + "\". Utilisez le format HH:mm (ex: 08:30).";
            } else if (target == LocalDate.class) {
                message = "Format de date invalide pour le champ \"" + fieldName
                        + "\". Utilisez le format yyyy-MM-dd (ex: 2024-09-01).";
            } else if (target != null && target.isEnum()) {
                String accepted = Arrays.stream(target.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                message = "Valeur invalide pour le champ \"" + fieldName
                        + "\". Valeurs acceptées : " + accepted + ".";
            } else {
                message = "Valeur invalide pour le champ \"" + fieldName + "\".";
            }
        } else if (cause instanceof UnrecognizedPropertyException upe) {
            message = "Champ inconnu : \"" + upe.getPropertyName() + "\".";
        }

        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Validation échouée : " + String.join(", ", errors));
        body.put("details", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(
                Map.of("message", "Paramètre obligatoire manquant : \"" + ex.getParameterName() + "\".")
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "inconnu";
        return ResponseEntity.badRequest().body(
                Map.of("message", "Valeur invalide pour le paramètre \"" + ex.getName()
                        + "\". Type attendu : " + expected + ".")
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = friendlyConstraintMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        String msg = ex.getReason() != null ? ex.getReason() : "Ressource introuvable.";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", msg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        HttpStatus status = resolveStatus(ex.getMessage());
        return ResponseEntity.status(status).body(Map.of("message", ex.getMessage() != null
                ? ex.getMessage() : "Une erreur inattendue est survenue."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Une erreur inattendue est survenue."));
    }

    private String friendlyConstraintMessage(String raw) {
        if (raw == null) return "Opération impossible : contrainte de données.";
        if (raw.contains("uk_note_unique") || (raw.contains("note") && raw.contains("Duplicate")))
            return "Cette note existe déjà pour cet étudiant sur cette période.";
        if (raw.contains("uk_bulletin") || raw.contains("bulletin"))
            return "Un bulletin existe déjà pour cet étudiant sur cette période.";
        if (raw.contains("Duplicate entry"))
            return "Cette entrée existe déjà.";
        if (raw.contains("foreign key") || raw.contains("FOREIGN KEY"))
            return "Impossible de supprimer : cet élément est référencé par d'autres données.";
        return "Opération impossible : une donnée identique existe déjà.";
    }

    private HttpStatus resolveStatus(String msg) {
        if (msg == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        String lower = msg.toLowerCase();
        if (lower.contains("introuvable"))       return HttpStatus.NOT_FOUND;
        if (lower.contains("existe déjà") || lower.contains("déjà"))    return HttpStatus.CONFLICT;
        if (lower.contains("conflit"))           return HttpStatus.CONFLICT;
        if (lower.contains("aucun") || lower.contains("inscription") || lower.contains("invalide"))
                                                 return HttpStatus.BAD_REQUEST;
        if (lower.contains("non autorisé") || lower.contains("accès refusé"))
                                                 return HttpStatus.FORBIDDEN;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
