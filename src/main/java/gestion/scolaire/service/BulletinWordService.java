package gestion.scolaire.service;

import gestion.scolaire.model.Bulletin;
import gestion.scolaire.model.LigneBulletin;
import gestion.scolaire.model.ParametreEcole;
import gestion.scolaire.repository.LigneBulletinRepository;
import gestion.scolaire.service.BulletinService.StatistiquesClasse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BulletinWordService {

    private final BulletinService bulletinService;
    private final LigneBulletinRepository ligneBulletinRepository;
    private final ParametreEcoleService parametreEcoleService;

    // ── Couleurs ──────────────────────────────────────────────────────────────
    private static final String GRIS_ENTETE = "DCDCDC";
    private static final String GRIS_CLAIR  = "F5F5F5";
    private static final String BLANC       = "FFFFFF";

    public byte[] genererWord(Long bulletinId) {
        Bulletin bulletin = bulletinService.getBulletin(bulletinId);
        List<LigneBulletin> lignes = ligneBulletinRepository.findByBulletin(bulletin);
        ParametreEcole ecole = parametreEcoleService.getParametres();

        StatistiquesClasse stats = bulletinService.getStatistiquesClasse(
                bulletin.getClasse().getId(),
                bulletin.getAnneeScolaire().getId(),
                bulletin.getPeriode(),
                bulletin.getTypePeriode());

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── 1. En-tête école ──────────────────────────────────────────────
            ajouterEnTeteEcole(doc, ecole);

            // ── 2. Titre bulletin ─────────────────────────────────────────────
            ajouterTitreBulletin(doc, bulletin);

            // ── 3. Infos étudiant ─────────────────────────────────────────────
            ajouterInfosEtudiant(doc, bulletin);
            para(doc, "");

            // ── 4. Tableau des notes ──────────────────────────────────────────
            ajouterTableauNotes(doc, lignes, stats, bulletin);

            // ── 5. Appréciation du proviseur ──────────────────────────────────
            para(doc, "");
            ajouterAppreciationProviseur(doc, bulletin);

            // ── 6. Signature ──────────────────────────────────────────────────
            para(doc, "");
            ajouterSignature(doc);

            doc.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération Word : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sections
    // ─────────────────────────────────────────────────────────────────────────

    private void ajouterEnTeteEcole(XWPFDocument doc, ParametreEcole ecole) {
        if (ecole.getNomEcole() != null && !ecole.getNomEcole().isBlank()) {
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            run(p, ecole.getNomEcole().toUpperCase(), true, 12);
        }
        if (ecole.getAdresseEcole() != null && !ecole.getAdresseEcole().isBlank()) {
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            run(p, ecole.getAdresseEcole(), false, 9);
        }
        if (ecole.getTelephoneEcole() != null && !ecole.getTelephoneEcole().isBlank()) {
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            run(p, "Tél : " + ecole.getTelephoneEcole(), false, 9);
        }
        para(doc, "");
    }

    private void ajouterTitreBulletin(XWPFDocument doc, Bulletin b) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        run(p, "BULLETIN DE NOTES DE LA " + b.getPeriode() + "è  PERIODE", true, 11);
    }

    private void ajouterInfosEtudiant(XWPFDocument doc, Bulletin b) {
        String annee  = b.getAnneeScolaire() != null ? b.getAnneeScolaire().getLibelle() : "—";
        String classe = b.getClasse() != null ? b.getClasse().getNom() : "—";

        // Classe + Année scolaire
        XWPFParagraph p1 = doc.createParagraph();
        runBold(p1, "Classe: ");
        run(p1, classe + "          ", false, 10);
        runBold(p1, "Année Scolaire: ");
        run(p1, annee, false, 10);

        // Prénom(s)
        String nomPrenom = (b.getEtudiant().getPrenom() + " " + b.getEtudiant().getNom()).toUpperCase();
        XWPFParagraph p2 = doc.createParagraph();
        runBold(p2, "PRENOM(S)  ");
        run(p2, nomPrenom, false, 10);

        // Matricule
        String mat = b.getEtudiant().getMatricule() != null ? b.getEtudiant().getMatricule() : "—";
        XWPFParagraph p3 = doc.createParagraph();
        runBold(p3, "N° Mle:  ");
        run(p3, mat, false, 10);
    }

    private void ajouterTableauNotes(XWPFDocument doc, List<LigneBulletin> lignes,
                                     StatistiquesClasse stats, Bulletin bulletin) {

        int nbLignesDonnees = lignes != null ? lignes.size() : 0;
        int nbLignesTotal   = 1 + nbLignesDonnees + 5; // 1 en-tête + données + 5 résumés

        XWPFTable table = doc.createTable(nbLignesTotal, 7);
        table.setWidth("100%");

        // ── En-tête ────────────────────────────────────────────────────────────
        String[] entetes = {"Matières", "N. Classe", "N. Comp", "Moyenne", "Coeff", "Moy. Coeff", "Appréciation"};
        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < 7; i++) {
            XWPFTableCell c = headerRow.getCell(i);
            setCouleur(c, GRIS_ENTETE);
            setTexte(c, entetes[i], true, i == 0 ? ParagraphAlignment.LEFT : ParagraphAlignment.CENTER);
        }

        // ── Données ────────────────────────────────────────────────────────────
        double totalCoeff    = 0;
        double totalMoyCoeff = 0;

        for (int i = 0; i < nbLignesDonnees; i++) {
            LigneBulletin l = lignes.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            String bg = (i % 2 == 0) ? BLANC : GRIS_CLAIR;

            double moyCoeff = l.getMoyenneMatiere() * l.getCoefficient();
            totalCoeff    += l.getCoefficient();
            totalMoyCoeff += moyCoeff;

            setCouleur(row.getCell(0), bg);
            setTexte(row.getCell(0), nvl(l.getMatiere() != null ? l.getMatiere().getNom() : null),
                    false, ParagraphAlignment.LEFT);
            setCouleur(row.getCell(1), bg);
            setTexte(row.getCell(1), l.getNoteClasse() > 0 ? fmt2(l.getNoteClasse()) : "—",
                    false, ParagraphAlignment.CENTER);
            setCouleur(row.getCell(2), bg);
            setTexte(row.getCell(2), l.getNoteComposition() > 0 ? fmt2(l.getNoteComposition()) : "—",
                    false, ParagraphAlignment.CENTER);
            setCouleur(row.getCell(3), bg);
            setTexte(row.getCell(3), fmt2(l.getMoyenneMatiere()), false, ParagraphAlignment.CENTER);
            setCouleur(row.getCell(4), bg);
            setTexte(row.getCell(4), fmtEntier(l.getCoefficient()), false, ParagraphAlignment.CENTER);
            setCouleur(row.getCell(5), bg);
            setTexte(row.getCell(5), fmt2(moyCoeff), false, ParagraphAlignment.CENTER);
            setCouleur(row.getCell(6), bg);
            setTexte(row.getCell(6), nvl(l.getAppreciation()), false, ParagraphAlignment.CENTER);
        }

        int base = 1 + nbLignesDonnees;

        // ── Total Coeff ────────────────────────────────────────────────────────
        ligneResume(table, base,     "Total Coeff", fmtEntier(totalCoeff), 4);

        // ── Total ──────────────────────────────────────────────────────────────
        ligneResume(table, base + 1, "Total", fmt2(totalMoyCoeff), 5);

        // ── Moyenne du 1er ─────────────────────────────────────────────────────
        ligneResume(table, base + 2, "Moyenne du 1er de la classe", fmt2(stats.moyennePremier()), 3);

        // ── Moyenne de l'élève ─────────────────────────────────────────────────
        ligneResume(table, base + 3, "Moyenne de l'élève", fmt2(bulletin.getMoyenneGenerale()), 3);

        // ── Rang ───────────────────────────────────────────────────────────────
        String rangStr = bulletin.getRang() != null
                ? ordinal(bulletin.getRang()) + " /" + stats.effectif()
                : "—";
        ligneResume(table, base + 4, "Rang", rangStr, 3);
    }

    /**
     * Crée une ligne de résumé : [label sur colspan cols] [valeur] [vides restants]
     * La table a 7 colonnes.
     */
    private void ligneResume(XWPFTable table, int rowIdx, String label, String valeur, int colspan) {
        XWPFTableRow row = table.getRow(rowIdx);

        // Fusionner les premières cellules pour le label (via CTHMerge)
        for (int i = 0; i < 7; i++) {
            XWPFTableCell c = row.getCell(i);
            setCouleur(c, GRIS_CLAIR);

            if (i == 0) {
                setTexte(c, label, true, ParagraphAlignment.LEFT);
                if (colspan > 1) setHMergeRestart(c);
            } else if (i < colspan) {
                setTexte(c, "", false, ParagraphAlignment.LEFT);
                setHMergeContinue(c);
            } else if (i == colspan) {
                setTexte(c, valeur, true, ParagraphAlignment.CENTER);
            } else {
                setTexte(c, "", false, ParagraphAlignment.LEFT);
            }
        }
    }

    private void ajouterAppreciationProviseur(XWPFDocument doc, Bulletin b) {
        XWPFParagraph p = doc.createParagraph();
        runBold(p, "APPRECIATION DU PROVISEUR :    ");
        runBold(p, nvl(b.getAppreciation()).toUpperCase());
    }

    private void ajouterSignature(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.RIGHT);
        p.setSpacingBefore(600);
        run(p, "Le Proviseur", false, 10);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void para(XWPFDocument doc, String texte) {
        doc.createParagraph().createRun().setText(texte);
    }

    private XWPFRun run(XWPFParagraph p, String texte, boolean bold, int size) {
        XWPFRun r = p.createRun();
        r.setBold(bold);
        r.setFontSize(size);
        r.setText(texte);
        return r;
    }

    private void runBold(XWPFParagraph p, String texte) {
        run(p, texte, true, 10);
    }

    private void setCouleur(XWPFTableCell cell, String hex) {
        CTTc tc = cell.getCTTc();
        CTTcPr pr = tc.isSetTcPr() ? tc.getTcPr() : tc.addNewTcPr();
        CTShd shd = pr.isSetShd() ? pr.getShd() : pr.addNewShd();
        shd.setFill(hex);
        shd.setColor(hex);
        shd.setVal(STShd.CLEAR);
    }

    private void setTexte(XWPFTableCell cell, String texte, boolean bold, ParagraphAlignment align) {
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        p.setAlignment(align);
        p.getRuns().forEach(r -> r.setText("", 0));
        XWPFRun r = p.getRuns().isEmpty() ? p.createRun() : p.getRuns().get(0);
        r.setBold(bold);
        r.setFontSize(9);
        r.setText(texte);
    }

    private void setHMergeRestart(XWPFTableCell cell) {
        CTTc tc = cell.getCTTc();
        CTTcPr pr = tc.isSetTcPr() ? tc.getTcPr() : tc.addNewTcPr();
        CTHMerge merge = pr.isSetHMerge() ? pr.getHMerge() : pr.addNewHMerge();
        merge.setVal(STMerge.RESTART);
    }

    private void setHMergeContinue(XWPFTableCell cell) {
        CTTc tc = cell.getCTTc();
        CTTcPr pr = tc.isSetTcPr() ? tc.getTcPr() : tc.addNewTcPr();
        CTHMerge merge = pr.isSetHMerge() ? pr.getHMerge() : pr.addNewHMerge();
        merge.setVal(STMerge.CONTINUE);
    }

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
