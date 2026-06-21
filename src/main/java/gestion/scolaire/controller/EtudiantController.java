package gestion.scolaire.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gestion.scolaire.dto.InscriptionEtudiantRequest;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.service.DossierEtudiantPdfService;
import gestion.scolaire.service.EtudiantService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final DossierEtudiantPdfService dossierPdfService;

    @PostMapping
    public ResponseEntity<Etudiant> creerEtudiant(@RequestBody Etudiant etudiant) {
        return ResponseEntity.ok(etudiantService.creerEtudiant(etudiant));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Etudiant> modifierEtudiant(@PathVariable Long id,
            @RequestBody Etudiant data) {
        return ResponseEntity.ok(etudiantService.modifierEtudiant(id, data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerEtudiant(@PathVariable Long id) {
        etudiantService.supprimerEtudiant(id);
        return ResponseEntity.ok("Étudiant supprimé");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Etudiant> getEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(etudiantService.getEtudiant(id));
    }

    @GetMapping
    public ResponseEntity<List<Etudiant>> listerEtudiants() {
        return ResponseEntity.ok(etudiantService.listerEtudiants());
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<Etudiant>> getEtudiantsParClasse(@PathVariable Long classeId) {
        return ResponseEntity.ok(etudiantService.getEtudiantsParClasse(classeId));
    }

    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Etudiant> getEtudiantParMatricule(@PathVariable String matricule) {
        return ResponseEntity.ok(etudiantService.getEtudiantParMatricule(matricule));
    }

    @PostMapping("/inscrire")
    public ResponseEntity<Etudiant> creerEtudiantAvecInscription(
            @RequestBody InscriptionEtudiantRequest request) {
        return ResponseEntity.ok(etudiantService.creerEtudiantAvecInscription(request));
    }

    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exporterDossierPdf(@PathVariable Long id) {
        byte[] pdf = dossierPdfService.generer(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dossier-etudiant-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
