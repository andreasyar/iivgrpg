FROM node:24.11.0-bookworm-slim
ARG LOCAL_UID=1000
ARG LOCAL_GID=1000
ENV HOME=/home/developer
RUN mkdir -p /home/developer/.npm /workspace/client/node_modules /workspace/client/dist /workspace/client/cache \
    && chown -R "${LOCAL_UID}:${LOCAL_GID}" /home/developer /workspace
WORKDIR /workspace/client
USER ${LOCAL_UID}:${LOCAL_GID}
