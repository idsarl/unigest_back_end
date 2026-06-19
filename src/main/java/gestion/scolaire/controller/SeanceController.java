package gestion.scolaire.controller;

import gestion.scolaire.dto.SeanceDTO;
import gestion.scolaire.model.Seance;
import gestion.scolaire.service.SeanceService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
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

        @Operation(summary = "Récupérer les séances par date")
        @GetMapping("/date")
        public ResponseEntity<List<Seance>> getSeancesParDate(
                        @RequestParam LocalDate date) {

                return ResponseEntity.ok(
                                seanceService.getSeancesParDate(date));
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
        public ResponseEntity<List<Map<String, Object>>> getSeances() {

                return ResponseEntity.ok(
                                seanceService.getSeances()
                                                .stream()
                                                .map(this::toMobileResponse)
                                                .toList());
        }

        private Map<String, Object> toMobileResponse(Seance seance) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("id", seance.getId());
                response.put("date", seance.getDate());
                response.put("heureDebut", seance.getHeureDebut());
                response.put("heureFin", seance.getHeureFin());
                response.put("statut", seance.getStatut());
                response.put("matiere", seance.getMatiere());

                Map<String, Object> affectation = new LinkedHashMap<>();
                if (seance.getAffectation() != null) {
                        affectation.put("id", seance.getAffectation().getId());

                        if (seance.getAffectation().getClasse() != null) {
                                Map<String, Object> classe = new LinkedHashMap<>();
                                classe.put("id", seance.getAffectation().getClasse().getId());
                                classe.put("nom", seance.getAffectation().getClasse().getNom());
                                affectation.put("classe", classe);
                        }

                        if (seance.getAffectation().getEnseignant() != null) {
                                Map<String, Object> enseignant = new LinkedHashMap<>();
                                enseignant.put("id", seance.getAffectation().getEnseignant().getId());
                                enseignant.put("nom", seance.getAffectation().getEnseignant().getNom());
                                enseignant.put("prenom", seance.getAffectation().getEnseignant().getPrenom());
                                affectation.put("enseignant", enseignant);
                        }
                }
                response.put("affectation", affectation);

                return response;
        }
}
