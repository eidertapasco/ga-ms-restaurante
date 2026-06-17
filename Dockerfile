# ─── ETAPA 1: Compilar el proyecto ───────────────────────────────────────────
# Usamos una imagen con Java 21 y Maven para construir el .jar
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar caché de capas
# (si no cambia el pom, Maven no vuelve a descargar dependencias)
COPY pom.xml .

# Copiamos la librería local ANTES de descargar dependencias
COPY libs ./libs
# Instalamos el .jar local en el repositorio Maven del contenedor
RUN mvn install:install-file \
    -Dfile=libs/ga-lib-security-0.0.1-SNAPSHOT.jar \
    -DgroupId=co.edu.sena \
    -DartifactId=ga-lib-security \
    -Dversion=0.0.1-SNAPSHOT \
    -Dpackaging=jar \
    -DgeneratePom=true

RUN mvn dependency:go-offline -B

# Ahora copiamos el código fuente
COPY src ./src

# Compilamos y empaquetamos — saltamos los tests (ya los probaste en Postman)
RUN mvn clean package -DskipTests

# ─── ETAPA 2: Imagen final liviana ───────────────────────────────────────────
# Solo necesitamos Java para correr el .jar, no Maven
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos el .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone el contenedor
EXPOSE 8080

# Comando para arrancar la app con perfil prod
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]