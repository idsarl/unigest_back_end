package gestion.scolaire.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gestion.scolaire.model.EmploiDuTemps;
import gestion.scolaire.service.EmploiDuTempsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/emplois-du-temps")
@RequiredArgsConstructor
public class EmploiDuTempsController {

    private final EmploiDuTempsService service;

    @PostMapping
    public ResponseEntity<?> save(@RequestBody EmploiDuTemps dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody EmploiDuTemps dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmploiDuTemps>> getAllValidate() {
        return ResponseEntity.ok(service.getAllValidate(LocalDate.now()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<?> getByClasse(@PathVariable Long classeId) {
        return ResponseEntity.ok(service.getByClasse(classeId));
    }

    @GetMapping("/enseignant/{enseignantId}/date")
    public ResponseEntity<List<EmploiDuTemps>> getByEnseignantAndDate(
            @PathVariable Long enseignantId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(service.getByEnseignantAndDate(enseignantId, date));
    }

    @GetMapping("/export/pdf/{classeId}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long classeId) {
        byte[] pdf = service.exportPdf(classeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"emploi-du-temps.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export/excel/{classeId}")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long classeId) {
        byte[] excel = service.exportExcel(classeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"emploi-du-temps.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/export/word/{classeId}")
    public ResponseEntity<byte[]> exportWord(@PathVariable Long classeId) {
        byte[] word = service.exportWord(classeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"emploi-du-temps.docx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(word);
    }
}