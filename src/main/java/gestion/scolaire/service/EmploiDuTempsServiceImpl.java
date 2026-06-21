package gestion.scolaire.service;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
import gestion.scolaire.dto.EmploiDuTempsAvecSeance;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.EmploiDuTemps;
import gestion.scolaire.repository.EmploiDuTempsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmploiDuTempsServiceImpl implements EmploiDuTempsService {

    private final EmploiDuTempsRepository repository;
    private final AnneeScolaireService anneeScolaireService;
    private final gestion.scolaire.repository.AffectationRepository affectationRepository;
    private final gestion.scolaire.repository.SeanceRepository seanceRepository;

    // @Override
    // public EmploiDuTemps save(EmploiDuTemps dto) {
    //     AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
    //     EmploiDuTemps em = new EmploiDuTemps();
    //     em.setActif(dto.isActif());
    //     em.setAnneeScolaire(anneeActive);
    //     em.setClasse(dto.getClasse());
    //     em.setCouleur(dto.getCouleur());
    //     em.setDateDebut(dto.getDateDebut());
    //     em.setDateFin(dto.getDateFin());
    //     em.setDescription(dto.getDescription());
    //     em.setEnseignant(dto.getEnseignant());
    //     em.setHeureDebut(dto.getHeureDebut());
    //     em.setHeureFin(dto.getHeureFin());
    //     em.setJours(dto.getJours());
    //     em.setMatiere(dto.getMatiere());
    //     em.setPeriodicite(dto.getPeriodicite());
    //     em.setType(dto.getType());

    //     verifierConflit(dto);
    //     return repository.save(dto);
    // }@Override
public EmploiDuTemps save(EmploiDuTemps dto) {
    AnneeScolaire anneeActive = anneeScolaireService.getAnneeActive();
    EmploiDuTemps em = new EmploiDuTemps();
    em.setActif(dto.isActif());
    em.setAnneeScolaire(anneeActive);
    em.setType(dto.getType());
    em.setJours(dto.getJours());
    em.setHeureDebut(dto.getHeureDebut());
    em.setHeureFin(dto.getHeureFin());
    em.setDateDebut(dto.getDateDebut());
    em.setDateFin(dto.getDateFin());
    em.setPeriodicite(dto.getPeriodicite());
    em.setDescription(dto.getDescription());
    em.setCouleur(dto.getCouleur());

    // Champs requis seulement pour les COURS
    if (dto.getType() == TypeEmploi.COURS) {
        em.setClasse(dto.getClasse());
        em.setEnseignant(dto.getEnseignant());
        em.setMatiere(dto.getMatiere());
        verifierConflit(dto);
    }

    return repository.save(em);
}
 

    @Override
    public EmploiDuTemps update(Long id, EmploiDuTemps dto) {
        EmploiDuTemps old = getById(id);
        old.setClasse(dto.getClasse());
        old.setEnseignant(dto.getEnseignant());
        old.setMatiere(dto.getMatiere());
        old.setJours(dto.getJours());
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
    public List<EmploiDuTemps> getAll() {
        return repository.findByActifTrue();
    }

    @Override
    public List<EmploiDuTemps> getAllValidate(LocalDate today) {
        return repository.findAllValides(today);
    }

    @Override
    public EmploiDuTemps getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Introuvable"));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<EmploiDuTemps> getByClasse(Long classeId) {
        return repository.findByClasseId(classeId);
    }

    @Override
    public List<EmploiDuTemps> getByEnseignantAndDate(Long enseignantId, LocalDate date) {
        System.out.println("=== getByEnseignantAndDate(" + enseignantId + ", " + date + ") ===");
        gestion.scolaire.dto.JourSemaine jourSemaine = gestion.scolaire.dto.JourSemaine.values()[date.getDayOfWeek().getValue() - 1];
        System.out.println("Jour de la semaine: " + jourSemaine);
        
        // First, log all emplois for this enseignant
        List<EmploiDuTemps> allForEnseignant = repository.findByEnseignantId(enseignantId);
        System.out.println("Tous les emplois pour cet enseignant: " + allForEnseignant.size());
        for (EmploiDuTemps e : allForEnseignant) {
            System.out.println("- ID: " + e.getId() + 
                               ", Matière: " + (e.getMatiere() != null ? e.getMatiere().getNom() : "null") + 
                               ", Classe: " + (e.getClasse() != null ? e.getClasse().getNom() : "null") + 
                               ", Jours: " + e.getJours() + 
                               ", Heure début: " + e.getHeureDebut() + 
                               ", Heure fin: " + e.getHeureFin() + 
                               ", Date début: " + e.getDateDebut() + 
                               ", Date fin: " + e.getDateFin() + 
                               ", Actif: " + e.isActif());
        }
        
        List<EmploiDuTemps> result = repository.findAllValidesByDateAndJour(enseignantId, date, jourSemaine);
        System.out.println("Résultat après filtrage: " + result.size() + " emplois du temps trouvés");
        return result;
    }

    @Override
    public List<EmploiDuTempsAvecSeance> getByEnseignantAndDateAvecSeances(Long enseignantId, LocalDate date) {
        List<EmploiDuTemps> emplois = getByEnseignantAndDate(enseignantId, date);
        List<gestion.scolaire.model.Seance> seancesDuJour = seanceRepository.findByAffectationEnseignantIdAndDate(enseignantId, date);

        return emplois.stream().map(e -> {
            EmploiDuTempsAvecSeance dto = new EmploiDuTempsAvecSeance();
            dto.setEmploiDuTemps(e);
            
            if (e.getType() == TypeEmploi.COURS) {
                // Find affectation
                java.util.Optional<gestion.scolaire.model.Affectation> affectation = affectationRepository.findByClasseAndEnseignantAndMatiere(
                        e.getClasse().getId(), enseignantId, e.getMatiere().getNom());
                
                affectation.ifPresent(a -> {
                    dto.setAffectationId(a.getId());
                    // Find seance matching this emploi du temps heureDebut
                    java.util.Optional<gestion.scolaire.model.Seance> seanceOpt = seancesDuJour.stream()
                        .filter(s -> s.getAffectation().getId().equals(a.getId()) && 
                                     s.getHeureDebut().equals(e.getHeureDebut()))
                        .findFirst();
                    seanceOpt.ifPresent(dto::setSeance);
                });
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private void verifierConflit(EmploiDuTemps dto) {

        if (dto.getType() != TypeEmploi.COURS) {
            return;
        }

        List<EmploiDuTemps> emplois = repository.findByClasseId(dto.getClasse().getId());

        boolean conflit = emplois.stream()

                .filter(e -> dto.getId() == null
                        || !e.getId().equals(dto.getId()))

                .filter(e -> e.getType() == TypeEmploi.COURS)

                .anyMatch(e ->

                e.getJours().stream()
                        .anyMatch(dto.getJours()::contains)

                        && dto.getHeureDebut().isBefore(e.getHeureFin())

                        && dto.getHeureFin().isAfter(e.getHeureDebut())

                );

        if (conflit) {
            throw new RuntimeException(
                    "Conflit horaire détecté pour cette classe");
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

            String[] columns = { "Jour", "Heure Début", "Heure Fin", "Matière", "Enseignant", "Salle" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (EmploiDuTemps e : emplois) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0)
                        .setCellValue(
                                e.getJours()
                                        .stream()
                                        .map(Enum::name)
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse(""));
                row.createCell(1).setCellValue(e.getHeureDebut().toString());
                row.createCell(2).setCellValue(e.getHeureFin().toString());
                row.createCell(3).setCellValue(e.getMatiere().getNom());
                row.createCell(4).setCellValue(e.getEnseignant().getNom() + " " + e.getEnseignant().getPrenom());
            }

            for (int i = 0; i < columns.length; i++)
                sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur Excel: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportPdf(Long classeId) {

        Document document = new Document();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    16);

            Paragraph title = new Paragraph(
                    "EMPLOI DU TEMPS",
                    titleFont);

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);

            table.setWidthPercentage(100);

            addTableHeader(table);

            List<EmploiDuTemps> emplois = repository.findByClasseId(classeId);

            emplois.sort(
                    Comparator.comparing(
                            EmploiDuTemps::getHeureDebut));

            for (EmploiDuTemps e : emplois) {

                // =========================
                // RECREATION / PAUSE
                // =========================

                if (e.getType() == TypeEmploi.RECREATION
                        ||
                        e.getType() == TypeEmploi.PAUSE) {

                    PdfPCell pauseCell = new PdfPCell(
                            new Phrase(
                                    "☕ RÉCRÉATION : "
                                            + e.getHeureDebut()
                                            + " - "
                                            + e.getHeureFin()));

                    pauseCell.setColspan(6);

                    pauseCell.setHorizontalAlignment(
                            Element.ALIGN_CENTER);

                    pauseCell.setBackgroundColor(
                            Color.ORANGE);

                    pauseCell.setPadding(10);

                    table.addCell(pauseCell);

                    continue;
                }

                // =========================
                // COURS NORMAL
                // =========================

                // table.addCell(
                // e.getJour().toString());

                table.addCell(
                        e.getHeureDebut().toString());

                table.addCell(
                        e.getHeureFin().toString());

                table.addCell(
                        e.getMatiere() != null
                                ? e.getMatiere().getNom()
                                : "");

                table.addCell(
                        e.getEnseignant() != null
                                ? e.getEnseignant().getNom()
                                : "");

            }

            document.add(table);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erreur PDF",
                    e);
        }
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = { "Jour", "Début", "Fin", "Matière", "Prof", "Salle" };

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