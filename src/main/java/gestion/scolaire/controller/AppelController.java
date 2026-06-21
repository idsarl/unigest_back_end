package gestion.scolaire.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gestion.scolaire.dto.AppelBatchRequest;
import gestion.scolaire.dto.ClasseAttendanceSummaryDTO;
import gestion.scolaire.model.Appel;
import gestion.scolaire.model.StatutPresence;
import gestion.scolaire.service.AbsenceClassePdfService;
import gestion.scolaire.service.AppelService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appels")
@RequiredArgsConstructor
public class AppelController {

        private final AppelService          appelService;
        private final AbsenceClassePdfService absenceClassePdfService;

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
        public ResponseEntity<List<Appel>> getAppelsParEtudiant(
                        @PathVariable Long etudiantId) {

                return ResponseEntity.ok(
                                appelService.getAppelsParEtudiant(etudiantId));
        }

        /**
         * 5️⃣1️⃣ Résumé de présence pour une classe
         */
        @GetMapping("/classe/{classeId}/resume")
        public ResponseEntity<ClasseAttendanceSummaryDTO> getResumeParClasse(
                        @PathVariable Long classeId,
                        @RequestParam(required = false) Long seanceId) {

                return ResponseEntity.ok(appelService.getResumeParClasse(
                                classeId,
                                seanceId));
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

        /**
         * 1️⃣2️⃣ Export PDF absences/retards d'une classe
         */
        @GetMapping("/classe/{classeId}/export-pdf")
        public ResponseEntity<byte[]> exporterAbsencesClasse(
                        @PathVariable Long classeId,
                        @RequestParam Long anneeId) {

                byte[] pdf = absenceClassePdfService.generer(classeId, anneeId);
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                        "attachment; filename=\"absences-classe-" + classeId + ".pdf\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(pdf);
        }
}