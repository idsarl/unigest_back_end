package gestion.scolaire.service;
import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

// Utilisation de OpenPDF (plus compatible avec Spring Boot récent) 
// ou iText (selon ta préférence, les imports ci-dessous sont pour iText)
// REMPLACE CEUX-LÀ
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import java.awt.Color; // OpenPDF utilise souvent java.awt.Color pour les couleurs

import gestion.scolaire.dto.TypeEmploi;
import gestion.scolaire.model.EmploiDuTemps;
import gestion.scolaire.repository.EmploiDuTempsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmploiDuTempsServiceImpl implements EmploiDuTempsService {

    private final EmploiDuTempsRepository repository;

    @Override
    public EmploiDuTemps save(EmploiDuTemps dto) {
        verifierConflit(dto);
        return repository.save(dto);
    }

    @Override
    public EmploiDuTemps update(Long id, EmploiDuTemps dto) {
        EmploiDuTemps old = getById(id);
        old.setClasse(dto.getClasse());
        old.setEnseignant(dto.getEnseignant());
        old.setMatiere(dto.getMatiere());
        old.setSalle(dto.getSalle());
        old.setJour(dto.getJour());
        old.setHeureDebut(dto.getHeureDebut());
        old.setHeureFin(dto.getHeureFin());
        old.setPeriodicite(dto.getPeriodicite());
        old.setDateDebut(dto.getDateDebut());
        old.setDateFin(dto.getDateFin());
        old.setDescription(dto.getDescription());
        old.setType(dto.getType());
        verifierConflit(old);
        return repository.save(old);
    }

    @Override
    public List<EmploiDuTemps> getAll() { return repository.findAll(); }

    @Override
    public EmploiDuTemps getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Introuvable"));
    }


    @Override
    public void delete(Long id) { repository.deleteById(id); }

    @Override
    public List<EmploiDuTemps> getByClasse(Long classeId) {
        return repository.findByClasseId(classeId);
    }

    // private void verifierConflit(EmploiDuTemps dto) {
    //     List<EmploiDuTemps> emplois = repository.findByClasseId(dto.getClasse().getId());
    //     boolean conflit = emplois.stream()
    //         .filter(e -> !e.getId().equals(dto.getId())) // Ignorer soi-même lors de l'update
    //         .anyMatch(e ->
    //             e.getJour() == dto.getJour() &&
    //             dto.getHeureDebut().isBefore(e.getHeureFin()) &&
    //             dto.getHeureFin().isAfter(e.getHeureDebut())
    //     );
    //     if (conflit) throw new RuntimeException("Conflit horaire détecté pour cette classe");
    // }
    private void verifierConflit(EmploiDuTemps dto) {

    // Ignorer les pauses/récréations
    if (dto.getType() != TypeEmploi.COURS) {
        return;
    }

    List<EmploiDuTemps> emplois =
        repository.findByClasseId(dto.getClasse().getId());

    boolean conflit = emplois.stream()
        .filter(e -> !e.getId().equals(dto.getId()))
        .filter(e -> e.getType() == TypeEmploi.COURS)
        .anyMatch(e ->
            e.getJour() == dto.getJour() &&
            dto.getHeureDebut().isBefore(e.getHeureFin()) &&
            dto.getHeureFin().isAfter(e.getHeureDebut())
        );

    if (conflit) {
        throw new RuntimeException(
            "Conflit horaire détecté pour cette classe"
        );
    }
}

    @Override
    public byte[] exportExcel(Long classeId) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            List<EmploiDuTemps> emplois = repository.findByClasseId(classeId);
            Sheet sheet = workbook.createSheet("Emploi du temps");

            // Style d'entête
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] columns = {"Jour", "Heure Début", "Heure Fin", "Matière", "Enseignant", "Salle"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (EmploiDuTemps e : emplois) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getJour().toString());
                row.createCell(1).setCellValue(e.getHeureDebut().toString());
                row.createCell(2).setCellValue(e.getHeureFin().toString());
                row.createCell(3).setCellValue(e.getMatiere().getNom());
                row.createCell(4).setCellValue(e.getEnseignant().getNom() + " " + e.getEnseignant().getPrenom());
                row.createCell(5).setCellValue(e.getSalle());
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur Excel: " + e.getMessage());
        }
    }

    // @Override
    // public byte[] exportPdf(Long classeId) {
    //     Document document = new Document();
    //     ByteArrayOutputStream out = new ByteArrayOutputStream();
    //     try {
    //         PdfWriter.getInstance(document, out);
    //         document.open();

    //         // Titre
    //         Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    //         Paragraph title = new Paragraph("EMPLOI DU TEMPS PERSONNALISÉ", titleFont);
    //         title.setAlignment(Element.ALIGN_CENTER);
    //         document.add(title);
    //         document.add(new Paragraph(" "));

    //         // Tableau (Matrice comme l'image)
    //         PdfPTable table = new PdfPTable(6); // Jour, Début, Fin, Matière, Enseignant, Salle
    //         table.setWidthPercentage(100);

    //         // Entêtes de colonnes
    //         addTableHeader(table);

    //         List<EmploiDuTemps> emplois = repository.findByClasseId(classeId);
    //         for (EmploiDuTemps e : emplois) {
    //             table.addCell(e.getJour().name());
    //             table.addCell(e.getHeureDebut().toString());
    //             table.addCell(e.getHeureFin().toString());
    //             table.addCell(e.getMatiere().getNom());
    //             table.addCell(e.getEnseignant().getNom());
    //             table.addCell(e.getSalle());
    //         }

    //         document.add(table);
    //         document.close();
    //         return out.toByteArray();
    //     } catch (Exception e) {
    //         throw new RuntimeException("Erreur PDF: " + e.getMessage());
    //     }
    // }

//     @Override
// public byte[] exportPdf(Long classeId) {
//     Document document = new Document();
//     ByteArrayOutputStream out = new ByteArrayOutputStream();
    
//     try {
//         PdfWriter.getInstance(document, out);
//         document.open();

//         // Titre stylisé
//         Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
//         Paragraph title = new Paragraph("EMPLOI DU TEMPS DES ÉPREUVES", titleFont);
//         title.setAlignment(Element.ALIGN_CENTER);
//         document.add(title);
//         document.add(new Paragraph(" ")); // Espace

//         // Création de la table (ex: 7 colonnes pour correspondre à ton image)
//         PdfPTable table = new PdfPTable(7); 
//         table.setWidthPercentage(100);

//         // Header : Dates | Série 1 | Série 2 ...
//         String[] headers = {"DATES", "T.A.L", "T.L.L", "T.S.S", "TS ECO", "TS EXP", "T.S.E"};
//         for (String h : headers) {
//             PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
//             cell.setBackgroundColor(Color.LIGHT_GRAY);
//             cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//             table.addCell(cell);
//         }

//         // Récupération des données
//         List<EmploiDuTemps> emplois = repository.findByClasseId(classeId);
        
//         // Logique de construction de la grille personnalisée
//         // Note: Tu devras boucler sur tes jours et tes créneaux horaires
//         // pour remplir les cellules vides comme sur l'image.
        
//         for (EmploiDuTemps e : emplois) {
//             table.addCell(e.getJour().toString());
//             table.addCell(e.getMatiere().getNom() + "\n" + e.getHeureDebut());
//             // ... remplir les autres cellules selon la série
//         }

//         document.add(table);
//         document.close();
//         return out.toByteArray();
//     } catch (Exception e) {
//         throw new RuntimeException("Erreur lors de la génération du PDF", e);
//     }
// }

 @Override
    public byte[] exportPdf(Long classeId) {

        Document document = new Document();

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            16
                    );

            Paragraph title =
                    new Paragraph(
                            "EMPLOI DU TEMPS",
                            titleFont
                    );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(new Paragraph(" "));

            PdfPTable table =
                    new PdfPTable(6);

            table.setWidthPercentage(100);

            addTableHeader(table);

            List<EmploiDuTemps> emplois =
                    repository.findByClasseId(classeId);

            emplois.sort(
                    Comparator.comparing(
                            EmploiDuTemps::getHeureDebut
                    )
            );

            for (EmploiDuTemps e : emplois) {

                // =========================
                // RECREATION / PAUSE
                // =========================

                if (
                        e.getType() == TypeEmploi.RECREATION
                                ||
                                e.getType() == TypeEmploi.PAUSE
                ) {

                    PdfPCell pauseCell =
                            new PdfPCell(
                                    new Phrase(
                                            "☕ RÉCRÉATION : "
                                                    + e.getHeureDebut()
                                                    + " - "
                                                    + e.getHeureFin()
                                    )
                            );

                    pauseCell.setColspan(6);

                    pauseCell.setHorizontalAlignment(
                            Element.ALIGN_CENTER
                    );

                    pauseCell.setBackgroundColor(
                            Color.ORANGE
                    );

                    pauseCell.setPadding(10);

                    table.addCell(pauseCell);

                    continue;
                }

                // =========================
                // COURS NORMAL
                // =========================

                table.addCell(
                        e.getJour().toString()
                );

                table.addCell(
                        e.getHeureDebut().toString()
                );

                table.addCell(
                        e.getHeureFin().toString()
                );

                table.addCell(
                        e.getMatiere() != null
                                ? e.getMatiere().getNom()
                                : ""
                );

                table.addCell(
                        e.getEnseignant() != null
                                ? e.getEnseignant().getNom()
                                : ""
                );

                table.addCell(
                        e.getSalle()
                );
            }

            document.add(table);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erreur PDF",
                    e
            );
        }
    }
    

   private void addTableHeader(PdfPTable table) {
    String[] headers = {"Jour", "Début", "Fin", "Matière", "Prof", "Salle"};
    
    // Création d'une police pour l'entête (optionnel mais recommandé)
    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

    for (String header : headers) {
        PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
        
        // Utilisation de java.awt.Color
        cell.setBackgroundColor(Color.LIGHT_GRAY); 
        
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        
        table.addCell(cell);
    }
}

}