# Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Kopiuj pliki projektu
COPY pom.xml .
COPY src ./src

# Buduj aplikację (target/*.jar)
RUN mvn clean package -DskipTests

# Etap 2: Obraz produkcyjny – mały, bezpieczny, distroless
FROM gcr.io/distroless/java17-debian11

WORKDIR /app

# Skopiuj zbudowany plik .jar z poprzedniego etapu
COPY --from=build /app/target/*.jar app.jar

# Domyślnie uruchamiaj na profilu 'default', można nadpisać przez SPRING_PROFILES_ACTIVE
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:stag}", "-jar", "app.jar"]
