package gestion.scolaire.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gestion.scolaire.model.Bulletin;
import gestion.scolaire.model.TypePeriode;
import gestion.scolaire.service.BulletinService;
import gestion.scolaire.service.BulletinPdfService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bulletins")
@RequiredArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;
    private final BulletinPdfService bulletinPdfService;

    /**
     * Générer un bulletin
     */
    @PostMapping
    public ResponseEntity<Bulletin> genererBulletin(
            @RequestParam Long etudiantId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        Bulletin bulletin = bulletinService.genererBulletin(
                etudiantId,
                periode,
                typePeriode
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bulletin);
    }

    /**
     * Récupérer un bulletin par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Bulletin> getBulletin(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                bulletinService.getBulletin(id)
        );
    }

    /**
     * Tous les bulletins d’un étudiant
     */
    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<List<Bulletin>> getBulletinsEtudiant(
            @PathVariable Long etudiantId) {

        return ResponseEntity.ok(
                bulletinService.getBulletinsEtudiant(etudiantId)
        );
    }

    /**
     * Bulletin d’un étudiant pour une période donnée
     */
    @GetMapping("/etudiant/{etudiantId}/periode")
    public ResponseEntity<Bulletin> getBulletinPeriode(
            @PathVariable Long etudiantId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                bulletinService.getBulletinEtudiantPeriode(
                        etudiantId,
                        periode,
                        typePeriode
                )
        );
    }

    /**
     * Bulletins d’une classe (année active)
     */
    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<Bulletin>> getBulletinsClasse(
            @PathVariable Long classeId) {

        return ResponseEntity.ok(
                bulletinService.getBulletinsClasse(classeId)
        );
    }

    /**
     * Bulletins d’une classe par période
     */
    @GetMapping("/classe/{classeId}/periode")
    public ResponseEntity<List<Bulletin>> getBulletinsClassePeriode(
            @PathVariable Long classeId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                bulletinService.getBulletinsClassePeriode(
                        classeId,
                        periode,
                        typePeriode
                )
        );
    }

    /**
     * Régénérer un bulletin
     */
    @PutMapping("/regenerer")
    public ResponseEntity<Bulletin> regenererBulletin(
            @RequestParam Long etudiantId,
            @RequestParam Integer periode,
            @RequestParam TypePeriode typePeriode) {

        return ResponseEntity.ok(
                bulletinService.regenererBulletin(
                        etudiantId,
                        periode,
                        typePeriode
                )
        );
    }

    /**
     * Télécharger le PDF d'un bulletin
     * GET /api/bulletins/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable Long id) {

        byte[] pdfBytes = bulletinPdfService.genererPdf(id);

        Bulletin bulletin = bulletinService.getBulletin(id);
        String nomFichier = String.format("bulletin_%s_%s_%s%d.pdf",
                bulletin.getEtudiant().getNom().toLowerCase().replace(" ", "_"),
                bulletin.getEtudiant().getPrenom().toLowerCase().replace(" ", "_"),
                bulletin.getTypePeriode().name().toLowerCase() + "_",
                bulletin.getPeriode());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", nomFichier);
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Supprimer un bulletin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerBulletin(
            @PathVariable Long id) {

        bulletinService.supprimerBulletin(id);

        return ResponseEntity.noContent().build();
    }
}