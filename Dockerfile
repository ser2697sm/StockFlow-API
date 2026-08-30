# Primera fase: compilar la aplicación
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos primero Maven Wrapper y pom.xml para aprovechar la caché
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiamos el código después de descargar las dependencias
COPY src ./src

# Los tests ya los ejecuta GitHub Actions
RUN ./mvnw clean package -DskipTests -B


# Segunda fase: ejecutar la aplicación
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]