# Architecture et Deploiement Back SALOONS

Ce document resume l'architecture backend SALOONS, les environnements staging/production, la configuration CORS, le VPS, Traefik et les commandes de verification.

## Vue d'ensemble

Le backend SALOONS est une application Spring Boot exposee par Traefik.

Images Docker attendues :

```text
matcabdel/saloons-api:staging
matcabdel/saloons-api:production
```

Domaines API :

```text
https://staging-api.saloons.fr -> backend staging
https://api.saloons.fr         -> backend production
```

Le front est separe en landing/app :

```text
staging.saloons.fr      -> landing staging
staging-app.saloons.fr  -> app staging
saloons.fr              -> landing production
www.saloons.fr          -> landing production
app.saloons.fr          -> app production
```

## Profils Spring

Fichiers principaux :

```text
src/main/resources/application.yml
src/main/resources/application-staging.yml
src/main/resources/application-production.yml
```

Le profil est fourni par Docker via :

```text
SPRING_PROFILES_ACTIVE=staging
SPRING_PROFILES_ACTIVE=production
```

## Configuration staging

Fichier :

```text
src/main/resources/application-staging.yml
```

Points importants :

```yaml
cors:
  allowedOrigins: ${CORS_ALLOWED_ORIGINS_STAGING:https://staging-app.saloons.fr,https://staging.saloons.fr,https://localhost,capacitor://localhost}

app:
  base-url: ${APP_BASE_URL_STAGING}
  frontend-url: ${APP_FRONTEND_URL_STAGING:https://staging-app.saloons.fr}
```

`.env` staging recommande sur le VPS :

```env
CLIENT_URL_STAGING=https://staging-app.saloons.fr
APP_BASE_URL_STAGING=https://staging-api.saloons.fr
APP_FRONTEND_URL_STAGING=https://staging-app.saloons.fr
CORS_ALLOWED_ORIGINS_STAGING=https://staging-app.saloons.fr,https://staging.saloons.fr,https://localhost,capacitor://localhost
```

`CLIENT_URL_STAGING` et `APP_FRONTEND_URL_STAGING` peuvent avoir la meme valeur. C'est normal : les liens applicatifs doivent pointer vers l'app, pas vers la landing.

## Configuration production

Fichier :

```text
src/main/resources/application-production.yml
```

Points importants :

```yaml
cors:
  allowedOrigins: ${CORS_ALLOWED_ORIGINS_PROD:https://app.saloons.fr,https://saloons.fr,https://www.saloons.fr,capacitor://localhost,https://localhost}

app:
  base-url: ${APP_BASE_URL_PROD}
  frontend-url: ${APP_FRONTEND_URL_PROD:https://app.saloons.fr}
```

`.env` production recommande sur le VPS :

```env
CLIENT_URL_PROD=https://app.saloons.fr
APP_BASE_URL_PROD=https://api.saloons.fr
APP_FRONTEND_URL_PROD=https://app.saloons.fr
CORS_ALLOWED_ORIGINS_PROD=https://app.saloons.fr,https://saloons.fr,https://www.saloons.fr,https://localhost,capacitor://localhost
```

## CORS

Le CORS est configure dans :

```text
src/main/java/com/backend_project_template/config/CorsConfig.java
```

Il lit :

```text
cors.allowedOrigins
```

Puis applique les origines a WebMvc et a `CorsConfigurationSource`.

Origines importantes a garder :

```text
capacitor://localhost -> iOS/Android Capacitor
https://localhost     -> webview/native selon contexte
staging-app/app       -> front applicatif
staging/landing       -> landing et contact public
```

Si l'app mobile affiche "Erreur reseau" apres une modification CORS, verifier que `capacitor://localhost` est toujours present.

## Liens applicatifs backend

Le service de reset password utilise :

```text
app.frontend-url
```

Fichier :

```text
src/main/java/com/backend_project_template/domains/auth/PasswordResetService.java
```

Les liens de reset doivent pointer vers :

```text
staging -> https://staging-app.saloons.fr/mot-de-passe-oublie?token=...
prod    -> https://app.saloons.fr/mot-de-passe-oublie?token=...
```

Ils ne doivent pas pointer vers la landing.

## Docker backend

Dockerfile :

```text
Dockerfile
```

Le build :

1. compile avec Maven;
2. produit le jar;
3. lance `java -jar app.jar`.

Build manuel staging :

```bash
docker build -t matcabdel/saloons-api:staging .
docker push matcabdel/saloons-api:staging
```

Build manuel production :

```bash
docker build -t matcabdel/saloons-api:production .
docker push matcabdel/saloons-api:production
```

## CI/CD backend

Workflow :

```text
.github/workflows/cd.yml
```

Declenchement :

```text
push sur staging    -> image matcabdel/saloons-api:staging
push sur production -> image matcabdel/saloons-api:production
```

La CD :

1. build l'image Docker;
2. push Docker Hub;
3. SSH sur le VPS;
4. lance :

```bash
cd /home/$VPS_USER/backend/$TAG
docker compose pull
docker compose down && docker compose up -d
```

`TAG` vaut `staging` ou `production`.

## VPS backend

Structure attendue :

```text
/home/ubuntu/backend/staging/docker-compose.yml
/home/ubuntu/backend/staging/.env
/home/ubuntu/backend/production/docker-compose.yml
/home/ubuntu/backend/production/.env
```

## Compose staging backend

Structure actuellement attendue sur le VPS :

```yaml
services:
  mysql-staging:
    image: mysql:8.0
    container_name: mysql-staging
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME_STAGING}
      MYSQL_USER: ${DB_USER_STAGING}
      MYSQL_PASSWORD: ${DB_PASSWORD_STAGING}
    env_file:
      - .env
    volumes:
      - mysql_data_staging:/var/lib/mysql
    ports:
      - "127.0.0.1:3306:3306"
    networks:
      - traefik

  redis-staging:
    image: redis:7-alpine
    container_name: redis-staging
    restart: unless-stopped
    command: redis-server --appendonly yes
    volumes:
      - redis_data_staging:/data
    networks:
      - traefik

  spring-app-staging:
    image: matcabdel/saloons-api:staging
    container_name: spring-app-staging
    restart: unless-stopped
    depends_on:
      - mysql-staging
      - redis-staging
    env_file:
      - .env
    environment:
      SPRING_PROFILES_ACTIVE: staging
      REDIS_HOST_STAGING: redis-staging
      REDIS_PORT_STAGING: 6379
      GOOGLE_APPLICATION_CREDENTIALS: /run/secrets/firebase-service-account.json
    volumes:
      - uploads_staging:/app/uploads
      - /opt/secrets/firebase-service-account.json:/run/secrets/firebase-service-account.json:ro
    networks:
      - traefik
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.spring-app-staging.rule=Host(`staging-api.saloons.fr`)"
      - "traefik.http.routers.spring-app-staging.entrypoints=websecure"
      - "traefik.http.routers.spring-app-staging.tls.certresolver=letsencrypt"
      - "traefik.http.services.spring-app-staging.loadbalancer.server.port=8080"
      - "traefik.http.middlewares.websocket-headers.headers.customrequestheaders.X-Forwarded-Proto=https"
      - "traefik.http.routers.spring-app-staging.middlewares=websocket-headers"

volumes:
  mysql_data_staging:
  redis_data_staging:
  uploads_staging:

networks:
  traefik:
    external: true
```

## Compose production backend

La production suit la meme logique avec :

```text
spring-app-production
mysql-prod
redis-prod
matcabdel/saloons-api:production
Host(`api.saloons.fr`)
```

Le port interne Spring est :

```text
8080
```

Traefik doit donc utiliser :

```yaml
- "traefik.http.services.<service>.loadbalancer.server.port=8080"
```

## Traefik

Traefik tourne dans le container :

```text
traefik
```

Le reseau Docker partage est :

```text
traefik
```

Tous les services exposes doivent etre sur ce reseau et avoir :

```yaml
networks:
  - traefik
```

Les certificats sont geres par Let's Encrypt via :

```yaml
tls.certresolver=letsencrypt
```

Le dossier VPS `~/traefik/letsencrypt` contient les certificats. En usage normal, il ne faut pas modifier ce dossier a la main.

## DNS

Les domaines doivent pointer vers l'IP publique du VPS.

Production :

```text
api.saloons.fr
app.saloons.fr
saloons.fr
www.saloons.fr
```

Staging :

```text
staging-api.saloons.fr
staging-app.saloons.fr
staging.saloons.fr
```

Verifier :

```bash
dig +short A staging-api.saloons.fr
dig +short A staging-app.saloons.fr
dig +short A staging.saloons.fr
```

Une erreur d'un chiffre dans l'IP suffit a provoquer un timeout.

## Commandes VPS utiles

Lister les containers actifs :

```bash
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"
```

Lister aussi les containers arretes :

```bash
docker ps -a --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"
```

Relancer le backend staging :

```bash
cd ~/backend/staging
docker compose pull
docker compose down
docker compose up -d
```

Voir les logs backend staging :

```bash
docker logs spring-app-staging --tail=200
```

Verifier les variables injectees :

```bash
docker exec spring-app-staging printenv | grep -E "CORS|CLIENT_URL|APP_FRONTEND|APP_BASE|SPRING_PROFILES"
```

Tester l'API staging :

```bash
curl -I https://staging-api.saloons.fr/auth/login
```

Une reponse type :

```text
HTTP/2 401
allow: POST
content-type: application/json
```

est normale pour un `HEAD` sans authentification. Un `404 page not found` en `text/plain` indique souvent que Traefik ne trouve pas le backend.

Tester le router Traefik localement :

```bash
curl -kI -H "Host: staging-api.saloons.fr" https://127.0.0.1/auth/login
```

## Incidents connus

### Front staging OK mais app mobile staging en erreur reseau

Verifier que le backend staging tourne :

```bash
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"
```

Si `spring-app-staging` n'apparait pas :

```bash
cd ~/backend/staging
docker compose up -d
```

Puis :

```bash
curl -I https://staging-api.saloons.fr/auth/login
```

### `staging-api.saloons.fr` retourne `404 page not found`

Probable cause : aucun router Traefik ne pointe vers le backend staging, ou le backend staging est arrete.

Verifier :

```bash
docker ps -a | grep staging
docker inspect spring-app-staging --format '{{json .Config.Labels}}'
docker logs traefik --tail=300 2>&1 | grep -i "staging-api\|spring-app\|acme"
```

### CORS mobile casse

Verifier que `capacitor://localhost` est present dans :

```text
CORS_ALLOWED_ORIGINS_STAGING
CORS_ALLOWED_ORIGINS_PROD
```

## Ordre normal de livraison staging

1. Merger le back vers `staging`.
2. La CD back build/push `matcabdel/saloons-api:staging`.
3. La CD back deploie `/home/ubuntu/backend/staging`.
4. Merger le front vers `staging`.
5. La CD front build/push `saloons-app:staging` et `saloons-landing:staging`.
6. La CD front deploie `/home/ubuntu/frontend/staging`.
7. Verifier :

```bash
curl -I https://staging.saloons.fr
curl -I https://staging-app.saloons.fr
curl -I https://staging-api.saloons.fr/auth/login
```

## Ordre normal de livraison production

1. Merger back `staging` vers `production`.
2. La CD back deploie `matcabdel/saloons-api:production`.
3. Merger front `staging` vers `production`.
4. La CD front deploie `saloons-app:production` et `saloons-landing:production`.
5. Verifier :

```bash
curl -I https://saloons.fr
curl -I https://www.saloons.fr
curl -I https://app.saloons.fr
curl -I https://api.saloons.fr/auth/login
```
