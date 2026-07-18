-- Le formulaire admin a temporairement utilisé le rayon par défaut des saloons
-- (100 m) pour les événements. Restaurer le rayon de découverte attendu.
UPDATE event SET radius_meters = 25000 WHERE radius_meters = 100;
