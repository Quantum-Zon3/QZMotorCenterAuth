# Etapa 1: Construcción
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Copiamos solo los archivos de configuración para aprovechar caché
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew gradlew.bat ./

# Descargamos las dependencias
RUN ./gradlew dependencies --no-daemon || true

# Copiamos el resto del código
COPY src ./src

# Construimos la aplicación excluyendo los tests (ya que no hay base de datos levantada en este punto)
RUN ./gradlew clean build -x test --no-daemon

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el JAR generado desde la etapa de construcción
COPY --from=builder /app/build/libs/QZMotorCenterAuth-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto
EXPOSE 8080

# Ejecutamos la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
