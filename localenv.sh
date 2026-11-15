#!/bin/bash

# Старый localenv.sh занимал имя rfr-mysql через docker run. Compose с тем же
# container_name отказывается стартовать, пока это имя занято. Свой контейнер
# виден в `compose ps` (короткий id — префикс полного) и остаётся. `tr` снимает
# `\r`, который docker.exe пишет в Git Bash, иначе префикс не совпадает. Остальные
# контейнеры с этим именем удаляются. `docker rm` оставляет том mysql-volume.
current=$(docker ps -aq -f name='^rfr-mysql$' | tr -d '\r')
if [ -n "$current" ]; then
  managed=$(docker compose ps -aq mysql | tr -d '\r')
  if [ "${managed#"$current"}" = "$managed" ]; then
    docker rm -f rfr-mysql
  fi
fi

docker compose up -d mysql --wait

