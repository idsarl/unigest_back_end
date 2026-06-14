-- Désactiver les clés étrangères pour la suppression en masse
SET FOREIGN_KEY_CHECKS=0;

-- Nettoyer toutes les tables
TRUNCATE TABLE appel;
TRUNCATE TABLE message;
TRUNCATE TABLE seance;
TRUNCATE TABLE ligne_bulletin;
TRUNCATE TABLE bulletin;
TRUNCATE TABLE note;
TRUNCATE TABLE paiement;
TRUNCATE TABLE inscription;
TRUNCATE TABLE affectation_matiere;
TRUNCATE TABLE affectation;
TRUNCATE TABLE classe_matiere;
TRUNCATE TABLE classe;
TRUNCATE TABLE etudiant;
TRUNCATE TABLE parent;
TRUNCATE TABLE admin;
TRUNCATE TABLE enseignant;
TRUNCATE TABLE utilisateur;
TRUNCATE TABLE annee_scolaire;
TRUNCATE TABLE matiere;
TRUNCATE TABLE filiere;
TRUNCATE TABLE niveau;
TRUNCATE TABLE categorie_depense;

-- Réactiver les clés étrangères
SET FOREIGN_KEY_CHECKS=1;
