package gestion.scolaire.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gestion.scolaire.dto.NoteBatchRequest;
import gestion.scolaire.model.Note;
import gestion.scolaire.model.TypeNote;
import gestion.scolaire.model.TypePeriode;
import gestion.scolaire.service.NoteService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Ajouter une note
     */
    @PostMapping
    public ResponseEntity<Note> ajouterNote(
            @RequestParam Long etudiantId,
            @RequestParam Long affectationId,
            @RequestParam Long matiereId,
            @RequestParam double valeur,
            @RequestParam TypeNote type,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        Note note = noteService.ajouterNote(
                etudiantId,
                affectationId,
                matiereId,
                valeur,
                type,
                periode,
                typePeriode
        );

        return new ResponseEntity<>(note, HttpStatus.CREATED);
    }

    /**
     * Ajouter plusieurs notes
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Note>> ajouterNotesBatch(
            @RequestBody List<NoteBatchRequest> notes) {

        return ResponseEntity.ok(
                noteService.ajouterNotesBatch(notes)
        );
    }

    /**
     * Modifier une note
     */
    @PutMapping("/{id}")
    public ResponseEntity<Note> modifierNote(
            @PathVariable Long id,
            @RequestParam double valeur,
            @RequestParam TypeNote type) {

        return ResponseEntity.ok(
                noteService.modifierNote(id, valeur, type)
        );
    }

    /**
     * Supprimer une note
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerNote(
            @PathVariable Long id) {

        noteService.supprimerNote(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer une note
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                noteService.getNoteById(id)
        );
    }

    /**
     * Notes par classe et période
     */
    @GetMapping("/classe/periode")
    public ResponseEntity<List<Note>> getNotesParClasseEtPeriode(
            @RequestParam Long classeId,
            @RequestParam Long anneeScolaireId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                noteService.getNotesParClasseEtPeriode(
                        classeId,
                        anneeScolaireId,
                        periode,
                        typePeriode
                )
        );
    }

    /**
     * Notes d’un étudiant
     */
    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<List<Map<String, Object>>> getNotesEtudiant(
            @PathVariable Long etudiantId) {

        return ResponseEntity.ok(
                noteService.getNotesEtudiant(etudiantId)
                        .stream()
                        .map(this::toMobileResponse)
                        .toList()
        );
    }

    /**
     * Notes d’un étudiant par période
     */
    @GetMapping("/etudiant/{etudiantId}/periode")
    public ResponseEntity<List<Map<String, Object>>> getNotesEtudiantPeriode(
            @PathVariable Long etudiantId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                noteService.getNotesEtudiantPeriode(
                        etudiantId,
                        periode,
                        typePeriode
                )
                        .stream()
                        .map(this::toMobileResponse)
                        .toList()
        );
    }

    /**
     * Notes par affectation
     */
    @GetMapping("/affectation/{affectationId}")
    public ResponseEntity<List<Note>> getNotesParAffectation(
            @PathVariable Long affectationId) {

        return ResponseEntity.ok(
                noteService.getNotesParAffectation(
                        affectationId
                )
        );
    }

    /**
     * Notes par affectation et période
     */
    @GetMapping("/affectation/{affectationId}/periode")
    public ResponseEntity<List<Note>> getNotesParAffectationEtPeriode(
            @PathVariable Long affectationId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                noteService.getNotesParAffectationEtPeriode(
                        affectationId,
                        periode,
                        typePeriode
                )
        );
    }

    /**
     * Notes par période
     */
    @GetMapping("/periode")
    public ResponseEntity<List<Note>> getNotesParPeriode(
            @RequestParam Long anneeScolaireId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                noteService.getNotesParPeriode(
                        anneeScolaireId,
                        periode,
                        typePeriode
                )
        );
    }

    /**
     * Notes par période (année active)
     */
    @GetMapping("/periode/active")
    public ResponseEntity<List<Note>> getNotesParPeriodeActive(
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                noteService.getNotesParPeriodeActive(
                        periode,
                        typePeriode
                )
        );
    }

    /**
     * Moyenne étudiant
     */
    @GetMapping("/etudiant/{etudiantId}/moyenne")
    public ResponseEntity<Double> calculerMoyenneEtudiant(
            @PathVariable Long etudiantId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                noteService.calculerMoyenneEtudiant(
                        etudiantId,
                        periode,
                        typePeriode
                )
        );
    }

    private Map<String, Object> toMobileResponse(Note note) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", note.getId());
        response.put("valeur", note.getValeur());
        response.put("coefficient", note.getCoefficient());
        response.put("type", note.getType());
        response.put("periode", note.getPeriode());
        response.put("typePeriode", note.getTypePeriode());
        response.put("dateEvaluation", note.getDateEvaluation());

        Map<String, Object> etudiant = new LinkedHashMap<>();
        etudiant.put("id", note.getEtudiant().getId());
        response.put("etudiant", etudiant);

        Map<String, Object> matiere = new LinkedHashMap<>();
        matiere.put("id", note.getMatiere().getId());
        matiere.put("nom", note.getMatiere().getNom());
        response.put("matiere", matiere);

        return response;
    }
}
