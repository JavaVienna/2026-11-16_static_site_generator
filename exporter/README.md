# exporter — raffle Postgres → markdown content for java.wien

Turns the private raffle database into **editable, diffable markdown files** (plus a
little JSON for list pages and the images), which are then rsync'd into the Roq site at
the repo root (this `exporter/` lives inside that site repo). The database stays the
source of truth; the markdown is a reviewable artifact you look at as a `git diff` before
publishing — you don't hand-edit it (a re-export would overwrite it).

```
raffle Postgres ──(ExportContent.java, read-only JDBC)──► out/ ──(02_sync.sh, rsync)──► .. (site repo root)
```

## Files

| File | What |
|------|------|
| `ExportContent.java` | JBang exporter. Reads the DB, writes `out/`. `//JAVA 21` — jbang fetches the JDK itself. |
| `01_export.sh` | run the exporter → `out/` |
| `02_sync.sh` | rsync `out/` into the site repo root `..` (with `--delete`), then show the git diff |
| `out/` | staging output (gitignored) |

## Output shape (`out/`)

```
out/content/events/<slug>.md     one markdown file per PUBLISHED event
out/content/speakers/<slug>.md   one markdown file per ACTIVE speaker
out/data/eventsByYear.json       events grouped by year (events list page)
out/data/home.json               3 recent past + next upcoming (home page)
out/data/speakersList.json       slim speaker cards (speakers list page)
out/data/sponsors.json           active sponsorships
out/data/companies.json          company cards
out/public/img/<uuid>.<ext>      downloaded images
```

Each markdown file = **YAML front matter** (structured fields the Roq layouts read as
`page.data.*`) + a **markdown body** (the prose: event description / speaker bio). The
body is what a human would actually edit; it renders via `{#insert /}` in the layout.

Why events/speakers are markdown but sponsors/companies are JSON: events and speakers have
real prose (agendas, bios) that belongs in a markdown body and benefits from per-item
diffs. Sponsors/companies are logos + links + levels with no prose, and are consumed as
site-wide lists, so they stay as small JSON data files.

## Usage

```bash
# 1. make sure the raffle Postgres + app are up (for image download):
(cd .. && ./01_start_docker.sh)

# 2. export DB -> markdown + data + images (into ./out):
./01_export.sh

# 3. sync into the site and review what changed:
./02_sync.sh
#    -> prints `git status` in the site repo; review with `git -C .. diff`

# 4. regenerate the static site:
(cd .. && ./03_generate_static_files.sh)
```

Connection defaults (override via env): `JUG_DB_URL`
(`jdbc:postgresql://localhost:15432/jugviennaraffle`), `JUG_DB_USER` (`juguser`),
`JUG_DB_PASS` (`jugpassword`), `JUG_IMAGE_BASE` (`http://localhost:8080/api/images/`),
`OUT_DIR` (`out`). `02_sync.sh` honors `SITE_DIR` (`..`, the site repo root).

## Gotchas

- **Keep null keys in front matter.** Qute throws "property not found" on a `JsonObject`
  for an *absent* key — even inside `{#if}`. So the exporter emits every expected field,
  null included (do not add `NON_NULL` to the YAML mapper).
- **`--delete` on sync** prunes events/speakers/images that left the DB (unpublished /
  deactivated / removed). That is intentional — the diff shows removals too.
- Only `event.status='PUBLISHED'` and `speaker.active=true` are exported.
