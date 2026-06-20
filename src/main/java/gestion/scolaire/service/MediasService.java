package gestion.scolaire.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import gestion.scolaire.dto.DocumentType;
import gestion.scolaire.model.Medias;
import gestion.scolaire.repository.MediasRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.nio.file.*;

@Service
public class MediasService {

    @Autowired
    private MediasRepository mediasRepository;
    @Autowired
    private CodeGenerator code;
    @Autowired
    private FileUploade fileUpload;

    public Medias create(Medias m, MultipartFile file) throws Exception {

        if (file != null && !file.isEmpty()) {

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path tempDir = Paths.get(System.getProperty("user.home"), "unigest");
            Files.createDirectories(tempDir);

            Path filePath = tempDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            try {
                fileUpload.uploadFileToFTP(filePath, fileName);
            } catch (Exception e) {
                System.out.println("FTP upload failed, keeping file locally: " + e.getMessage());
            }

            m.setFichier(fileName);
        }

        m.setDateEnregistrement(code.genereateDate());

        Medias saved = mediasRepository.save(m);

        if (file != null && !file.isEmpty()) {
            
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                originalName = "Document";
            }
            originalName = originalName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

            String url = "https://api.lyuni-gest.com/api/medias/"
                    + saved.getIdMedia()
                    + "/fichier/" + originalName;

            saved.setFichierUrl(url);
            return mediasRepository.save(saved);
        }

        return saved;
    }

    public List<Medias> getByTypeAndReference(DocumentType type, Long referenceId) {
        return mediasRepository.findByTypeAndReferenceId(type, referenceId);
    }

    public String delete(Long id) {

        Medias m = mediasRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aucun Media trouvé"));

        mediasRepository.delete(m);

        return "Supprimé avec succès";
    }
}
