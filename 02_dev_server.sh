#!/usr/bin/env bash
# 02 — Roq dev server with live reload (Quarkus dev mode).
#
# Serves http://localhost:8099 from the committed data/*.json. Edit content/,
# templates/ or data/ and the browser updates. No database needed. Ctrl-C to stop.
set -euo pipefail
cd "$(dirname "$0")"
# shellcheck source=_env.sh
source ./_env.sh

free_port "$ROQ_PORT"
echo "==> Roq dev server on http://localhost:$ROQ_PORT  (live reload; Ctrl-C to stop)"
echo "    Java: $(java -version 2>&1 | head -1)"
exec mvn quarkus:dev -Dquarkus.http.port="$ROQ_PORT"
