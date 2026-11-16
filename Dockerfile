FROM eclipse-temurin:21-jdk-alpine-3.24 AS build
WORKDIR /source
COPY . /source
RUN ./gradlew :app:shadowJar -x test

FROM eclipse-temurin:21-jre-alpine-3.24
WORKDIR /app
COPY --from=build --chown=app:app /source/app/build/libs/*.jar app.jar
EXPOSE 1099 1098
ENTRYPOINT ["java", "-cp", "/app/app.jar", "ude.obligatorio.servidor.ServidorRMI"]
