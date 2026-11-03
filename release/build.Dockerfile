# Используется только сборщиком релиза. Пользователь получает готовые файлы.
FROM node:24.11.0-bookworm-slim AS client-build
WORKDIR /build/client
COPY client/package*.json ./
RUN npm ci --no-audit --no-fund
COPY client/ ./
# Кэш мира в образе сборки отсутствует: его создаст сервер при запуске дистрибутива.
RUN rm -rf cache \
    && npm run build \
    && test -f dist/index.html \
    && test -f dist/grass.png \
    && test -f dist/player.png

FROM maven:3.9.11-eclipse-temurin-21 AS server-build
WORKDIR /build/server
COPY server/pom.xml ./
COPY server/src ./src
RUN mvn -B -ntp clean verify

FROM scratch AS artifacts
COPY --from=client-build /build/client/dist /client
COPY --from=server-build /build/server/target/iivgrpg-server.jar /server/iivgrpg-server.jar
