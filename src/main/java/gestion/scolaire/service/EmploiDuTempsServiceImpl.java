package gestion.scolaire.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Utilisation de OpenPDF (plus compatible avec Spring Boot récent) 
// ou iText (selon ta préférence, les imports ci-dessous sont pour iText)
// REMPLACE CEUX-LÀ
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import java.awt.Color;

import gestion.scolaire.dto.TypeEmploi;
import gestion.scolaire.model.AnneeScolaire;
import gestion.scolaire.model.EmploiDuTemps;
import gestion.scolaire.repository.EmploiDuTempsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmploiDuTempsServiceImpl implements EmploiDuTempsService {

    private final EmploiDuTempsRepository repository;
    private final AnneeScolaireService anneeScolaireService;

   
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
        return repository.findByActifTrueAndAnneeScolaireId(anneeScolaireService.getAnneeActive().getId());
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
        List<EmploiDuTemps> cours = repository.findByClasseIdAndAnneeScolaireId(
                classeId, anneeScolaireService.getAnneeActive().getId());
        List<EmploiDuTemps> pausesGlobales = repository.findRecreationsGlobales();
        List<EmploiDuTemps> combined = new ArrayList<>(cours);
        combined.addAll(pausesGlobales);
        return combined;
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
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4.rotate(), 25, 25, 35, 25);
            PdfWriter.getInstance(document, out);
            document.open();

            List<EmploiDuTemps> emplois = new ArrayList<>(
                    repository.findByClasseId(classeId).stream()
                            .filter(EmploiDuTemps::isActif).collect(Collectors.toList()));
            emplois.addAll(repository.findRecreationsGlobales());

            String nomClasse = emplois.isEmpty() ? "" :
                    (emplois.get(0).getClasse() != null ? emplois.get(0).getClasse().getNom() : "");

            // ── Bandeau titre ──────────────────────────────────────────────
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.WHITE);
            PdfPTable titleBar = new PdfPTable(1);
            titleBar.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Phrase(
                    "EMPLOI DU TEMPS" + (nomClasse.isEmpty() ? "" : "  —  " + nomClasse), titleFont));
            titleCell.setBackgroundColor(new Color(30, 41, 59));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setPadding(10);
            titleCell.setBorder(0);
            titleBar.addCell(titleCell);
            document.add(titleBar);
            document.add(new Paragraph(" "));

            // ── Collecte des créneaux horaires uniques ─────────────────────
            TreeMap<LocalTime, LocalTime> slots = new TreeMap<>();
            emplois.forEach(e -> {
                if (e.getHeureDebut() != null && e.getHeureFin() != null)
                    slots.put(e.getHeureDebut(), e.getHeureFin());
            });

            if (slots.isEmpty()) {
                document.add(new Paragraph("Aucun cours pour cette classe."));
                document.close();
                return out.toByteArray();
            }

            // ── Carte de cellules : "JOUR-heureDebut" → emploi ────────────
            String[] jours = {"LUNDI","MARDI","MERCREDI","JEUDI","VENDREDI","SAMEDI"};
            Map<String, EmploiDuTemps> cellMap = new LinkedHashMap<>();
            emplois.forEach(e -> {
                if (e.getJours() != null)
                    e.getJours().forEach(j -> cellMap.put(j.name() + "-" + e.getHeureDebut(), e));
            });

            // ── Grille : 1 col horaire + 6 jours ─────────────────────────
            PdfPTable grid = new PdfPTable(7);
            grid.setWidthPercentage(100);
            grid.setWidths(new float[]{13f, 14.5f, 14.5f, 14.5f, 14.5f, 14.5f, 14.5f});

            Font hFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font sFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(71,85,105));
            Font sFin   = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(148,163,184));
            Font cFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font eFont  = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(220,220,255));
            Font vFont  = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(200,200,200));

            Color headerBg = new Color(79, 70, 229);
            String[] jourLabels = {"Horaire","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"};
            for (String lbl : jourLabels) {
                PdfPCell c = new PdfPCell(new Phrase(lbl, hFont));
                c.setBackgroundColor(headerBg);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c.setPadding(6); c.setBorderColor(new Color(99,102,241));
                grid.addCell(c);
            }

            boolean alt = false;
            for (Map.Entry<LocalTime, LocalTime> slot : slots.entrySet()) {
                Color rowBg = alt ? new Color(248,250,252) : Color.WHITE; alt = !alt;

                PdfPCell hc = new PdfPCell();
                hc.setBackgroundColor(new Color(241,245,249));
                hc.setHorizontalAlignment(Element.ALIGN_CENTER);
                hc.setVerticalAlignment(Element.ALIGN_MIDDLE);
                hc.setPadding(5);
                Paragraph hp = new Paragraph(slot.getKey().toString(), sFont);
                hp.add(new Phrase("\n" + slot.getValue().toString(), sFin));
                hc.addElement(hp);
                grid.addCell(hc);

                for (String jour : jours) {
                    EmploiDuTemps cours = cellMap.get(jour + "-" + slot.getKey());
                    PdfPCell dc = new PdfPCell();
                    dc.setPadding(4); dc.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    if (cours != null) {
                        boolean isPause = cours.getType() == TypeEmploi.PAUSE
                                || cours.getType() == TypeEmploi.RECREATION;
                        if (isPause) {
                            dc.setBackgroundColor(new Color(254, 243, 199));
                            dc.setHorizontalAlignment(Element.ALIGN_CENTER);
                            Font pauseLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(146, 64, 14));
                            Font pauseSub   = FontFactory.getFont(FontFactory.HELVETICA, 7,   new Color(180, 120, 50));
                            String label = cours.getType() == TypeEmploi.RECREATION ? "Recreation" : "Pause";
                            Paragraph cp = new Paragraph(label, pauseLabel);
                            if (cours.getDescription() != null && !cours.getDescription().isBlank())
                                cp.add(new Phrase("\n" + cours.getDescription(), pauseSub));
                            dc.addElement(cp);
                        } else {
                            dc.setBackgroundColor(parseHexColor(cours.getCouleur(), new Color(99,102,241)));
                            Paragraph cp = new Paragraph();
                            cp.add(new Phrase((cours.getMatiere() != null ? cours.getMatiere().getNom() : "—") + "\n", cFont));
                            if (cours.getEnseignant() != null) {
                                String ens = cours.getEnseignant().getPrenom() + " " + cours.getEnseignant().getNom();
                                cp.add(new Phrase(ens, eFont));
                            }
                            dc.addElement(cp);
                        }
                    } else {
                        dc.setBackgroundColor(rowBg);
                        Paragraph vp = new Paragraph("—", vFont);
                        vp.setAlignment(Element.ALIGN_CENTER);
                        dc.addElement(vp);
                        dc.setHorizontalAlignment(Element.ALIGN_CENTER);
                    }
                    grid.addCell(dc);
                }
            }
            document.add(grid);

            document.add(new Paragraph(" "));
            Font footFont = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(148,163,184));
            Paragraph foot = new Paragraph(
                    "Genere le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), footFont);
            foot.setAlignment(Element.ALIGN_RIGHT);
            document.add(foot);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la generation du PDF", e);
        }
    }

    private Color parseHexColor(String hex, Color fallback) {
        if (hex == null || hex.isBlank()) return fallback;
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            return new Color(
                    Integer.parseInt(h.substring(0,2), 16),
                    Integer.parseInt(h.substring(2,4), 16),
                    Integer.parseInt(h.substring(4,6), 16));
        } catch (Exception e) { return fallback; }
    }

}