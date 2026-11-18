#!/usr/bin/env bash
# 02 — rsync the exported content into the site repo (the parent dir), then show changes.
#
# The DB is the source of truth; these synced files are a reviewable artifact. After
# sync, `git diff` in the site repo shows exactly which events/speakers/images changed since
# the last export. --delete prunes items that disappeared from the DB (unpublished events,
# deactivated speakers, removed images).
set -euo pipefail
cd "$(dirname "$0")"

SITE="${SITE_DIR:-..}"
OUT="${OUT_DIR:-out}"

[ -d "$OUT/content" ] || { echo "No $OUT/ — run ./01_export.sh first."; exit 1; }
[ -d "$SITE" ] || { echo "Site dir $SITE not found (set SITE_DIR)."; exit 1; }

echo "==> Syncing into $SITE ..."
mkdir -p "$SITE/content/events" "$SITE/content/speakers" "$SITE/content/companies" "$SITE/data" "$SITE/public/img"
rsync -a --delete "$OUT/content/events/"    "$SITE/content/events/"
rsync -a --delete "$OUT/content/speakers/"  "$SITE/content/speakers/"
rsync -a --delete "$OUT/content/companies/" "$SITE/content/companies/"
rsync -a --delete "$OUT/public/img/"        "$SITE/public/img/"
rsync -a --delete "$OUT/data/"              "$SITE/data/"

echo "==> Sync done."
if git -C "$SITE" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "==> Changes in $SITE (git status):"
  git -C "$SITE" status --short
  echo "   Review with:  git -C $SITE diff"
else
  echo "   ($SITE is not a git repo yet — 'git -C $SITE init' to get diffs on the next sync.)"
  echo "   events: $(ls "$SITE/content/events" | wc -l | tr -d ' ') files, speakers: $(ls "$SITE/content/speakers" | wc -l | tr -d ' ') files, companies: $(ls "$SITE/content/companies" | wc -l | tr -d ' ') files"
fi
echo "==> Then regenerate: (cd $SITE && ./03_generate_static_files.sh)"
