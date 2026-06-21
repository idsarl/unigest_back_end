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
public class HistoriquePaiementsPdfService {

    private final EtudiantRepository      etudiantRepository;
    private final PaiementRepository      paiementRepository;
    private final ClasseRepository        classeRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final ParametreEcoleService   parametreEcoleService;

    // ── Couleurs ─────────────────────────────────────────────────────────────
    private static final Color INDIGO       = new Color(79, 70, 229);
    private static final Color INDIGO_LIGHT = new Color(224, 231, 255);
    private static final Color SLATE_50     = new Color(248, 250, 252);
    private static final Color SLATE_200    = new Color(226, 232, 240);
    private static final Color SLATE_700    = new Color(51, 65, 85);
    private static final Color WHITE        = Color.WHITE;
    private static final Color GREEN        = new Color(5, 150, 105);

    // ── Polices ───────────────────────────────────────────────────────────────
    private static final Font F_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,  INDIGO);
    private static final Font F_HEADER  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8,  WHITE);
    private static final Font F_CELL    = FontFactory.getFont(FontFactory.HELVETICA,      8,  SLATE_700);
    private static final Font F_LABEL   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8,  SLATE_700);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─────────────────────────────────────────────────────────────────────────

    public byte[] generer(Long etudiantId, Long classeId, Long anneeId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));
        ParametreEcole ecole = parametreEcoleService.getParametres();

        List<Paiement> paiements;
        String contexte = "";

        if (classeId != null && anneeId != null) {
            paiements = paiementRepository.findByEtudiantClasseAnnee(etudiantId, classeId, anneeId);
            Classe classe = classeRepository.findById(classeId).orElse(null);
            AnneeScolaire annee = anneeScolaireRepository.findById(anneeId).orElse(null);
            contexte = (classe != null ? "Classe : " + classe.getNom() : "") +
                       (annee != null ? "   ·   Année : " + annee.getLibelle() : "");
        } else if (anneeId != null) {
            paiements = paiementRepository.findByEtudiantIdAndAnneeId(etudiantId, anneeId);
            AnneeScolaire annee = anneeScolaireRepository.findById(anneeId).orElse(null);
            contexte = annee != null ? "Année : " + annee.getLibelle() : "";
        } else {
            paiements = paiementRepository.findByEtudiantId(etudiantId);
            contexte = "Toutes les années";
        }

        double total = paiements.stream().mapToDouble(Paiement::getMontant).sum();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            ajouterEntete(doc, ecole, etudiant, contexte);
            ajouterInfosEtudiant(doc, etudiant);
            ajouterSectionTitre(doc, "HISTORIQUE DES PAIEMENTS");
            ajouterTableauPaiements(doc, paiements, total);
            ajouterPiedDePage(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF paiements : " + e.getMessage(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void ajouterEntete(Document doc, ParametreEcole ecole, Etudiant etudiant, String contexte)
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
        Paragraph titre = new Paragraph("HISTORIQUE DES PAIEMENTS", fTitre);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingBefore(4);
        cell.addElement(titre);

        Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 9, INDIGO_LIGHT);
        String nomEtudiant = etudiant.getPrenom() + " " + etudiant.getNom();
        Paragraph sub = new Paragraph(nomEtudiant +
                (contexte.isBlank() ? "" : "   ·   " + contexte), fSub);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingBefore(3);
        cell.addElement(sub);

        banner.addCell(cell);
        doc.add(banner);
        doc.add(new Paragraph(" "));
    }

    private void ajouterInfosEtudiant(Document doc, Etudiant e) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{2, 4, 2, 4});
        t.setWidthPercentage(100);
        t.setSpacingAfter(8);

        celluleLabel(t, "Matricule");  celluleValeur(t, e.getMatricule() != null ? e.getMatricule() : "—", GREEN);
        celluleLabel(t, "Nom complet"); celluleValeur(t, e.getPrenom() + " " + e.getNom(), SLATE_700);
        celluleLabel(t, "Téléphone");  celluleValeur(t, e.getTelephone() != null ? e.getTelephone() : "—", SLATE_700);
        celluleLabel(t, "Email");      celluleValeur(t, e.getEmail() != null ? e.getEmail() : "—", SLATE_700);

        doc.add(t);
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

    private void ajouterTableauPaiements(Document doc, List<Paiement> paiements, double total)
            throws DocumentException {

        PdfPTable t = new PdfPTable(new float[]{3, 4, 3, 4});
        t.setWidthPercentage(100);

        entete(t, "Date", "Montant (FCFA)", "Mode", "Référence");

        boolean odd = false;
        for (Paiement p : paiements) {
            Color bg = odd ? WHITE : SLATE_50;
            cellule(t, p.getDatePaiement() != null ? p.getDatePaiement().format(DATE_FMT) : "—", bg);
            celluleDroite(t, String.format("%,.0f", p.getMontant()), bg);
            cellule(t, p.getModePaiement() != null ? p.getModePaiement().name() : "—", bg);
            cellule(t, p.getReference() != null ? p.getReference() : "—", bg);
            odd = !odd;
        }

        // Total
        Font fLbl = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, INDIGO);
        Font fTot = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, INDIGO);

        PdfPCell cLbl = new PdfPCell(new Phrase("TOTAL", fLbl));
        cLbl.setBackgroundColor(INDIGO_LIGHT);
        cLbl.setBorderColor(INDIGO);
        cLbl.setBorderWidth(0.5f);
        cLbl.setPadding(5);
        cLbl.setColspan(3);
        t.addCell(cLbl);

        PdfPCell cTot = new PdfPCell(new Phrase(String.format("%,.0f FCFA", total), fTot));
        cTot.setBackgroundColor(INDIGO_LIGHT);
        cTot.setBorderColor(INDIGO);
        cTot.setBorderWidth(0.5f);
        cTot.setPadding(5);
        cTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cTot);

        doc.add(t);

        if (paiements.isEmpty()) {
            Font f = FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_700);
            Paragraph p = new Paragraph("Aucun paiement enregistré.", f);
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

    private void celluleDroite(PdfPTable t, String val, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(val, F_LABEL));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(SLATE_200);
        cell.setBorderWidth(0.5f);
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cell);
    }

    private void celluleLabel(PdfPTable t, String val) {
        PdfPCell cell = new PdfPCell(new Phrase(val, F_LABEL));
        cell.setBackgroundColor(SLATE_50);
        cell.setBorderColor(SLATE_200);
        cell.setBorderWidth(0.5f);
        cell.setPadding(5);
        t.addCell(cell);
    }

    private void celluleValeur(PdfPTable t, String val, Color color) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 8, color);
        PdfPCell cell = new PdfPCell(new Phrase(val, f));
        cell.setBackgroundColor(WHITE);
        cell.setBorderColor(SLATE_200);
        cell.setBorderWidth(0.5f);
        cell.setPadding(5);
        t.addCell(cell);
    }
}
