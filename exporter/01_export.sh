#!/usr/bin/env bash
# 01 — export the raffle Postgres into markdown + data files under ./out/.
#
# Writes:
#   out/content/events/<slug>.md     one markdown file per published event
#   out/content/speakers/<slug>.md   one markdown file per active speaker
#   out/content/companies/<slug>.md  one markdown file per company shown on the list
#   out/data/*.json                  eventsByYear, home, speakersList, sponsors, companies
#   out/public/img/*                 downloaded images
# (Markdown + JSON + images only — never HTML. Roq renders the HTML.)
#
# Needs: the raffle Postgres running (see ../01_start_docker.sh) and, for image
# download, the raffle app on :8080. jbang handles Java 21 automatically (//JAVA 21).
# Connection defaults overridable via env: JUG_DB_URL, JUG_DB_USER, JUG_DB_PASS,
# JUG_IMAGE_BASE, OUT_DIR.
set -euo pipefail
cd "$(dirname "$0")"

command -v jbang >/dev/null 2>&1 || { echo "ERROR: jbang not found (sdk install jbang)."; exit 1; }

echo "==> Exporting Postgres -> markdown content + data (./out) ..."
jbang ExportContent.java
echo "==> Done. Next: ./02_sync.sh  (rsync into the site repo and show the git diff)"
