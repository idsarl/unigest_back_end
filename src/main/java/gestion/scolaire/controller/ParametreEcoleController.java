package gestion.scolaire.controller;

import gestion.scolaire.model.AppreciationSeuil;
import gestion.scolaire.model.ParametreEcole;
import gestion.scolaire.service.ParametreEcoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parametres")
@RequiredArgsConstructor
public class ParametreEcoleController {

    private final ParametreEcoleService parametreEcoleService;

    // ── Paramètres école ──────────────────────────────────────────────────────

    @GetMapping("/ecole")
    public ResponseEntity<ParametreEcole> getParametres() {
        return ResponseEntity.ok(parametreEcoleService.getParametres());
    }

    @PutMapping("/ecole")
    public ResponseEntity<ParametreEcole> sauvegarderParametres(
            @RequestBody ParametreEcole dto) {
        return ResponseEntity.ok(parametreEcoleService.sauvegarderParametres(dto));
    }

    // ── Seuils d'appréciation ─────────────────────────────────────────────────

    @GetMapping("/appreciations")
    public ResponseEntity<List<AppreciationSeuil>> getSeuils() {
        return ResponseEntity.ok(parametreEcoleService.getSeuils());
    }

    @PutMapping("/appreciations")
    public ResponseEntity<List<AppreciationSeuil>> sauvegarderSeuils(
            @RequestBody List<AppreciationSeuil> seuils) {
        return ResponseEntity.ok(parametreEcoleService.sauvegarderSeuils(seuils));
    }

    @DeleteMapping("/appreciations/{id}")
    public ResponseEntity<Void> supprimerSeuil(@PathVariable Long id) {
        parametreEcoleService.supprimerSeuil(id);
        return ResponseEntity.noContent().build();
    }
}
