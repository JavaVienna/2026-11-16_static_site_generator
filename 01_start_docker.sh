#!/usr/bin/env bash
# 01 — start the raffle Postgres (our data source) and check the raffle app.
#
# The exporter (04) reads this Postgres and downloads images from the raffle app
# on :8080. The Roq dev/generate steps (02/03) do NOT need either — they read the
# committed data/*.json.
set -euo pipefail
cd "$(dirname "$0")"
# shellcheck source=_env.sh
source ./_env.sh

echo "==> Starting raffle Postgres (docker compose) in $RAFFLE_DIR"
( cd "$RAFFLE_DIR" && docker compose up -d postgres )

echo "==> Waiting for Postgres to be ready on localhost:15432 ..."
until docker exec java-vienna-portal-postgres pg_isready -U juguser -d jugviennaraffle >/dev/null 2>&1; do
  sleep 1
done
echo "    Postgres ready."

echo "==> Checking the raffle app on :8080 (needed only for image export) ..."
if [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/ || true)" = "200" ]; then
  echo "    Raffle app is up — image export will work."
else
  echo "    Raffle app is NOT running. It is only needed when you re-export images."
  echo "    Start it with:  (cd \"$RAFFLE_DIR\" && ./mvnw spring-boot:run)"
fi
echo "Done. Next: ./02_dev_server.sh  (live preview)  or  ./03_generate_static_files.sh"
