# syntax=docker/dockerfile:1

# =====================================================================
# Immagine unica: Angular compilato + backend Spring Boot.
# Il frontend viene servito dal backend sulla stessa porta, quindi non
# servono CORS, reverse proxy o un secondo servizio (utile sul piano
# free di Render, dove le ore disponibili sono condivise).
# =====================================================================

# ---------- 1. Build del frontend Angular ----------
FROM node:22-alpine AS frontend
WORKDIR /build/frontend

# Dipendenze prima del codice: sfrutta la cache dei layer Docker.
COPY Frontend/Angular/package.json Frontend/Angular/package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY Frontend/Angular/ ./
# La configurazione "production" usa environment.production.ts (backendUrl: /api).
RUN npm run build -- --configuration production


# ---------- 2. Build del backend Spring Boot ----------
FROM eclipse-temurin:27-jdk AS backend
WORKDIR /build/backend

COPY Backend/calibrazione/ ./

# Il frontend compilato entra nel jar come risorsa statica.
COPY --from=frontend /build/frontend/dist/Angular/browser/ ./src/main/resources/static/

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon


# ---------- 3. Immagine finale ----------
FROM eclipse-temurin:27-jre
WORKDIR /app

COPY --from=backend /build/backend/build/libs/calibrazione-0.0.1-SNAPSHOT.jar /app/app.jar

# Render imposta PORT (di default 10000); in locale si puo' usare -e PORT=8080.
EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=60", \
    "-XX:+UseSerialGC", \
    "-XX:MaxMetaspaceSize=128m", \
    "-Xss512k", \
    "-jar", "/app/app.jar"]
