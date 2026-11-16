#!/usr/bin/env bash
# 05 — preview the generated static site as plain files (no JVM, no DB).
#
# Serves target/roq/ on http://localhost:8097. This is exactly what gets deployed.
set -euo pipefail
cd "$(dirname "$0")"
# shellcheck source=_env.sh
source ./_env.sh

if [ ! -d target/roq ]; then
  echo "No target/roq/ yet — run ./03_generate_static_files.sh first." >&2
  exit 1
fi
free_port "$PREVIEW_PORT"
echo "==> Serving target/roq/ at http://localhost:$PREVIEW_PORT  (Ctrl-C to stop)"
exec python3 -m http.server -d target/roq "$PREVIEW_PORT"
