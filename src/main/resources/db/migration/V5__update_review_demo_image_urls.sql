UPDATE saloon
SET img_url = 'https://api.saloons.fr/images/saloonDemo.jpg'
WHERE name = 'Saloons Review Demo';

UPDATE `user`
SET img_url = 'https://api.saloons.fr/images/Camille.png',
    profile_image_updated_at = NOW()
WHERE email = 'apple-review-camille-demo@saloons.fr';

UPDATE `user`
SET img_url = 'https://api.saloons.fr/images/Alex.png',
    profile_image_updated_at = NOW()
WHERE email = 'apple-review-alex-demo@saloons.fr';
