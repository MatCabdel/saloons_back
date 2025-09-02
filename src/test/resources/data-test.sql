-- USERS
INSERT INTO
    "user" (
        id,
        email,
        password,
        first_name,
        last_name,
        user_name,
        description,
        birth_date,
        city,
        img_url
    )
VALUES (
        1,
        'alice@example.com',
        'password',
        'Alice',
        'Wonder',
        'Alice',
        'Test user Alice',
        '1990-01-01',
        'Paris',
        'https://example.com/alice.jpg'
    ),
    (
        2,
        'bob@example.com',
        'password',
        'Bob',
        'Builder',
        'Bob',
        'Test user Bob',
        '1985-05-10',
        'Lyon',
        'https://example.com/bob.jpg'
    );

-- ROLES (ElementCollection, so H2 creates a table USER_ROLES)
INSERT INTO
    "user_roles" (user_id, roles)
VALUES (1, 'USER'),
    (2, 'USER');

-- SALOONS
INSERT INTO
    saloon (
        id,
        name,
        img_url,
        visitor_number,
        created_at,
        longitude,
        latitude,
        address
    )
VALUES (
        1,
        'Le Sherlock',
        'https://example.com/sherlock.jpg',
        10,
        CURRENT_TIMESTAMP,
        44.838357,
        -0.575559,
        '100 rue des remparts, Bordeaux'
    ),
    (
        2,
        'Le Vintage Café',
        'https://example.com/vintage.jpg',
        5,
        CURRENT_TIMESTAMP,
        44.838929,
        -0.568325,
        '137 rue des remparts, Bordeaux'
    );

-- SESSIONS
INSERT INTO
    saloon_sessions (
        id,
        user_id,
        saloon_id,
        connected_at,
        disconnected_at
    )
VALUES (
        1,
        1,
        1,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        2,
        2,
        2,
        CURRENT_TIMESTAMP,
        NULL
    );

-- USER LIKES
INSERT INTO
    user_likes (id, liker_id, liked_id)
VALUES (1, 1, 2),
    (2, 2, 1);

-- USER MATCHES
INSERT INTO
    user_matches (
        id,
        user1_id,
        user2_id,
        matched_at
    )
VALUES (1, 1, 2, CURRENT_TIMESTAMP);

-- CONVERSATIONS
INSERT INTO conversation (id) VALUES (1), (2);

-- PARTICIPANTS
INSERT INTO
    conversation_participants (conversation_id, user_id)
VALUES (1, 1),
    (1, 2),
    (2, 2);

-- MESSAGES
INSERT INTO
    message (
        id,
        conversation_id,
        sender_id,
        content,
        sent_at
    )
VALUES (
        1,
        1,
        1,
        'Hello Bob!',
        CURRENT_TIMESTAMP
    ),
    (
        2,
        1,
        2,
        'Hello Alice!',
        CURRENT_TIMESTAMP
    );