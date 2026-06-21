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
public class DossierEtudiantPdfService {

    private final EtudiantRepository      etudiantRepository;
    private final InscriptionRepository   inscriptionRepository;
    private final PaiementRepository      paiementRepository;
    private final NoteRepository          noteRepository;
    private final AppelRepository         appelRepository;
    private final ParametreEcoleService   parametreEcoleService;

    // ── Palette couleurs ─────────────────────────────────────────────────────
    private static final Color INDIGO        = new Color(79, 70, 229);   // #4f46e5
    private static final Color INDIGO_LIGHT  = new Color(224, 231, 255); // #e0e7ff
    private static final Color SLATE_50      = new Color(248, 250, 252);
    private static final Color SLATE_200     = new Color(226, 232, 240);
    private static final Color SLATE_700     = new Color(51, 65, 85);
    private static final Color WHITE         = Color.WHITE;
    private static final Color GREEN         = new Color(5, 150, 105);
    private static final Color RED           = new Color(220, 38, 38);
    private static final Color AMBER         = new Color(217, 119, 6);

    // ── Polices ───────────────────────────────────────────────────────────────
    private static final Font F_ECOLE   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  12, INDIGO);
    private static final Font F_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, INDIGO);
    private static final Font F_HEADER  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8, WHITE);
    private static final Font F_CELL    = FontFactory.getFont(FontFactory.HELVETICA,         8, SLATE_700);
    private static final Font F_LABEL   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8, SLATE_700);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─────────────────────────────────────────────────────────────────────────

    public byte[] generer(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        List<Inscription>   inscriptions = inscriptionRepository.findByEtudiantId(etudiantId);
        List<Paiement>      paiements    = paiementRepository.findByEtudiantId(etudiantId);
        List<Note>          notes        = noteRepository.findByEtudiantId(etudiantId);
        List<Appel>         appels       = appelRepository.findByEtudiantId(etudiantId);
        ParametreEcole      ecole        = parametreEcoleService.getParametres();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ── 1. En-tête établissement ──────────────────────────────────────
            ajouterEntete(doc, ecole, etudiant);

            // ── 2. Informations étudiant ──────────────────────────────────────
            ajouterSectionTitre(doc, "INFORMATIONS DE L'ÉTUDIANT");
            ajouterInfosEtudiant(doc, etudiant);

            // ── 3. Inscriptions ───────────────────────────────────────────────
            if (!inscriptions.isEmpty()) {
                ajouterSectionTitre(doc, "INSCRIPTIONS");
                ajouterTableauInscriptions(doc, inscriptions);
            }

            // ── 4. Notes ──────────────────────────────────────────────────────
            if (!notes.isEmpty()) {
                ajouterSectionTitre(doc, "NOTES");
                ajouterTableauNotes(doc, notes);
            }

            // ── 5. Paiements ──────────────────────────────────────────────────
            if (!paiements.isEmpty()) {
                ajouterSectionTitre(doc, "HISTORIQUE DES PAIEMENTS");
                ajouterTableauPaiements(doc, paiements);
            }

            // ── 6. Présences/Absences ─────────────────────────────────────────
            if (!appels.isEmpty()) {
                ajouterSectionTitre(doc, "PRÉSENCES / ABSENCES");
                ajouterTableauAppels(doc, appels);
            }

            // ── 7. Pied de page ───────────────────────────────────────────────
            ajouterPiedDePage(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération dossier étudiant PDF : " + e.getMessage(), e);
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sections
    // ─────────────────────────────────────────────────────────────────────────

    private void ajouterEntete(Document doc, ParametreEcole ecole, Etudiant etudiant) throws DocumentException {
        // Bannière indigo
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(INDIGO);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(14);

        if (ecole.getNomEcole() != null && !ecole.getNomEcole().isBlank()) {
            Paragraph nom = new Paragraph(ecole.getNomEcole().toUpperCase(), F_ECOLE);
            nom.setAlignment(Element.ALIGN_CENTER);
            // Override font color to white
            nom.getFont().setColor(WHITE);
            cell.addElement(nom);
        }

        Font fTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, WHITE);
        Paragraph titreDoc = new Paragraph("DOSSIER ÉTUDIANT", fTitre);
        titreDoc.setAlignment(Element.ALIGN_CENTER);
        titreDoc.setSpacingBefore(4);
        cell.addElement(titreDoc);

        Font fMatricule = FontFactory.getFont(FontFactory.HELVETICA, 9, INDIGO_LIGHT);
        Paragraph mat = new Paragraph("Matricule : " + etudiant.getMatricule(), fMatricule);
        mat.setAlignment(Element.ALIGN_CENTER);
        mat.setSpacingBefore(2);
        cell.addElement(mat);

        banner.addCell(cell);
        doc.add(banner);

        // Infos établissement sous la bannière
        if (ecole.getAdresseEcole() != null || ecole.getTelephoneEcole() != null) {
            Font fAddr = FontFactory.getFont(FontFactory.HELVETICA, 8, SLATE_700);
            StringBuilder sb = new StringBuilder();
            if (ecole.getAdresseEcole() != null) sb.append(ecole.getAdresseEcole());
            if (ecole.getTelephoneEcole() != null) {
                if (sb.length() > 0) sb.append("  |  ");
                sb.append("Tél : ").append(ecole.getTelephoneEcole());
            }
            Paragraph addr = new Paragraph(sb.toString(), fAddr);
            addr.setAlignment(Element.ALIGN_CENTER);
            addr.setSpacingBefore(4);
            addr.setSpacingAfter(8);
            doc.add(addr);
        } else {
            doc.add(new Paragraph(" "));
        }
    }

    private void ajouterSectionTitre(Document doc, String titre) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(10);
        t.setSpacingAfter(4);

        PdfPCell cell = new PdfPCell(new Phrase("  " + titre, F_SECTION));
        cell.setBackgroundColor(INDIGO_LIGHT);
        cell.setBorderColor(INDIGO);
        cell.setBorderWidth(0.5f);
        cell.setPadding(5);
        t.addCell(cell);
        doc.add(t);
    }

    private void ajouterInfosEtudiant(Document doc, Etudiant e) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{3, 5, 3, 5});
        t.setWidthPercentage(100);

        String[][] data = {
            { "Nom", e.getNom() != null ? e.getNom() : "—",
              "Prénom", e.getPrenom() != null ? e.getPrenom() : "—" },
            { "Matricule", e.getMatricule() != null ? e.getMatricule() : "—",
              "Date de naissance", e.getDateNaissance() != null ? e.getDateNaissance().format(DATE_FMT) : "—" },
            { "Email", e.getEmail() != null ? e.getEmail() : "—",
              "Téléphone", e.getTelephone() != null ? e.getTelephone() : "—" },
        };

        if (e.getParent() != null) {
            Parent p = e.getParent();
            String nomParent = (p.getPrenom() != null ? p.getPrenom() + " " : "") + (p.getNom() != null ? p.getNom() : "");
            data = appendRow(data,
                new String[]{ "Parent", nomParent, "Tél parent", p.getTelephone() != null ? p.getTelephone() : "—" });
        }

        boolean odd = false;
        for (String[] row : data) {
            Color bg = odd ? WHITE : SLATE_50;
            for (int i = 0; i < 4; i++) {
                PdfPCell cell = new PdfPCell(new Phrase(row[i], i % 2 == 0 ? F_LABEL : F_CELL));
                cell.setBackgroundColor(bg);
                cell.setBorderColor(SLATE_200);
                cell.setBorderWidth(0.5f);
                cell.setPadding(5);
                t.addCell(cell);
            }
            odd = !odd;
        }
        doc.add(t);
    }

    private void ajouterTableauInscriptions(Document doc, List<Inscription> inscriptions) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{4, 4, 3, 2});
        t.setWidthPercentage(100);

        entete(t, "Classe", "Année scolaire", "Date", "Statut");

        boolean odd = false;
        for (Inscription ins : inscriptions) {
            Color bg = odd ? WHITE : SLATE_50;
            cellule(t, ins.getClasse() != null ? ins.getClasse().getNom() : "—", bg);
            cellule(t, ins.getAnneeScolaire() != null ? ins.getAnneeScolaire().getLibelle() : "—", bg);
            cellule(t, ins.getDateInscription() != null ? ins.getDateInscription().format(DATE_FMT) : "—", bg);

            PdfPCell c = new PdfPCell();
            c.setBackgroundColor(bg);
            c.setBorderColor(SLATE_200);
            c.setBorderWidth(0.5f);
            c.setPadding(4);
            String st = ins.getStatut() != null ? ins.getStatut() : "—";
            Color fc = "INSCRIT".equals(st) ? GREEN : ("ABANDONNE".equals(st) ? RED : SLATE_700);
            c.addElement(new Phrase(st, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, fc)));
            t.addCell(c);
            odd = !odd;
        }
        doc.add(t);
    }

    private void ajouterTableauNotes(Document doc, List<Note> notes) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{5, 2, 2, 2, 3, 3});
        t.setWidthPercentage(100);

        entete(t, "Matière", "Note", "Coeff.", "Type", "Période", "Date");

        boolean odd = false;
        for (Note n : notes) {
            Color bg = odd ? WHITE : SLATE_50;
            cellule(t, n.getMatiere() != null ? n.getMatiere().getNom() : "—", bg);

            // Couleur note selon résultat
            PdfPCell cNote = new PdfPCell();
            cNote.setBackgroundColor(bg);
            cNote.setBorderColor(SLATE_200);
            cNote.setBorderWidth(0.5f);
            cNote.setPadding(4);
            Color fc = n.getValeur() >= 10 ? GREEN : RED;
            cNote.addElement(new Phrase(String.format("%.2f", n.getValeur()) + "/20",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, fc)));
            t.addCell(cNote);

            cellule(t, String.valueOf(n.getCoefficient()), bg);
            cellule(t, n.getType() != null ? n.getType().name() : "—", bg);
            cellule(t, n.getPeriode() != null ? n.getPeriode() + "e période" : "—", bg);
            cellule(t, n.getDateEvaluation() != null ? n.getDateEvaluation().format(DATE_FMT) : "—", bg);
            odd = !odd;
        }
        doc.add(t);
    }

    private void ajouterTableauPaiements(Document doc, List<Paiement> paiements) throws DocumentException {
        double total = paiements.stream().mapToDouble(Paiement::getMontant).sum();

        PdfPTable t = new PdfPTable(new float[]{3, 4, 3, 4});
        t.setWidthPercentage(100);

        entete(t, "Date", "Montant (FCFA)", "Mode", "Référence");

        boolean odd = false;
        for (Paiement p : paiements) {
            Color bg = odd ? WHITE : SLATE_50;
            cellule(t, p.getDatePaiement() != null ? p.getDatePaiement().format(DATE_FMT) : "—", bg);
            celluleDroite(t, String.format("%,.0f", p.getMontant()), bg, F_LABEL);
            cellule(t, p.getModePaiement() != null ? p.getModePaiement().name() : "—", bg);
            cellule(t, p.getReference() != null ? p.getReference() : "—", bg);
            odd = !odd;
        }

        // Ligne total
        PdfPCell lblTotal = new PdfPCell(new Phrase("TOTAL", F_SECTION));
        lblTotal.setBackgroundColor(INDIGO_LIGHT);
        lblTotal.setBorderColor(INDIGO);
        lblTotal.setBorderWidth(0.5f);
        lblTotal.setPadding(5);
        lblTotal.setColspan(3);
        t.addCell(lblTotal);

        Font fTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, INDIGO);
        PdfPCell cTotal = new PdfPCell(new Phrase(String.format("%,.0f FCFA", total), fTotal));
        cTotal.setBackgroundColor(INDIGO_LIGHT);
        cTotal.setBorderColor(INDIGO);
        cTotal.setBorderWidth(0.5f);
        cTotal.setPadding(5);
        cTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cTotal);

        doc.add(t);
    }

    private void ajouterTableauAppels(Document doc, List<Appel> appels) throws DocumentException {
        long absences = appels.stream().filter(a -> a.getStatut() != null && a.getStatut().name().equals("ABSENT")).count();
        long retards  = appels.stream().filter(a -> a.getStatut() != null && a.getStatut().name().equals("RETARD")).count();

        PdfPTable t = new PdfPTable(new float[]{3, 4, 2, 3, 4});
        t.setWidthPercentage(100);

        entete(t, "Date", "Séance", "Statut", "Retard (min)", "Motif");

        boolean odd = false;
        for (Appel a : appels) {
            Color bg = odd ? WHITE : SLATE_50;
            String date = a.getSeance() != null && a.getSeance().getDate() != null
                    ? a.getSeance().getDate().format(DATE_FMT) : "—";
            String seance = a.getSeance() != null && a.getSeance().getMatiere() != null
                    ? a.getSeance().getMatiere() : "—";

            cellule(t, date, bg);
            cellule(t, seance, bg);

            PdfPCell cStatut = new PdfPCell();
            cStatut.setBackgroundColor(bg);
            cStatut.setBorderColor(SLATE_200);
            cStatut.setBorderWidth(0.5f);
            cStatut.setPadding(4);
            Color fc = SLATE_700;
            if (a.getStatut() != null) {
                switch (a.getStatut().name()) {
                    case "PRESENT" -> fc = GREEN;
                    case "ABSENT"  -> fc = RED;
                    case "RETARD"  -> fc = AMBER;
                }
            }
            cStatut.addElement(new Phrase(a.getStatut() != null ? a.getStatut().name() : "—",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, fc)));
            t.addCell(cStatut);

            cellule(t, a.getMinutesRetard() != null && a.getMinutesRetard() > 0 ? a.getMinutesRetard() + " min" : "—", bg);
            cellule(t, a.getMotif() != null ? a.getMotif() : "—", bg);
            odd = !odd;
        }

        doc.add(t);

        // Résumé absences/retards
        Font fStats = FontFactory.getFont(FontFactory.HELVETICA, 8, SLATE_700);
        Paragraph stats = new Paragraph(
                "Absences : " + absences + "   |   Retards : " + retards, fStats);
        stats.setSpacingBefore(4);
        doc.add(stats);
    }

    private void ajouterPiedDePage(Document doc) throws DocumentException {
        Font fPied = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(148, 163, 184));
        Paragraph p = new Paragraph(
                "Document généré automatiquement par UniGest  ·  " +
                java.time.LocalDate.now().format(DATE_FMT), fPied);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(20);
        doc.add(p);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers tableau
    // ─────────────────────────────────────────────────────────────────────────

    private void entete(PdfPTable t, String... titres) {
        for (String titre : titres) {
            PdfPCell cell = new PdfPCell(new Phrase(titre, F_HEADER));
            cell.setBackgroundColor(INDIGO);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(5);
            t.addCell(cell);
        }
    }

    private void cellule(PdfPTable t, String valeur, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(valeur, F_CELL));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(SLATE_200);
        cell.setBorderWidth(0.5f);
        cell.setPadding(4);
        t.addCell(cell);
    }

    private void celluleDroite(PdfPTable t, String valeur, Color bg, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valeur, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(SLATE_200);
        cell.setBorderWidth(0.5f);
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cell);
    }

    private String[][] appendRow(String[][] arr, String[] row) {
        String[][] newArr = new String[arr.length + 1][];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = row;
        return newArr;
    }
}
