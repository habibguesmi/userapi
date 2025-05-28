# Utilise une image Java officielle
FROM eclipse-temurin:17-jdk

# Répertoire de travail
WORKDIR /app

# Copie les fichiers dans l'image
COPY . /app

# Donne les droits d’exécution à mvnw
RUN chmod +x ./mvnw

# Build du projet
RUN ./mvnw clean install -DskipTests

# Démarrage de l'application
CMD ["java", "-jar", "target/*.jar"]
