#!/usr/bin/env bash
# 99 — stop demo helpers: Roq dev/preview servers + the raffle Postgres container.
#
# Data persists in the Postgres volume. The raffle Spring Boot app (if you started
# it for image export) is not managed here — stop that process yourself.
set -uo pipefail
cd "$(dirname "$0")"
# shellcheck source=_env.sh
source ./_env.sh || true

echo "==> Freeing Roq/preview ports ($ROQ_PORT, $PREVIEW_PORT) ..."
free_port "$ROQ_PORT"
free_port "$PREVIEW_PORT"

echo "==> Stopping raffle Postgres container ..."
( cd "$RAFFLE_DIR" && docker compose stop postgres ) || true

echo "Done. (If the raffle app is still running on :8080, stop that process manually.)"
