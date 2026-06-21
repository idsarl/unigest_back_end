package gestion.scolaire.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

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
import gestion.scolaire.repository.ClasseRepository;
import gestion.scolaire.repository.EmploiDuTempsRepository;
import gestion.scolaire.repository.EnseignantRepository;
import gestion.scolaire.repository.MatiereRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmploiDuTempsServiceImpl implements EmploiDuTempsService {

    private final EmploiDuTempsRepository repository;
    private final AnneeScolaireService anneeScolaireService;
    private final ClasseRepository classeRepository;
    private final EnseignantRepository enseignantRepository;
    private final MatiereRepository matiereRepository;

    @Override
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

        if (dto.getType() == TypeEmploi.COURS) {
            if (dto.getClasse() != null && dto.getClasse().getId() != null)
                em.setClasse(classeRepository.getReferenceById(dto.getClasse().getId()));
            if (dto.getEnseignant() != null && dto.getEnseignant().getId() != null)
                em.setEnseignant(enseignantRepository.getReferenceById(dto.getEnseignant().getId()));
            if (dto.getMatiere() != null && dto.getMatiere().getId() != null)
                em.setMatiere(matiereRepository.getReferenceById(dto.getMatiere().getId()));
            verifierConflit(em);
        }

        return repository.save(em);
    }

    @Override
    public EmploiDuTemps update(Long id, EmploiDuTemps dto) {
        EmploiDuTemps old = getById(id);
        if (dto.getClasse() != null && dto.getClasse().getId() != null)
            old.setClasse(classeRepository.getReferenceById(dto.getClasse().getId()));
        else
            old.setClasse(null);
        if (dto.getEnseignant() != null && dto.getEnseignant().getId() != null)
            old.setEnseignant(enseignantRepository.getReferenceById(dto.getEnseignant().getId()));
        else
            old.setEnseignant(null);
        if (dto.getMatiere() != null && dto.getMatiere().getId() != null)
            old.setMatiere(matiereRepository.getReferenceById(dto.getMatiere().getId()));
        else
            old.setMatiere(null);
        old.setJours(dto.getJours());
        old.setHeureDebut(dto.getHeureDebut());
        old.setHeureFin(dto.getHeureFin());
        old.setPeriodicite(dto.getPeriodicite());
        old.setDateDebut(dto.getDateDebut());
        old.setDateFin(dto.getDateFin());
        old.setDescription(dto.getDescription());
        old.setType(dto.getType());
        old.setCouleur(dto.getCouleur());
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
        gestion.scolaire.dto.JourSemaine jourSemaine = gestion.scolaire.dto.JourSemaine.values()[date.getDayOfWeek().getValue() - 1];
        return repository.findAllValidesByDateAndJour(enseignantId, date, jourSemaine);
    }

    private void verifierConflit(EmploiDuTemps dto) {
        if (dto.getType() != TypeEmploi.COURS || dto.getClasse() == null) return;

        List<EmploiDuTemps> emplois = repository.findByClasseId(dto.getClasse().getId());

        boolean conflit = emplois.stream()
                .filter(e -> dto.getId() == null || !e.getId().equals(dto.getId()))
                .filter(e -> e.getType() == TypeEmploi.COURS)
                .anyMatch(e ->
                        e.getJours().stream().anyMatch(dto.getJours()::contains)
                                && dto.getHeureDebut().isBefore(e.getHeureFin())
                                && dto.getHeureFin().isAfter(e.getHeureDebut())
                );

        if (conflit) {
            throw new RuntimeException("Conflit horaire détecté pour cette classe");
        }
    }

    // ─── Export Excel ─────────────────────────────────────────────────────────

    @Override
    public byte[] exportExcel(Long classeId) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            List<EmploiDuTemps> emplois = repository.findByClasseId(classeId)
                    .stream().filter(EmploiDuTemps::isActif).collect(Collectors.toList());

            String nomClasse = emplois.stream()
                    .filter(e -> e.getClasse() != null)
                    .map(e -> e.getClasse().getNom())
                    .findFirst().orElse("Classe");

            Sheet sheet = workbook.createSheet("Emploi du temps");

            // Styles
            XSSFColor indigoColor = new XSSFColor(new java.awt.Color(79, 70, 229), null);
            XSSFColor whiteColor  = new XSSFColor(new java.awt.Color(255, 255, 255), null);

            XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
            XSSFFont headerFont = (XSSFFont) workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(whiteColor);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(indigoColor);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setWrapText(true);

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("EMPLOI DU TEMPS — " + nomClasse);
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            // Header row
            String[] columns = {"Jour(s)", "Heure Début", "Heure Fin", "Matière", "Enseignant", "Type"};
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 2;
            for (EmploiDuTemps e : emplois) {
                Row row = sheet.createRow(rowIdx++);

                Cell joursCell = row.createCell(0);
                joursCell.setCellValue(e.getJours().stream()
                        .map(j -> j.name().charAt(0) + j.name().substring(1).toLowerCase())
                        .reduce((a, b) -> a + ", " + b).orElse(""));
                joursCell.setCellStyle(dataStyle);

                Cell debutCell = row.createCell(1);
                debutCell.setCellValue(e.getHeureDebut() != null ? e.getHeureDebut().toString().substring(0, 5) : "");
                debutCell.setCellStyle(dataStyle);

                Cell finCell = row.createCell(2);
                finCell.setCellValue(e.getHeureFin() != null ? e.getHeureFin().toString().substring(0, 5) : "");
                finCell.setCellStyle(dataStyle);

                Cell matCell = row.createCell(3);
                matCell.setCellValue(e.getMatiere() != null ? e.getMatiere().getNom() : "—");
                matCell.setCellStyle(dataStyle);

                Cell ensCell = row.createCell(4);
                ensCell.setCellValue(e.getEnseignant() != null ?
                        e.getEnseignant().getNom() + " " + e.getEnseignant().getPrenom() : "—");
                ensCell.setCellStyle(dataStyle);

                Cell typeCell = row.createCell(5);
                typeCell.setCellValue(e.getType() != null ? e.getType().name() : "COURS");
                typeCell.setCellStyle(dataStyle);
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur Excel: " + e.getMessage(), e);
        }
    }

    // ─── Export Word ──────────────────────────────────────────────────────────

    @Override
    public byte[] exportWord(Long classeId) {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            List<EmploiDuTemps> emplois = repository.findByClasseId(classeId)
                    .stream().filter(EmploiDuTemps::isActif).collect(Collectors.toList());
            emplois.addAll(repository.findRecreationsGlobales());

            String nomClasse = emplois.stream()
                    .filter(e -> e.getClasse() != null)
                    .map(e -> e.getClasse().getNom())
                    .findFirst().orElse("Classe");

            // Title
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(14);
            titleRun.setText("EMPLOI DU TEMPS — " + nomClasse);

            XWPFParagraph datePara = doc.createParagraph();
            datePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun dateRun = datePara.createRun();
            dateRun.setFontSize(9);
            dateRun.setColor("888888");
            dateRun.setText("Généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            doc.createParagraph(); // spacing

            // Collect time slots
            TreeMap<LocalTime, LocalTime> slots = new TreeMap<>();
            emplois.forEach(e -> {
                if (e.getHeureDebut() != null && e.getHeureFin() != null)
                    slots.put(e.getHeureDebut(), e.getHeureFin());
            });

            if (slots.isEmpty()) {
                doc.createParagraph().createRun().setText("Aucun cours configuré pour cette classe.");
                doc.write(out);
                return out.toByteArray();
            }

            String[] jours = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"};
            String[] joursLabels = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};

            Map<String, EmploiDuTemps> cellMap = new LinkedHashMap<>();
            emplois.forEach(e -> {
                if (e.getJours() != null)
                    e.getJours().forEach(j -> cellMap.put(j.name() + "-" + e.getHeureDebut(), e));
            });

            // Table: header row + one row per slot, 7 columns (Horaire + 6 jours)
            XWPFTable table = doc.createTable(slots.size() + 1, 7);

            // Header row
            XWPFTableRow header = table.getRow(0);
            wordCell(header.getCell(0), "Horaire", true, "4F46E5", "FFFFFF", 9);
            for (int i = 0; i < 6; i++)
                wordCell(header.getCell(i + 1), joursLabels[i], true, "4F46E5", "FFFFFF", 9);

            // Data rows
            int rowIdx = 1;
            for (Map.Entry<LocalTime, LocalTime> slot : slots.entrySet()) {
                XWPFTableRow row = table.getRow(rowIdx++);
                String heureDebut = slot.getKey().toString().substring(0, 5);
                String heureFin   = slot.getValue().toString().substring(0, 5);
                wordCell(row.getCell(0), heureDebut + "\n" + heureFin, true, "F1F5F9", "475569", 8);

                for (int i = 0; i < 6; i++) {
                    EmploiDuTemps cours = cellMap.get(jours[i] + "-" + slot.getKey());
                    XWPFTableCell cell = row.getCell(i + 1);

                    if (cours == null) {
                        wordCell(cell, "—", false, "FFFFFF", "CBD5E1", 8);
                    } else if (cours.getType() == TypeEmploi.PAUSE || cours.getType() == TypeEmploi.RECREATION) {
                        String lbl = cours.getType() == TypeEmploi.RECREATION ? "Récréation" : "Pause";
                        wordCell(cell, lbl, true, "FEF3C7", "92400E", 8);
                    } else {
                        String mat = cours.getMatiere() != null ? cours.getMatiere().getNom() : "—";
                        String ens = cours.getEnseignant() != null ?
                                cours.getEnseignant().getPrenom() + " " + cours.getEnseignant().getNom() : "";
                        wordCellCours(cell, mat, ens);
                    }
                }
            }

            doc.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur Word: " + e.getMessage(), e);
        }
    }

    private void wordCell(XWPFTableCell cell, String text, boolean bold,
                          String bgColor, String fontColor, int fontSize) {
        cell.setColor(bgColor);
        XWPFParagraph para = cell.getParagraphs().get(0);
        para.setAlignment(ParagraphAlignment.CENTER);
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            XWPFRun run = para.createRun();
            if (i > 0) run.addBreak();
            run.setText(lines[i]);
            run.setBold(bold);
            run.setFontSize(fontSize);
            run.setColor(fontColor);
        }
    }

    private void wordCellCours(XWPFTableCell cell, String matiere, String enseignant) {
        cell.setColor("E0E7FF");
        XWPFParagraph para = cell.getParagraphs().get(0);
        para.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun matRun = para.createRun();
        matRun.setText(matiere);
        matRun.setBold(true);
        matRun.setFontSize(9);
        matRun.setColor("1E1B4B");

        if (enseignant != null && !enseignant.isBlank()) {
            XWPFRun ensRun = para.createRun();
            ensRun.addBreak();
            ensRun.setText(enseignant);
            ensRun.setBold(false);
            ensRun.setFontSize(7);
            ensRun.setColor("3730A3");
        }
    }

    // ─── Export PDF ───────────────────────────────────────────────────────────

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

            String nomClasse = emplois.stream()
                    .filter(e -> e.getClasse() != null)
                    .map(e -> e.getClasse().getNom())
                    .findFirst().orElse("");

            // Bandeau titre
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

            // Créneaux horaires uniques
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

            String[] jours = {"LUNDI","MARDI","MERCREDI","JEUDI","VENDREDI","SAMEDI"};
            Map<String, EmploiDuTemps> cellMap = new LinkedHashMap<>();
            emplois.forEach(e -> {
                if (e.getJours() != null)
                    e.getJours().forEach(j -> cellMap.put(j.name() + "-" + e.getHeureDebut(), e));
            });

            // Grille : 1 col horaire + 6 jours
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
                Paragraph hp = new Paragraph(slot.getKey().toString().substring(0,5), sFont);
                hp.add(new Phrase("\n" + slot.getValue().toString().substring(0,5), sFin));
                hc.addElement(hp);
                grid.addCell(hc);

                for (String jour : jours) {
                    EmploiDuTemps cours = cellMap.get(jour + "-" + slot.getKey());
                    PdfPCell dc = new PdfPCell();
                    dc.setPadding(4); dc.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    if (cours != null) {
                        boolean isPause = cours.getType() == TypeEmploi.PAUSE || cours.getType() == TypeEmploi.RECREATION;
                        if (isPause) {
                            dc.setBackgroundColor(new Color(254, 243, 199));
                            dc.setHorizontalAlignment(Element.ALIGN_CENTER);
                            Font pauseLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(146,64,14));
                            Font pauseSub   = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(180,120,50));
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
                    "Généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), footFont);
            foot.setAlignment(Element.ALIGN_RIGHT);
            document.add(foot);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
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
