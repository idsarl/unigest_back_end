package gestion.scolaire.controller;

import gestion.scolaire.dto.SeanceDTO;
import gestion.scolaire.model.Seance;
import gestion.scolaire.service.SeanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/seances")
@Tag(name = "Séances", description = "Gestion des séances de cours")
public class SeanceController {

        @Autowired
        private SeanceService seanceService;

        @Operation(summary = "Démarrer une séance", description = "Crée une nouvelle séance pour une affectation")
        @PostMapping("/demarrer")
        public ResponseEntity<Seance> demarrerSeance(
                        @Parameter(description = "ID de l'affectation") @RequestParam Long affectationId,
                        @RequestParam String matiere) {

                return ResponseEntity.ok(
                                seanceService.demarrerSeance(affectationId, matiere));
        }

        @Operation(summary = "Terminer une séance")
        @PutMapping("/{seanceId}/terminer")
        public ResponseEntity<Seance> terminerSeance(
                        @PathVariable Long seanceId) {

                return ResponseEntity.ok(
                                seanceService.terminerSeance(seanceId));
        }

        @GetMapping("/jour")
        public List<SeanceDTO> getSeancesDuJour() {
                return seanceService.getSeancesDuJour();
        } 

        @Operation(summary = "Récupérer les séances du jour d'un enseignant")
        @GetMapping("/enseignant/{enseignantId}/jour")
        public ResponseEntity<List<SeanceDTO>> getSeancesDuJourParEnseignant(
                        @PathVariable Long enseignantId) {
                return ResponseEntity.ok(
                                seanceService.getSeancesDuJourParEnseignant(enseignantId));
        }

        @Operation(summary = "Récupérer le nombre d'absences lors des séances du jour d'un enseignant")
        @GetMapping("/enseignant/{enseignantId}/absences/jour")
        public ResponseEntity<Map<String, Object>> getAbsencesDuJourParEnseignant(
                        @PathVariable Long enseignantId) {
                return ResponseEntity.ok(
                                seanceService.getAbsencesDuJourParEnseignant(enseignantId));
        }

        @Operation(summary = "Récupérer la moyenne de la matière en cours ou de la prochaine matière d'un enseignant")
        @GetMapping("/enseignant/{enseignantId}/moyenne-matiere/encours")
        public ResponseEntity<Map<String, Object>> getMoyenneMatiereEnCoursParEnseignant(
                        @PathVariable Long enseignantId) {
                Map<String, Object> moyenne = seanceService.getMoyenneMatiereEnCoursParEnseignant(enseignantId);
                if (moyenne.containsKey("message")) {
                        return ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok(moyenne);
        }

        @Operation(summary = "Récupérer les séances par date")
    @GetMapping("/date")
    public ResponseEntity<List<SeanceDTO>> getSeancesParDate(
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                seanceService.getSeancesParDate(date));
    }

    @Operation(summary = "Récupérer les séances par enseignant et date")
    @GetMapping("/enseignant/{enseignantId}/date")
    public ResponseEntity<List<SeanceDTO>> getSeancesParEnseignantEtDate(
            @PathVariable Long enseignantId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                seanceService.getSeancesParEnseignantEtDate(enseignantId, date));
    }

        @Operation(summary = "Récupérer les séances par affectation")
        @GetMapping("/affectation/{affectationId}")
        public ResponseEntity<List<Seance>> getSeancesParAffectation(
                        @PathVariable Long affectationId) {

                return ResponseEntity.ok(
                                seanceService.getSeancesParAffectation(affectationId));
        }

        @Operation(summary = "Récupérer les séances par affectation et date")
        @GetMapping("/affectation/{affectationId}/date")
        public ResponseEntity<List<Seance>> getSeancesParAffectationEtDate(
                        @PathVariable Long affectationId,
                        @RequestParam LocalDate date) {

                return ResponseEntity.ok(
                                seanceService.getSeancesParAffectationEtDate(affectationId, date));
        }

        @Operation(summary = "Récupérer toutes les séances en cours")
        @GetMapping("/encours")
        public ResponseEntity<List<Seance>> getSeancesEnCours() {

                return ResponseEntity.ok(
                                seanceService.getSeancesEnCours());
        }

        @Operation(summary = "Récupérer toutes les séances")
        @GetMapping
        public ResponseEntity<List<Seance>> getSeances() {

                return ResponseEntity.ok(
                                seanceService.getSeances());
        }

        

        @Operation(summary = "Récupérer le temps restant avant la prochaine séance du jour pour un enseignant")
        @GetMapping("/enseignant/{enseignantId}/prochaine")
        public ResponseEntity<Map<String, Object>> getTempsAvantProchaineSeanceParEnseignant(
                        @PathVariable Long enseignantId) {
                Map<String, Object> prochaine = seanceService.getTempsAvantProchaineSeanceParEnseignant(enseignantId);
                if (prochaine.containsKey("message")) {
                        return ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok(prochaine);
        }

}
