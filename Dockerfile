FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
COPY checkstyle.xml ./checkstyle.xml

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/backend*.jar app.jar

# Le fichier de credentials Firebase sera monté via docker-compose (volume)
# Chemin attendu dans le container : /app/secret/firebase-service-account.json

EXPOSE 8080

CMD ["java", "-Duser.timezone=Europe/Paris", "-jar", "app.jar"]
