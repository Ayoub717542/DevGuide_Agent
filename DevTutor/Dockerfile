# Stage 1: Build the application
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# 1. Copy Maven configuration first
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# 2. Make Maven Wrapper executable
RUN chmod +x mvnw

# 3. Download dependencies
RUN ./mvnw -B dependency:go-offline

# 4. Copy application source code
COPY src/ src/

# 5. Build the JAR
RUN ./mvnw -B -DskipTests package

RUN cp target/*.jar app.jar && java -Djarmode=tools -jar app.jar extract --layers --destination extracted


# Stage 2: Run the application
FROM eclipse-temurin:25-jre

WORKDIR /app

# 6. Copy the JAR layers, most-changing last
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
