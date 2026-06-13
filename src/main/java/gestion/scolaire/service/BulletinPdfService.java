package gestion.scolaire.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import gestion.scolaire.model.Bulletin;
import gestion.scolaire.model.LigneBulletin;
import gestion.scolaire.repository.LigneBulletinRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BulletinPdfService {

    private final BulletinService bulletinService;
    private final LigneBulletinRepository ligneBulletinRepository;

    // ── Couleurs ──────────────────────────────────────────────────────────────
    private static final Color BLEU_FONCE   = new Color(23,  54,  93);
    private static final Color BLEU_CLAIR   = new Color(189, 215, 238);
    private static final Color VERT_PASSE   = new Color(198, 224, 180);
    private static final Color ROUGE_PASSE  = new Color(255, 199, 206);
    private static final Color GRIS_CLAIR   = new Color(242, 242, 242);
    private static final Color BLANC        = Color.WHITE;
    private static final Color NOIR         = Color.BLACK;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font F_TITRE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  18, BLANC);
    private static final Font F_SOUS_TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BLEU_FONCE);
    private static final Font F_NORMAL   = FontFactory.getFont(FontFactory.HELVETICA,        9,  NOIR);
    private static final Font F_GRAS     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9,  NOIR);
    private static final Font F_BLANC    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9,  BLANC);
    private static final Font F_PETIT    = FontFactory.getFont(FontFactory.HELVETICA,         8,  Color.DARK_GRAY);
    private static final Font F_MOYEN_GRAS  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BLEU_FONCE);
    private static final Font F_GRAND_GRAS  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BLEU_FONCE);

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Génère le PDF d'un bulletin et retourne les bytes.
     */
    public byte[] genererPdf(Long bulletinId) {
        Bulletin bulletin = bulletinService.getBulletin(bulletinId);
        List<LigneBulletin> lignes = ligneBulletinRepository.findByBulletin(bulletin);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // ── 1. Bandeau titre ──────────────────────────────────────────────
            ajouterBandeauTitre(doc, bulletin);

            doc.add(new Paragraph(" "));

            // ── 2. Infos étudiant + résumé côte à côte ────────────────────────
            ajouterInfosEtudiant(doc, bulletin);

            doc.add(new Paragraph(" "));

            // ── 3. Tableau des notes ──────────────────────────────────────────
            doc.add(creerTitreSection("Détail des notes par matière"));
            doc.add(new Paragraph(" "));
            doc.add(creerTableauNotes(lignes));

            doc.add(new Paragraph(" "));

            // ── 4. Résumé général ─────────────────────────────────────────────
            ajouterResumeGeneral(doc, bulletin, lignes.size());

            // ── 5. Signatures ─────────────────────────────────────────────────
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));
            ajouterSignatures(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF : " + e.getMessage(), e);
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sections
    // ─────────────────────────────────────────────────────────────────────────

    private void ajouterBandeauTitre(Document doc, Bulletin b) throws DocumentException {
        PdfPTable bandeau = new PdfPTable(1);
        bandeau.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BLEU_FONCE);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(14);

        String typePer = b.getTypePeriode() != null ? b.getTypePeriode().name() : "";
        String titrePdf = "BULLETIN DE NOTES — " + typePer + " " + b.getPeriode();

        Paragraph titre = new Paragraph(titrePdf, F_TITRE);
        titre.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(titre);

        String annee = b.getAnneeScolaire() != null ? b.getAnneeScolaire().getLibelle() : "";
        Paragraph sousTitre = new Paragraph("Année scolaire " + annee,
                FontFactory.getFont(FontFactory.HELVETICA, 10, BLEU_CLAIR));
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(sousTitre);

        bandeau.addCell(cell);
        doc.add(bandeau);
    }

    private void ajouterInfosEtudiant(Document doc, Bulletin b) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1, 1});
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        // ── Colonne gauche : infos étudiant ───────────────────────────────────
        PdfPCell left = new PdfPCell();
        left.setBorderColor(BLEU_CLAIR);
        left.setPadding(10);

        left.addElement(new Paragraph("INFORMATIONS ÉTUDIANT", F_SOUS_TITRE));
        left.addElement(new Paragraph(" "));

        String nomPrenom = (b.getEtudiant().getPrenom() + " " + b.getEtudiant().getNom()).toUpperCase();
        left.addElement(ligneInfo("Élève       :", nomPrenom));
        left.addElement(ligneInfo("Matricule   :", b.getEtudiant().getMatricule() != null
                ? b.getEtudiant().getMatricule() : "—"));
        left.addElement(ligneInfo("Classe      :", b.getClasse().getNom()));

        if (b.getEtudiant().getDateNaissance() != null) {
            String ddn = b.getEtudiant().getDateNaissance()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            left.addElement(ligneInfo("Né(e) le    :", ddn));
        }

        if (b.getDateGeneration() != null) {
            String date = b.getDateGeneration()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            left.addElement(ligneInfo("Edité le    :", date));
        }

        // ── Colonne droite : résultat ─────────────────────────────────────────
        PdfPCell right = new PdfPCell();
        right.setBorderColor(BLEU_CLAIR);
        right.setPadding(10);

        right.addElement(new Paragraph("RÉSULTAT", F_SOUS_TITRE));
        right.addElement(new Paragraph(" "));

        // Moyenne dans une grande case colorée
        Color couleurMoy = couleurMoyenne(b.getMoyenneGenerale());
        PdfPTable moyTable = new PdfPTable(1);
        moyTable.setWidthPercentage(80);
        PdfPCell moyCell = new PdfPCell();
        moyCell.setBackgroundColor(couleurMoy);
        moyCell.setPadding(8);
        moyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        moyCell.setBorder(Rectangle.NO_BORDER);

        Paragraph moyPar = new Paragraph(
                String.format("%.2f / 20", b.getMoyenneGenerale()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BLEU_FONCE));
        moyPar.setAlignment(Element.ALIGN_CENTER);
        moyCell.addElement(moyPar);

        Paragraph apprPar = new Paragraph(
                b.getAppreciation() != null ? b.getAppreciation() : "",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BLEU_FONCE));
        apprPar.setAlignment(Element.ALIGN_CENTER);
        moyCell.addElement(apprPar);

        moyTable.addCell(moyCell);
        right.addElement(moyTable);

        right.addElement(new Paragraph(" "));
        if (b.getRang() != null) {
            right.addElement(ligneInfo("Rang        :", b.getRang() + "e sur la classe"));
        }

        table.addCell(left);
        table.addCell(right);
        doc.add(table);
    }

    private PdfPTable creerTableauNotes(List<LigneBulletin> lignes) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{3.5f, 1f, 1.5f, 3f});
        table.setWidthPercentage(100);

        // En-tête
        ajouterEnteteTableau(table, "MATIÈRE",        Element.ALIGN_LEFT);
        ajouterEnteteTableau(table, "COEFF.",         Element.ALIGN_CENTER);
        ajouterEnteteTableau(table, "MOYENNE /20",    Element.ALIGN_CENTER);
        ajouterEnteteTableau(table, "APPRÉCIATION",   Element.ALIGN_CENTER);

        if (lignes == null || lignes.isEmpty()) {
            PdfPCell vide = new PdfPCell(new Phrase("Aucune note disponible", F_NORMAL));
            vide.setColspan(4);
            vide.setHorizontalAlignment(Element.ALIGN_CENTER);
            vide.setPadding(8);
            table.addCell(vide);
            return table;
        }

        boolean pair = false;
        for (LigneBulletin ligne : lignes) {
            Color bg = pair ? GRIS_CLAIR : BLANC;
            pair = !pair;

            // Matière
            PdfPCell cMat = cellule(
                    ligne.getMatiere() != null ? ligne.getMatiere().getNom() : "—",
                    F_GRAS, Element.ALIGN_LEFT, bg);
            table.addCell(cMat);

            // Coefficient
            table.addCell(cellule(
                    String.valueOf((int) ligne.getCoefficient()),
                    F_NORMAL, Element.ALIGN_CENTER, bg));

            // Moyenne — colorée selon niveau
            Color couleur = couleurMoyenne(ligne.getMoyenneMatiere());
            PdfPCell cMoy = cellule(
                    String.format("%.2f", ligne.getMoyenneMatiere()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BLEU_FONCE),
                    Element.ALIGN_CENTER, couleur);
            table.addCell(cMoy);

            // Appréciation
            table.addCell(cellule(
                    ligne.getAppreciation() != null ? ligne.getAppreciation() : "—",
                    F_PETIT, Element.ALIGN_CENTER, bg));
        }

        return table;
    }

    private void ajouterResumeGeneral(Document doc, Bulletin b, int nbMatieres)
            throws DocumentException {

        PdfPTable table = new PdfPTable(new float[]{1, 1, 1, 1});
        table.setWidthPercentage(100);

        ajouterCaseResume(table, "MATIÈRES ÉVALUÉES",
                String.valueOf(nbMatieres), BLEU_CLAIR);

        ajouterCaseResume(table, "MOYENNE GÉNÉRALE",
                String.format("%.2f / 20", b.getMoyenneGenerale()),
                couleurMoyenne(b.getMoyenneGenerale()));

        ajouterCaseResume(table, "RANG",
                b.getRang() != null ? b.getRang() + "e" : "—", BLEU_CLAIR);

        ajouterCaseResume(table, "APPRÉCIATION",
                b.getAppreciation() != null ? b.getAppreciation() : "—", BLEU_CLAIR);

        doc.add(table);
    }

    private void ajouterSignatures(Document doc) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1, 1});
        table.setWidthPercentage(100);

        PdfPCell cLeft  = celluleSignature("Signature du Directeur");
        PdfPCell cRight = celluleSignature("Signature du Parent/Tuteur");

        table.addCell(cLeft);
        table.addCell(cRight);
        doc.add(table);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Paragraph creerTitreSection(String texte) {
        Paragraph p = new Paragraph(texte.toUpperCase(), F_SOUS_TITRE);
        p.setSpacingBefore(4);
        return p;
    }

    private Paragraph ligneInfo(String label, String valeur) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", F_GRAS));
        p.add(new Chunk(valeur, F_NORMAL));
        p.setSpacingBefore(2);
        return p;
    }

    private void ajouterEnteteTableau(PdfPTable table, String texte, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, F_BLANC));
        cell.setBackgroundColor(BLEU_FONCE);
        cell.setHorizontalAlignment(align);
        cell.setPadding(7);
        cell.setBorderColor(BLANC);
        table.addCell(cell);
    }

    private PdfPCell cellule(String texte, Font font, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setHorizontalAlignment(align);
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        return cell;
    }

    private void ajouterCaseResume(PdfPTable table, String label, String valeur, Color bg) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph lbl = new Paragraph(label, F_PETIT);
        lbl.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(lbl);

        Paragraph val = new Paragraph(valeur, F_MOYEN_GRAS);
        val.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(val);

        table.addCell(cell);
    }

    private PdfPCell celluleSignature(String label) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setMinimumHeight(60);
        cell.setBorderColor(BLEU_CLAIR);

        Paragraph p = new Paragraph(label, F_PETIT);
        p.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);
        return cell;
    }

    /**
     * Couleur de fond selon la moyenne :
     *   ≥ 10 → vert pâle  |  < 10 → rouge pâle
     */
    private Color couleurMoyenne(double moyenne) {
        return moyenne >= 10 ? VERT_PASSE : ROUGE_PASSE;
    }
}
