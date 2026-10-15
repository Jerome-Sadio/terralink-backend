FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/uploads/inspections

COPY build/libs/terralink-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xmx400m -Xms200m -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
