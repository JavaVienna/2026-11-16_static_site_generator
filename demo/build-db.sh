#!/usr/bin/env bash
# Rebuild demo/demo.db from demo/seed.sql. Run after editing the seed.
# The .db is committed too (zero-setup builds); this just regenerates it from the readable source.
set -euo pipefail
cd "$(dirname "$0")"
command -v sqlite3 >/dev/null 2>&1 || { echo "ERROR: sqlite3 not found."; exit 1; }
rm -f demo.db
sqlite3 demo.db < seed.sql
echo "Built demo.db from seed.sql:"
sqlite3 demo.db 'SELECT display_order, name, level FROM sponsor ORDER BY display_order;'
