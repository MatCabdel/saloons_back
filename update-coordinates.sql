-- Script pour nettoyer et mettre à jour les saloons
-- À exécuter dans DBeaver sur la base saloons_dev

-- 1. Supprimer les doublons et saloons non voulus
DELETE FROM saloon WHERE name = 'Passage du Titre CDA';

DELETE FROM saloon WHERE id > 5;

-- 2. Mettre à jour les coordonnées GPS précises
UPDATE saloon
SET
    latitude = 44.83842840285206,
    longitude = -0.575610564976734
WHERE
    name = 'Le Magnus';

UPDATE saloon
SET
    latitude = 44.84115720658105,
    longitude = -0.581747002585448
WHERE
    name LIKE '%Engrenage%';

UPDATE saloon
SET
    latitude = 44.838817483569436,
    longitude = -0.5789271928216408
WHERE
    name = 'Le Minouche';

UPDATE saloon
SET
    latitude = 44.841514754939915,
    longitude = -0.5818113755995328
WHERE
    name = 'Le Sherlock';

UPDATE saloon
SET
    latitude = 44.83909795210986,
    longitude = -0.5683563973081616
WHERE
    name LIKE '%Vintage%';

-- 3. Vérification finale
SELECT id, name, latitude, longitude, is_active FROM saloon;