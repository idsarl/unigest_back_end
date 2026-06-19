package gestion.scolaire.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gestion.scolaire.dto.AppelBatchRequest;
import gestion.scolaire.model.Appel;
import gestion.scolaire.model.StatutPresence;
import gestion.scolaire.service.AppelService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appels")
@RequiredArgsConstructor
public class AppelController {

        private final AppelService appelService;

        /**
         * 1️⃣ Faire l'appel (marquer présence/absence/retard)
         */
        @PostMapping
        public ResponseEntity<Appel> faireAppel(
                        @RequestParam Long seanceId,
                        @RequestParam Long etudiantId,
                        @RequestParam StatutPresence statut,
                        @RequestParam(defaultValue = "0") int retard,
                        @RequestParam(required = false) String motif) {

                Appel appel = appelService.faireAppel(
                                seanceId,
                                etudiantId,
                                statut,
                                retard,
                                motif);

                return ResponseEntity.ok(appel);
        }

        @PostMapping("/batch")
        public ResponseEntity<Void> faireAppelBatch(
                        @RequestBody AppelBatchRequest request) {

                appelService.faireAppelBatch(request);

                return ResponseEntity.ok().build();
        }

        /**
         * 2️⃣ Récupérer tous les appels
         */
        @GetMapping
        public ResponseEntity<List<Appel>> getAppels() {
                return ResponseEntity.ok(appelService.getAppels());
        }

        /**
         * 3️⃣ Récupérer les appels d'une année scolaire
         */
        @GetMapping("/annee/{anneeId}")
        public ResponseEntity<List<Appel>> getAppelsParAnnee(
                        @PathVariable Long anneeId) {

                return ResponseEntity.ok(
                                appelService.getAppelsParAnnee(anneeId));
        }

        /**
         * 4️⃣ Récupérer les appels d'une séance
         */
        @GetMapping("/seance/{seanceId}")
        public ResponseEntity<List<Appel>> getAppelsParSeance(
                        @PathVariable Long seanceId) {

                return ResponseEntity.ok(
                                appelService.getAppelsParSeance(seanceId));
        }

        /**
         * 5️⃣ Récupérer les appels d'un étudiant
         */
        @GetMapping("/etudiant/{etudiantId}")
        public ResponseEntity<List<Map<String, Object>>> getAppelsParEtudiant(
                        @PathVariable Long etudiantId) {

                return ResponseEntity.ok(
                                appelService.getAppelsParEtudiant(etudiantId)
                                                .stream()
                                                .map(this::toMobileResponse)
                                                .toList());
        }

        /**
         * 6️⃣ Récupérer l'appel d'un étudiant dans une séance
         */
        @GetMapping("/seance/{seanceId}/etudiant/{etudiantId}")
        public ResponseEntity<Appel> getAppelEtudiantDansSeance(
                        @PathVariable Long seanceId,
                        @PathVariable Long etudiantId) {

                return ResponseEntity.ok(
                                appelService.getAppelEtudiantDansSeance(
                                                seanceId,
                                                etudiantId));
        }

        /**
         * 7️⃣ Modifier un appel
         */
        @PutMapping("/{appelId}")
        public ResponseEntity<Appel> modifierAppel(
                        @PathVariable Long appelId,
                        @RequestParam StatutPresence statut,
                        @RequestParam(defaultValue = "0") int retard,
                        @RequestParam(required = false) String motif) {

                Appel appel = appelService.modifierAppel(
                                appelId,
                                statut,
                                retard,
                                motif);

                return ResponseEntity.ok(appel);
        }

        /**
         * 8️⃣ Justifier une absence
         */
        @PutMapping("/{appelId}/justifier")
        public ResponseEntity<Appel> justifierAbsence(
                        @PathVariable Long appelId,
                        @RequestParam String motif) {

                Appel appel = appelService.justifierAbsence(
                                appelId,
                                motif);

                return ResponseEntity.ok(appel);
        }

        /**
         * 9️⃣ Supprimer un appel
         */
        @DeleteMapping("/{appelId}")
        public ResponseEntity<String> supprimerAppel(
                        @PathVariable Long appelId) {

                appelService.supprimerAppel(appelId);

                return ResponseEntity.ok(
                                "Appel supprimé avec succès");
        }

        /**
         * 🔟 Supprimer l'appel d'un étudiant dans une séance
         */
        @DeleteMapping("/seance/{seanceId}/etudiant/{etudiantId}")
        public ResponseEntity<String> supprimerAppelEtudiantSeance(
                        @PathVariable Long seanceId,
                        @PathVariable Long etudiantId) {

                appelService.supprimerAppelEtudiantSeance(
                                seanceId,
                                etudiantId);

                return ResponseEntity.ok(
                                "Appel supprimé pour cet étudiant dans cette séance");
        }

        /**
         * 1️⃣1️⃣ Supprimer tous les appels d'une séance
         */
        @DeleteMapping("/seance/{seanceId}")
        public ResponseEntity<String> supprimerAppelsSeance(
                        @PathVariable Long seanceId) {

                appelService.supprimerAppelsSeance(seanceId);

                return ResponseEntity.ok(
                                "Tous les appels de la séance ont été supprimés");
        }

        private Map<String, Object> toMobileResponse(Appel appel) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("id", appel.getId());
                response.put("statut", appel.getStatut());
                response.put("minutesRetard", appel.getMinutesRetard());
                response.put("motif", appel.getMotif());
                response.put("justifie", appel.isJustifie());
                response.put("dateJustification", appel.getDateJustification());

                Map<String, Object> etudiant = new LinkedHashMap<>();
                etudiant.put("id", appel.getEtudiant().getId());
                response.put("etudiant", etudiant);

                Map<String, Object> seance = new LinkedHashMap<>();
                seance.put("id", appel.getSeance().getId());
                seance.put("date", appel.getSeance().getDate());
                seance.put("heureDebut", appel.getSeance().getHeureDebut());
                seance.put("heureFin", appel.getSeance().getHeureFin());
                seance.put("matiere", appel.getSeance().getMatiere());
                response.put("seance", seance);

                return response;
        }
}
