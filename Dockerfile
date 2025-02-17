FROM gradle:8.12.1-jdk21 as build

WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src ./src

RUN gradle bootJar

FROM openjdk:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/
CMD ["sh", "-c", "java -jar $(ls *.jar)"]
