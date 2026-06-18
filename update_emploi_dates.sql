
-- Update emploi_du_temps dates
UPDATE emploi_du_temps 
SET date_debut = '2025-09-01', date_fin = '2026-08-31'
WHERE date_debut = '2024-09-01' AND date_fin = '2025-07-31';
