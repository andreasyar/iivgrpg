FROM eclipse-temurin:21-jre-jammy
RUN mkdir -p /app /run/iivgrpg && chown 10001:10001 /run/iivgrpg
COPY server/iivgrpg-server.jar /app/server.jar
COPY config/server.xml /app/config.xml
WORKDIR /run/iivgrpg
USER 10001:10001
EXPOSE 8015
ENTRYPOINT ["java", "-jar", "/app/server.jar", "/app/config.xml"]
