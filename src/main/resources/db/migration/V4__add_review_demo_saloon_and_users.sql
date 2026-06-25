INSERT INTO saloon (
    name,
    img_url,
    visitor_number,
    created_at,
    longitude,
    latitude,
    address,
    city,
    country,
    type,
    radius_meters,
    is_active,
    is_private
)
SELECT
    'Saloons Review Demo',
    'https://api.saloons.fr/images/saloonDemo.jpg',
    0,
    NOW(),
    -0.575610564976734,
    44.83842840285206,
    'Demo review saloon',
    'Bordeaux',
    'France',
    'BAR',
    NULL,
    TRUE,
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM saloon WHERE name = 'Saloons Review Demo'
);

INSERT INTO `user` (
    email,
    password,
    first_name,
    last_name,
    user_name,
    description,
    birth_date,
    city,
    postal_code,
    created_at,
    is_premium,
    profile_status,
    auth_provider,
    img_url,
    profile_image_updated_at
)
SELECT
    'apple-review-camille-demo@saloons.fr',
    NULL,
    'Camille',
    'Demo',
    'Camille Demo',
    'Profil de démonstration utilisé uniquement pendant la revue App Store.',
    '1998-04-12',
    'Bordeaux',
    '33000',
    NOW(),
    FALSE,
    'ACTIVE',
    'EMAIL',
    'https://api.saloons.fr/images/Camille.png',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE email = 'apple-review-camille-demo@saloons.fr'
);

INSERT INTO `user` (
    email,
    password,
    first_name,
    last_name,
    user_name,
    description,
    birth_date,
    city,
    postal_code,
    created_at,
    is_premium,
    profile_status,
    auth_provider,
    img_url,
    profile_image_updated_at
)
SELECT
    'apple-review-alex-demo@saloons.fr',
    NULL,
    'Alex',
    'Demo',
    'Alex Demo',
    'Profil de démonstration utilisé uniquement pendant la revue App Store.',
    '1997-09-24',
    'Bordeaux',
    '33000',
    NOW(),
    FALSE,
    'ACTIVE',
    'EMAIL',
    'https://api.saloons.fr/images/Alex.png',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE email = 'apple-review-alex-demo@saloons.fr'
);
