#!/bin/sh
# Запускаем от пользователя хоста: сохраняем его UID/GID и готовим Docker volumes.
set -eu
cd "$(dirname "$0")/.."
if [ "$(id -u)" = 0 ]; then
    echo 'Запустите настройку от обычного пользователя, без sudo.' >&2
    exit 1
fi
export LOCAL_UID="$(id -u)"
export LOCAL_GID="$(id -g)"
# Сохраняем остальные настройки .env, например порты клиента и сервера.
task_env_tmp=$(mktemp .env.XXXXXX)
trap 'rm -f "$task_env_tmp"' EXIT HUP INT TERM
if [ -f .env ]; then
    awk '!/^(export[[:space:]]+)?LOCAL_(UID|GID)=/' .env > "$task_env_tmp"
fi
printf '\nLOCAL_UID=%s\nLOCAL_GID=%s\n' "$LOCAL_UID" "$LOCAL_GID" >> "$task_env_tmp"
mv "$task_env_tmp" .env
# Docker иначе создаёт отсутствующие точки монтирования на хосте от root.
mkdir -p client/node_modules client/dist

docker compose build client server
# Перед изменением владельцев останавливаем процессы, использующие тома.
docker compose stop client server db
# Единственные процессы от root — разовое исправление владельцев старых томов.
# Содержимое исходников и БД не меняем, исправляем только владельцев.
docker compose run --rm --no-deps --user 0:0 --entrypoint sh --volume "$(pwd)/client:/host-client:rw,z" client -c '
    chown -R "$LOCAL_UID:$LOCAL_GID" /home/developer/.npm /workspace/client/node_modules /workspace/client/dist
    chown "$LOCAL_UID:$LOCAL_GID" /host-client/node_modules /host-client/dist
'
docker compose run --rm --no-deps --user 0:0 --entrypoint sh server -c '
    chown -R "$LOCAL_UID:$LOCAL_GID" /home/developer/.m2 /build/server /run/iivgrpg
'
docker compose run --rm --no-deps --user 0:0 --entrypoint sh db -c '
    chown -R "$LOCAL_UID:$LOCAL_GID" /var/lib/mysql
'
printf 'Готово: UID=%s GID=%s. Запуск: docker compose up -d\n' "$LOCAL_UID" "$LOCAL_GID"
