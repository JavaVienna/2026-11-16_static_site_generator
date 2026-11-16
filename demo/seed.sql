-- Demo database for the talk: "Generating HTML from Markdown and from your Database".
-- This SQLite file ships IN the repo (demo/demo.db) and is read live by Roq at build time
-- (see src/main/java/wien/java/DemoDb.java). No external DB, no network, no Docker — the
-- "...from your Database" half of the talk, reproducibly, from one committed file.
--
-- Rebuild the .db from this file:  ./demo/build-db.sh   (or: sqlite3 demo/demo.db < demo/seed.sql)
-- Keep this seed readable/diffable; the .db is the convenience artifact built from it.

DROP TABLE IF EXISTS sponsor;

CREATE TABLE sponsor (
    slug          TEXT NOT NULL,   -- links to the company page /companies/<slug>/
    name          TEXT NOT NULL,
    level         TEXT NOT NULL,   -- Diamond / Gold / ...
    logo          TEXT,            -- "/img/..." local, or an external https URL
    website_url   TEXT,
    display_order INTEGER NOT NULL -- lower = shown first
);

INSERT INTO sponsor (slug, name, level, logo, website_url, display_order) VALUES
  ('dynatrace-3',   'dynatrace',    'Diamond',
     'https://secure.meetupstatic.com/photos/sponsor/a/5/0/6/iab120x90_2742246.jpeg',
     'https://dynatrace.com', 1),
  ('posedio-gmbh-1','Posedio GmbH', 'Gold',
     '/img/1d5fcaba-89c1-4521-8986-871767af23c7.jpg',
     'https://posedio.com', 2);
