-- Mettre à jour l'année scolaire active pour qu'elle couvre aujourd'hui (2026-06-18)
UPDATE annee_scolaire 
SET date_debut = '2025-09-01', date_fin = '2026-08-31', libelle = '2025-2026'
WHERE id = 1;

-- Vérifier que les emplois du temps existent
SELECT * FROM emploi_du_temps;