FROM maven:3.9.11-eclipse-temurin-21
ARG LOCAL_UID=1000
ARG LOCAL_GID=1000
RUN apt-get update && apt-get install -y --no-install-recommends python3 \
    && rm -rf /var/lib/apt/lists/*
ENV HOME=/home/developer MAVEN_CONFIG=/home/developer/.m2
RUN mkdir -p /home/developer/.m2 /build/server /run/iivgrpg /workspace/server \
    && chown -R "${LOCAL_UID}:${LOCAL_GID}" /home/developer /build/server /run/iivgrpg /workspace
WORKDIR /workspace/server
USER ${LOCAL_UID}:${LOCAL_GID}
