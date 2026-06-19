package gestion.scolaire.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import gestion.scolaire.dto.InscriptionEtudiantRequest;
import gestion.scolaire.model.Etudiant;
import gestion.scolaire.service.EtudiantService;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    @Autowired
    private EtudiantService etudiantService;

    // 1️⃣ Créer un étudiant sans inscription à une classe
    @PostMapping
    public ResponseEntity<Etudiant> creerEtudiant(@RequestBody Etudiant etudiant) {
        return ResponseEntity.ok(etudiantService.creerEtudiant(etudiant));
    }

    // 2️⃣ Modifier un étudiant
    @PutMapping("/{id}")
    public ResponseEntity<Etudiant> modifierEtudiant(@PathVariable Long id,
            @RequestBody Etudiant data) {
        return ResponseEntity.ok(etudiantService.modifierEtudiant(id, data));
    }

    // 3️⃣ Supprimer un étudiant
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerEtudiant(@PathVariable Long id) {
        etudiantService.supprimerEtudiant(id);
        return ResponseEntity.ok("Étudiant supprimé");
    }

    // 4️⃣ Récupérer un étudiant par ID
    @GetMapping("/{id}")
    public ResponseEntity<Etudiant> getEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(etudiantService.getEtudiant(id));
    }

    // 5️⃣ Lister tous les étudiants
    @GetMapping
    public ResponseEntity<List<Etudiant>> listerEtudiants() {
        return ResponseEntity.ok(etudiantService.listerEtudiants());
    }

    // 6️⃣ Récupérer les étudiants d'une classe
    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<Etudiant>> getEtudiantsParClasse(@PathVariable Long classeId) {
        return ResponseEntity.ok(etudiantService.getEtudiantsParClasse(classeId));
    }

    // 7️⃣ Récupérer un étudiant par matricule
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<Etudiant> getEtudiantParMatricule(@PathVariable String matricule) {
        return ResponseEntity.ok(etudiantService.getEtudiantParMatricule(matricule));
    }

    // 8️⃣ Créer un étudiant avec inscription à une classe et lien avec un parent
    @PostMapping("/inscrire")
    public ResponseEntity<Etudiant> creerEtudiantAvecInscription(
            @RequestBody InscriptionEtudiantRequest request) {
        
        return ResponseEntity.ok(etudiantService.creerEtudiantAvecInscription(request));
    }
}
