# Saloon Backend

Ce projet est une API REST basée sur Spring Boot 3.4, conçue pour gérer l’application Saloons PRO.

---

## 🚀 Démarrage rapide

1. **Configuration**

   - Copie `.env.sample` en `.env` et adapte les variables (BDD, JWT, etc).
   - Configure la base de données dans `src/main/resources/application.yml`.

2. **Installation**

   ```bash
   ./mvnw clean install
   ```

3. **Lancement**

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Documentation API**
   - Swagger UI disponible sur [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - Spécification OpenAPI sur [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📁 Structure du projet

```
src/
  main/
    java/
      com.backend_project_template/
        domains/         # Entités métier (User, Saloon, etc.)
        security/        # Sécurité (JWT, config Spring Security)
        ...              # Autres modules
    resources/
      application.yml    # Config principale
      ...
  test/
    java/
      ...                # Tests unitaires et d'intégration
uploads/                 # Stockage des fichiers uploadés
Dockerfile               # Build image Docker
docker-compose.yml       # Orchestration (à compléter)
.env                     # Variables d'environnement
```

---

## 🔒 Sécurité

- Utilise **Spring Security** (JWT, rôles, endpoints protégés).
- Les endpoints `/admin/**` sont réservés aux admins.
- Les endpoints `/user/**` sont accessibles aux utilisateurs connectés.
- Swagger et `/v3/api-docs` sont accessibles en développement uniquement.

---

## 📝 Qualité & Conventions

- **Checkstyle** : règles dans `checkstyle.xml` (lance `mvn checkstyle:check`)
- **Prettier** : formatage automatique (lance `npm run prettier`)
- **Husky** : hooks git pour valider les commits

---

## 🧪 Tests

- Lance tous les tests avec :
  ```bash
  ./mvnw test
  ```
- Utilise JUnit et Surefire.

---

## 🐳 Docker & CI/CD

- Build image Docker :
  ```bash
  docker build -t saloon-backend .
  ```
- Orchestration via `docker-compose.yml` (à compléter).
- Pipeline CI/CD dans `.github/workflows/`.

---

## 📚 Documentation API

- Swagger UI : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON : [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🤝 Contribuer

- Respecte les conventions de code et de commit.
- Les PR sont soumises à validation et review.
- Les branches protégées : `production`, `staging`, `development`.

---

## 📦 Principales dépendances

- Spring Boot 3.4
- Spring Security
- Spring Data JPA (Hibernate)
- MySQL Driver
- Lombok
- Springdoc OpenAPI (Swagger)
- JUnit, Surefire

---

## 📄 Licence

Ce projet est sous licence MIT.

---

**Pour toute question ou bug, ouvre une issue ou contacte l’équipe technique.**
