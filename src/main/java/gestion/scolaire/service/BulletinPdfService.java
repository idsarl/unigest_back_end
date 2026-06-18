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
import gestion.scolaire.model.ParametreEcole;
import gestion.scolaire.repository.LigneBulletinRepository;
import gestion.scolaire.service.BulletinService.StatistiquesClasse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BulletinPdfService {

    private final BulletinService bulletinService;
    private final LigneBulletinRepository ligneBulletinRepository;
    private final ParametreEcoleService parametreEcoleService;

    // ── Couleurs ──────────────────────────────────────────────────────────────
    private static final Color GRIS_ENTETE  = new Color(220, 220, 220);
    private static final Color GRIS_CLAIR   = new Color(245, 245, 245);
    private static final Color BLANC        = Color.WHITE;
    private static final Color NOIR         = Color.BLACK;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font F_ECOLE_NOM   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  12, NOIR);
    private static final Font F_ECOLE_INFO  = FontFactory.getFont(FontFactory.HELVETICA,        9, NOIR);
    private static final Font F_TITRE_BULL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  11, NOIR);
    private static final Font F_ENTETE_TAB  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8, NOIR);
    private static final Font F_NORMAL      = FontFactory.getFont(FontFactory.HELVETICA,         8, NOIR);
    private static final Font F_GRAS        = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8, NOIR);
    private static final Font F_LABEL       = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, NOIR);

    // ─────────────────────────────────────────────────────────────────────────

    public byte[] genererPdf(Long bulletinId) {
        Bulletin bulletin  = bulletinService.getBulletin(bulletinId);
        List<LigneBulletin> lignes = ligneBulletinRepository.findByBulletin(bulletin);
        ParametreEcole ecole = parametreEcoleService.getParametres();

        Long classeId = bulletin.getClasse().getId();
        Long anneeId  = bulletin.getAnneeScolaire().getId();
        StatistiquesClasse stats = bulletinService.getStatistiquesClasse(
                classeId, anneeId, bulletin.getPeriode(), bulletin.getTypePeriode());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ── 1. En-tête école ──────────────────────────────────────────────
            ajouterEnTeteEcole(doc, ecole);

            // ── 2. Titre bulletin ─────────────────────────────────────────────
            ajouterTitreBulletin(doc, bulletin);

            // ── 3. Infos étudiant ─────────────────────────────────────────────
            ajouterInfosEtudiant(doc, bulletin);

            doc.add(new Paragraph(" "));

            // ── 4. Tableau des notes ──────────────────────────────────────────
            doc.add(creerTableauNotes(lignes, stats, bulletin));

            // ── 5. Appréciation du Proviseur ──────────────────────────────────
            doc.add(new Paragraph(" "));
            ajouterAppreciationProviseur(doc, bulletin);

            // ── 6. Signature ──────────────────────────────────────────────────
            doc.add(new Paragraph(" "));
            ajouterSignature(doc);

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

    private void ajouterEnTeteEcole(Document doc, ParametreEcole ecole) throws DocumentException {
        if (ecole.getNomEcole() != null && !ecole.getNomEcole().isBlank()) {
            Paragraph p = new Paragraph(ecole.getNomEcole().toUpperCase(), F_ECOLE_NOM);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
        }
        if (ecole.getAdresseEcole() != null && !ecole.getAdresseEcole().isBlank()) {
            Paragraph p = new Paragraph(ecole.getAdresseEcole(), F_ECOLE_INFO);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
        }
        if (ecole.getTelephoneEcole() != null && !ecole.getTelephoneEcole().isBlank()) {
            Paragraph p = new Paragraph("Tél : " + ecole.getTelephoneEcole(), F_ECOLE_INFO);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
        }
        doc.add(new Paragraph(" "));
    }

    private void ajouterTitreBulletin(Document doc, Bulletin b) throws DocumentException {
        String typePer = b.getTypePeriode() != null ? b.getTypePeriode().name() : "";
        String titre = "BULLETIN DE NOTES DE LA " + b.getPeriode() + "è  PERIODE";
        Paragraph p = new Paragraph(titre, F_TITRE_BULL);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private void ajouterInfosEtudiant(Document doc, Bulletin b) throws DocumentException {
        String annee = b.getAnneeScolaire() != null ? b.getAnneeScolaire().getLibelle() : "—";
        String classe = b.getClasse() != null ? b.getClasse().getNom() : "—";

        // Ligne : Classe: XXX    Année Scolaire: XXXX-XXXX
        Paragraph ligneClasse = new Paragraph();
        ligneClasse.add(new Chunk("Classe:  ", F_GRAS));
        ligneClasse.add(new Chunk(classe + "          ", F_NORMAL));
        ligneClasse.add(new Chunk("Année Scolaire:  ", F_GRAS));
        ligneClasse.add(new Chunk(annee, F_NORMAL));
        doc.add(ligneClasse);

        // Ligne : PRENOM(S) : Prénom Nom
        String nomPrenom = (b.getEtudiant().getPrenom() + " " + b.getEtudiant().getNom()).toUpperCase();
        Paragraph ligneNom = new Paragraph();
        ligneNom.add(new Chunk("PRENOM(S)  ", F_GRAS));
        ligneNom.add(new Chunk(nomPrenom, F_NORMAL));
        doc.add(ligneNom);

        // Ligne : N° Mle : matricule
        String mat = b.getEtudiant().getMatricule() != null ? b.getEtudiant().getMatricule() : "—";
        Paragraph ligneMat = new Paragraph();
        ligneMat.add(new Chunk("N° Mle:  ", F_GRAS));
        ligneMat.add(new Chunk(mat, F_NORMAL));
        doc.add(ligneMat);
    }

    /**
     * Tableau principal — colonnes :
     * Matières | N. Classe | N. Comp | Moyenne | Coeff | Moy. Coeff | Appréciation
     *
     * Lignes de bas :
     * Total Coeff, Total, Moyenne du 1er, Moyenne de l'élève, Rang
     */
    private PdfPTable creerTableauNotes(List<LigneBulletin> lignes, StatistiquesClasse stats, Bulletin b)
            throws DocumentException {

        // Largeurs relatives des 7 colonnes
        float[] widths = {3.5f, 1.4f, 1.4f, 1.4f, 1f, 1.6f, 2.5f};
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);

        // ── En-tête du tableau ────────────────────────────────────────────────
        ajouterEntete(table, "Matières",      Element.ALIGN_LEFT);
        ajouterEntete(table, "N. Classe",     Element.ALIGN_CENTER);
        ajouterEntete(table, "N. Comp",       Element.ALIGN_CENTER);
        ajouterEntete(table, "Moyenne",       Element.ALIGN_CENTER);
        ajouterEntete(table, "Coeff",         Element.ALIGN_CENTER);
        ajouterEntete(table, "Moy. Coeff",    Element.ALIGN_CENTER);
        ajouterEntete(table, "Appréciation",  Element.ALIGN_CENTER);

        // ── Lignes par matière ────────────────────────────────────────────────
        double totalCoeff    = 0;
        double totalMoyCoeff = 0;

        if (lignes == null || lignes.isEmpty()) {
            PdfPCell vide = new PdfPCell(new Phrase("Aucune note disponible", F_NORMAL));
            vide.setColspan(7);
            vide.setHorizontalAlignment(Element.ALIGN_CENTER);
            vide.setPadding(6);
            table.addCell(vide);
        } else {
            boolean pair = false;
            for (LigneBulletin l : lignes) {
                Color bg = pair ? GRIS_CLAIR : BLANC;
                pair = !pair;

                double moyCoeff = l.getMoyenneMatiere() * l.getCoefficient();
                totalCoeff    += l.getCoefficient();
                totalMoyCoeff += moyCoeff;

                table.addCell(cellule(nvl(l.getMatiere() != null ? l.getMatiere().getNom() : null),
                        F_NORMAL, Element.ALIGN_LEFT, bg));
                table.addCell(cellule(
                        l.getNoteClasse() > 0 ? fmt2(l.getNoteClasse()) : "—",
                        F_NORMAL, Element.ALIGN_CENTER, bg));
                table.addCell(cellule(
                        l.getNoteComposition() > 0 ? fmt2(l.getNoteComposition()) : "—",
                        F_NORMAL, Element.ALIGN_CENTER, bg));
                table.addCell(cellule(fmt2(l.getMoyenneMatiere()),
                        F_NORMAL, Element.ALIGN_CENTER, bg));
                table.addCell(cellule(fmtEntier(l.getCoefficient()),
                        F_NORMAL, Element.ALIGN_CENTER, bg));
                table.addCell(cellule(fmt2(moyCoeff),
                        F_NORMAL, Element.ALIGN_CENTER, bg));
                table.addCell(cellule(nvl(l.getAppreciation()),
                        F_NORMAL, Element.ALIGN_CENTER, bg));
            }
        }

        // ── Ligne Total Coeff ─────────────────────────────────────────────────
        table.addCell(ligneResume("Total Coeff", 4));
        table.addCell(celluleResume(fmtEntier(totalCoeff)));
        table.addCell(celluleVide());
        table.addCell(celluleVide());

        // ── Ligne Total ───────────────────────────────────────────────────────
        table.addCell(ligneResume("Total", 5));
        table.addCell(celluleResume(fmt2(totalMoyCoeff)));
        table.addCell(celluleVide());

        // ── Moyenne du 1er de la classe ───────────────────────────────────────
        table.addCell(ligneResume("Moyenne du 1er de la classe", 3));
        table.addCell(celluleResume(fmt2(stats.moyennePremier())));
        table.addCell(celluleVide());
        table.addCell(celluleVide());
        table.addCell(celluleVide());

        // ── Moyenne de l'élève ────────────────────────────────────────────────
        table.addCell(ligneResume("Moyenne de l'élève", 3));
        table.addCell(celluleResume(fmt2(b.getMoyenneGenerale())));
        table.addCell(celluleVide());
        table.addCell(celluleVide());
        table.addCell(celluleVide());

        // ── Rang ──────────────────────────────────────────────────────────────
        table.addCell(ligneResume("Rang", 3));
        String rangStr = b.getRang() != null
                ? ordinal(b.getRang()) + " /" + stats.effectif()
                : "—";
        table.addCell(celluleResume(rangStr));
        table.addCell(celluleVide());
        table.addCell(celluleVide());
        table.addCell(celluleVide());

        return table;
    }

    private void ajouterAppreciationProviseur(Document doc, Bulletin b) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk("APPRECIATION DU PROVISEUR :    ", F_LABEL));
        p.add(new Chunk(nvl(b.getAppreciation()).toUpperCase(), F_LABEL));
        doc.add(p);
    }

    private void ajouterSignature(Document doc) throws DocumentException {
        Paragraph p = new Paragraph("Le Proviseur", F_NORMAL);
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setSpacingBefore(30);
        doc.add(p);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de cellules
    // ─────────────────────────────────────────────────────────────────────────

    private void ajouterEntete(PdfPTable table, String texte, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, F_ENTETE_TAB));
        cell.setBackgroundColor(GRIS_ENTETE);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private PdfPCell cellule(String texte, Font font, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setHorizontalAlignment(align);
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        return cell;
    }

    /** Cellule qui s'étend sur colspan colonnes, texte en gras à gauche */
    private PdfPCell ligneResume(String texte, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, F_GRAS));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4);
        cell.setBackgroundColor(GRIS_CLAIR);
        return cell;
    }

    /** Cellule de valeur centrée pour les lignes de résumé */
    private PdfPCell celluleResume(String texte) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, F_GRAS));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        cell.setBackgroundColor(GRIS_CLAIR);
        return cell;
    }

    /** Cellule vide (pour compléter les lignes de résumé) */
    private PdfPCell celluleVide() {
        PdfPCell cell = new PdfPCell(new Phrase("", F_NORMAL));
        cell.setPadding(4);
        cell.setBackgroundColor(GRIS_CLAIR);
        return cell;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaires
    // ─────────────────────────────────────────────────────────────────────────

    private String fmt2(double v) {
        return String.format("%.2f", v);
    }

    private String fmtEntier(double v) {
        return String.valueOf((int) v);
    }

    private String nvl(String s) {
        return s != null ? s : "—";
    }

    private String ordinal(int n) {
        return n + (n == 1 ? "er" : "è");
    }
}
