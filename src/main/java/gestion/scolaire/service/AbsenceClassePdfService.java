package gestion.scolaire.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import gestion.scolaire.model.*;
import gestion.scolaire.repository.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbsenceClassePdfService {

    private final AppelRepository          appelRepository;
    private final ClasseRepository         classeRepository;
    private final AnneeScolaireRepository  anneeScolaireRepository;
    private final ParametreEcoleService    parametreEcoleService;

    // ── Couleurs ─────────────────────────────────────────────────────────────
    private static final Color INDIGO       = new Color(79, 70, 229);
    private static final Color INDIGO_LIGHT = new Color(224, 231, 255);
    private static final Color SLATE_50     = new Color(248, 250, 252);
    private static final Color SLATE_200    = new Color(226, 232, 240);
    private static final Color SLATE_700    = new Color(51, 65, 85);
    private static final Color WHITE        = Color.WHITE;
    private static final Color GREEN        = new Color(5, 150, 105);
    private static final Color RED          = new Color(220, 38, 38);
    private static final Color AMBER        = new Color(217, 119, 6);

    // ── Polices ───────────────────────────────────────────────────────────────
    private static final Font F_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, INDIGO);
    private static final Font F_HEADER  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, WHITE);
    private static final Font F_CELL    = FontFactory.getFont(FontFactory.HELVETICA,      8, SLATE_700);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─────────────────────────────────────────────────────────────────────────

    public byte[] generer(Long classeId, Long anneeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable"));
        AnneeScolaire annee = anneeScolaireRepository.findById(anneeId)
                .orElseThrow(() -> new RuntimeException("Année scolaire introuvable"));
        ParametreEcole ecole = parametreEcoleService.getParametres();
        List<Appel> appels   = appelRepository.findByClasseIdAndAnneeId(classeId, anneeId);

        long nbTotal    = appels.size();
        long nbAbsents  = appels.stream().filter(a -> StatutPresence.ABSENT.equals(a.getStatut())).count();
        long nbRetards  = appels.stream().filter(a -> StatutPresence.RETARD.equals(a.getStatut())).count();
        long nbPresents = appels.stream().filter(a -> StatutPresence.PRESENT.equals(a.getStatut())).count();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            ajouterEntete(doc, ecole, classe, annee);
            ajouterStats(doc, nbTotal, nbPresents, nbAbsents, nbRetards);
            ajouterSectionTitre(doc, "DÉTAIL DES APPELS");
            ajouterTableau(doc, appels);
            ajouterPiedDePage(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF absences : " + e.getMessage(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void ajouterEntete(Document doc, ParametreEcole ecole, Classe classe, AnneeScolaire annee)
            throws DocumentException {

        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(INDIGO);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(14);

        if (ecole.getNomEcole() != null && !ecole.getNomEcole().isBlank()) {
            Font fNom = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, WHITE);
            Paragraph nom = new Paragraph(ecole.getNomEcole().toUpperCase(), fNom);
            nom.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(nom);
        }

        Font fTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, WHITE);
        Paragraph titre = new Paragraph("RAPPORT D'ABSENCES ET RETARDS", fTitre);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingBefore(4);
        cell.addElement(titre);

        Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 9, INDIGO_LIGHT);
        Paragraph sub = new Paragraph(
                "Classe : " + classe.getNom() + "   ·   Année : " + annee.getLibelle(), fSub);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingBefore(3);
        cell.addElement(sub);

        banner.addCell(cell);
        doc.add(banner);
        doc.add(new Paragraph(" "));
    }

    private void ajouterStats(Document doc, long total, long presents, long absents, long retards)
            throws DocumentException {

        PdfPTable t = new PdfPTable(new float[]{1, 1, 1, 1});
        t.setWidthPercentage(100);
        t.setSpacingAfter(8);

        ajouterStatCell(t, String.valueOf(total),    "Total appels",  new Color(100, 116, 139));
        ajouterStatCell(t, String.valueOf(presents), "Présents",      GREEN);
        ajouterStatCell(t, String.valueOf(absents),  "Absences",      RED);
        ajouterStatCell(t, String.valueOf(retards),  "Retards",       AMBER);

        doc.add(t);
    }

    private void ajouterStatCell(PdfPTable t, String val, String label, Color couleur) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(SLATE_50);
        cell.setBorderColor(SLATE_200);
        cell.setBorderWidth(0.5f);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font fVal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, couleur);
        Font fLbl = FontFactory.getFont(FontFactory.HELVETICA, 7, SLATE_700);

        Paragraph p = new Paragraph();
        p.add(new Chunk(val + "\n", fVal));
        p.add(new Chunk(label, fLbl));
        p.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);
        t.addCell(cell);
    }

    private void ajouterSectionTitre(Document doc, String titre) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setSpacingAfter(4);

        PdfPCell cell = new PdfPCell(new Phrase("  " + titre, F_SECTION));
        cell.setBackgroundColor(INDIGO_LIGHT);
        cell.setBorderColor(INDIGO);
        cell.setBorderWidth(0.5f);
        cell.setPadding(5);
        t.addCell(cell);
        doc.add(t);
    }

    private void ajouterTableau(Document doc, List<Appel> appels) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{3, 2, 3, 2, 2, 3});
        t.setWidthPercentage(100);

        entete(t, "Étudiant", "Date", "Séance", "Statut", "Retard", "Motif");

        boolean odd = false;
        for (Appel a : appels) {
            Color bg = odd ? WHITE : SLATE_50;

            String nomEtudiant = a.getEtudiant() != null
                    ? a.getEtudiant().getPrenom() + " " + a.getEtudiant().getNom() : "—";
            String date = a.getSeance() != null && a.getSeance().getDate() != null
                    ? a.getSeance().getDate().format(DATE_FMT) : "—";
            String seance = a.getSeance() != null && a.getSeance().getMatiere() != null
                    ? a.getSeance().getMatiere() : "—";
            String retard = a.getMinutesRetard() != null && a.getMinutesRetard() > 0
                    ? a.getMinutesRetard() + " min" : "—";

            cellule(t, nomEtudiant, bg);
            cellule(t, date, bg);
            cellule(t, seance, bg);

            // Statut coloré
            PdfPCell cStatut = new PdfPCell();
            cStatut.setBackgroundColor(bg);
            cStatut.setBorderColor(SLATE_200);
            cStatut.setBorderWidth(0.5f);
            cStatut.setPadding(4);
            Color fc = SLATE_700;
            if (a.getStatut() != null) {
                switch (a.getStatut()) {
                    case PRESENT -> fc = GREEN;
                    case ABSENT  -> fc = RED;
                    case RETARD  -> fc = AMBER;
                }
            }
            cStatut.addElement(new Phrase(
                    a.getStatut() != null ? a.getStatut().name() : "—",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, fc)));
            t.addCell(cStatut);

            cellule(t, retard, bg);
            cellule(t, a.getMotif() != null ? a.getMotif() : "—", bg);
            odd = !odd;
        }
        doc.add(t);

        if (appels.isEmpty()) {
            Font f = FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_700);
            Paragraph p = new Paragraph("Aucun appel enregistré pour cette classe.", f);
            p.setSpacingBefore(8);
            doc.add(p);
        }
    }

    private void ajouterPiedDePage(Document doc) throws DocumentException {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(148, 163, 184));
        Paragraph p = new Paragraph(
                "Document généré automatiquement par UniGest  ·  " +
                java.time.LocalDate.now().format(DATE_FMT), f);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(16);
        doc.add(p);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void entete(PdfPTable t, String... titres) {
        for (String titre : titres) {
            PdfPCell cell = new PdfPCell(new Phrase(titre, F_HEADER));
            cell.setBackgroundColor(INDIGO);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(5);
            t.addCell(cell);
        }
    }

    private void cellule(PdfPTable t, String val, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(val, F_CELL));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(SLATE_200);
        cell.setBorderWidth(0.5f);
        cell.setPadding(4);
        t.addCell(cell);
    }
}
