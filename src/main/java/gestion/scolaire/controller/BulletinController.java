package gestion.scolaire.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gestion.scolaire.model.Bulletin;
import gestion.scolaire.model.TypePeriode;
import gestion.scolaire.service.BulletinPdfService;
import gestion.scolaire.service.BulletinService;
import gestion.scolaire.service.BulletinWordService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bulletins")
@RequiredArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;
    private final BulletinPdfService bulletinPdfService;
    private final BulletinWordService bulletinWordService;

    @PostMapping
    public ResponseEntity<Bulletin> genererBulletin(
            @RequestParam Long etudiantId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bulletinService.genererBulletin(etudiantId, periode, typePeriode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bulletin> getBulletin(@PathVariable Long id) {
        return ResponseEntity.ok(bulletinService.getBulletin(id));
    }

    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<List<Bulletin>> getBulletinsEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(bulletinService.getBulletinsEtudiant(etudiantId));
    }

    @GetMapping("/etudiant/{etudiantId}/periode")
    public ResponseEntity<Bulletin> getBulletinPeriode(
            @PathVariable Long etudiantId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                bulletinService.getBulletinEtudiantPeriode(etudiantId, periode, typePeriode));
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<Bulletin>> getBulletinsClasse(@PathVariable Long classeId) {
        return ResponseEntity.ok(bulletinService.getBulletinsClasse(classeId));
    }

    @GetMapping("/classe/{classeId}/periode")
    public ResponseEntity<List<Bulletin>> getBulletinsClassePeriode(
            @PathVariable Long classeId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                bulletinService.getBulletinsClassePeriode(classeId, periode, typePeriode));
    }

    @PutMapping("/regenerer")
    public ResponseEntity<Bulletin> regenererBulletin(
            @RequestParam Long etudiantId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                bulletinService.regenererBulletin(etudiantId, periode, typePeriode));
    }

    /** Télécharger le PDF d'un bulletin */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable Long id) {
        byte[] bytes = bulletinPdfService.genererPdf(id);
        Bulletin b = bulletinService.getBulletin(id);
        return exportResponse(bytes, nomFichier(b, "pdf"), MediaType.APPLICATION_PDF);
    }

    /** Télécharger le document Word d'un bulletin */
    @GetMapping("/{id}/word")
    public ResponseEntity<byte[]> telechargerWord(@PathVariable Long id) {
        byte[] bytes = bulletinWordService.genererWord(id);
        Bulletin b = bulletinService.getBulletin(id);
        MediaType wordType = MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return exportResponse(bytes, nomFichier(b, "docx"), wordType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerBulletin(@PathVariable Long id) {
        bulletinService.supprimerBulletin(id);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String nomFichier(Bulletin b, String ext) {
        return String.format("bulletin_%s_%s_%s%d.%s",
                b.getEtudiant().getNom().toLowerCase().replace(" ", "_"),
                b.getEtudiant().getPrenom().toLowerCase().replace(" ", "_"),
                b.getTypePeriode().name().toLowerCase() + "_",
                b.getPeriode(),
                ext);
    }

    private ResponseEntity<byte[]> exportResponse(byte[] bytes, String filename, MediaType type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
