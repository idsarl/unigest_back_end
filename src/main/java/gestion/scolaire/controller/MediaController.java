package gestion.scolaire.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import gestion.scolaire.model.Medias;
import gestion.scolaire.repository.MediasRepository;
import gestion.scolaire.service.FileUploade;
import gestion.scolaire.service.MediasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import gestion.scolaire.dto.DocumentType;

@RestController
@RequestMapping("/api/medias")
public class MediaController {

    private final MediasService jService;
    private final MediasRepository jRepository;
    private final FileUploade fileUpload;

    public MediaController(MediasService jService, MediasRepository jRepository,
            FileUploade fileUpload) {
        this.jService = jService;
        this.jRepository = jRepository;
        this.fileUpload = fileUpload;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter un media")
    public ResponseEntity<Medias> create(
            @RequestParam("medias") String j,
            @RequestParam(value = "fichier", required = false) MultipartFile fichier) throws Exception {

        Medias m;

        try {
            m = new JsonMapper().readValue(j, Medias.class);
        } catch (JsonProcessingException e) {
            throw new Exception("Erreur JSON: " + e.getMessage());
        }

        return new ResponseEntity<>(jService.create(m, fichier), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Medias>> getByType(
            @RequestParam DocumentType type,
            @RequestParam Long referenceId) {

        return ResponseEntity.ok(
                jService.getByTypeAndReference(type, referenceId));
    }

    @GetMapping({ "/{id}/fichier", "/{id}/fichier/{fileName:.+}" })
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) {

        Medias m = jRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Media introuvable"));

        if (m.getFichier() == null || m.getFichier().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] fileBytes;
            try {
                fileBytes = fileUpload.getFileByName(m.getFichier());
            } catch (Exception e) {
                // Fallback to local storage if FTP fails or not configured
                java.nio.file.Path localPath = java.nio.file.Paths.get(System.getProperty("user.home"), "unigest", m.getFichier());
                fileBytes = java.nio.file.Files.readAllBytes(localPath);
            }

            if (fileBytes == null || fileBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(detectContentType(m.getFichier()))
                    .body(fileBytes);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private MediaType detectContentType(String imageName) {
        String[] parts = imageName.split("\\.");
        if (parts.length > 1) {
            String extension = parts[parts.length - 1].toLowerCase();
            switch (extension) {
                case "jpg":
                case "jpeg":
                    return MediaType.IMAGE_JPEG;
                case "png":
                    return MediaType.IMAGE_PNG;
                case "gif":
                    return MediaType.IMAGE_GIF;
                // Ajoutez d'autres cas pour les types de contenu supplémentaires si nécessaire
                default:
                    break;
            }
        }
        // Par défaut, retourner MediaType.APPLICATION_OCTET_STREAM
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un fichier")
    public ResponseEntity<Void> deleteMicroProjet(@PathVariable Long id) {
        jService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
