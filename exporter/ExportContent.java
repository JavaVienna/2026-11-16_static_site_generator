///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21
//DEPS org.postgresql:postgresql:42.7.4
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.1
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.1

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Read-only exporter: raffle Postgres -> MARKDOWN content files + data/*.json + images.
 *
 * Output layout (under ./out by default, override with OUT_DIR):
 *   out/content/events/<slug>.md     one markdown file per published event
 *   out/content/speakers/<slug>.md   one markdown file per active speaker
 *   out/data/*.json                  eventsByYear, home, sponsors, companies (site-wide)
 *   out/public/img/<uuid>.<ext>      downloaded images
 *
 * Each markdown file has YAML front matter (structured fields the layouts read as
 * page.data.*) and a markdown BODY (the prose: event description / speaker bio). The
 * body is what humans would actually edit; the body renders via {#insert /} in the
 * Roq layout. The DB stays the source of truth; 02_sync.sh rsyncs out/ into java-wien
 * so `git diff` shows exactly what changed.
 *
 * Run:  jbang ExportContent.java
 */
public class ExportContent {

    static final String DB_URL = env("JUG_DB_URL", "jdbc:postgresql://localhost:15432/jugviennaraffle");
    static final String DB_USER = env("JUG_DB_USER", "juguser");
    static final String DB_PASS = env("JUG_DB_PASS", "jugpassword");
    static final String IMAGE_BASE = env("JUG_IMAGE_BASE", "http://localhost:8080/api/images/");
    static final String OUT_DIR = env("OUT_DIR", "out");

    static final ZoneId VIENNA = ZoneId.of("Europe/Vienna");
    static final DateTimeFormatter DATE_DISPLAY =
            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm", Locale.ENGLISH);

    static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    // YAML for front matter: no leading "---" doc marker (we add our own fences).
    // Keep null keys: Qute throws "property not found" on a JsonObject for ABSENT keys
    // (even inside {#if}), so every expected field must be present (null is fine).
    static final ObjectMapper YAML = new ObjectMapper(
            new YAMLFactory()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));

    static final HttpClient HTTP = HttpClient.newHttpClient();

    // image uuid -> local web path ("/img/<uuid>.<ext>"), downloaded once
    static final Map<String, String> imageCache = new LinkedHashMap<>();

    // entity id -> final slug, resolved once over the whole population so every reference
    // to an id (from any page) yields the same slug. Built in main() before any export.
    static Map<Long, String> speakerSlugs = Map.of();
    static Map<Long, String> companySlugs = Map.of();
    static Map<Long, String> eventSlugs = Map.of();

    static String speakerSlug(long id, String name) { return speakerSlugs.getOrDefault(id, slug(name, id)); }
    static String companySlug(long id, String name) { return companySlugs.getOrDefault(id, slug(name, id)); }
    static String eventSlug(long id, String title) { return eventSlugs.getOrDefault(id, slug(title, id)); }

    public static void main(String[] args) throws Exception {
        Path out = Paths.get(OUT_DIR).toAbsolutePath();
        Path eventsDir = out.resolve("content/events");
        Path speakersDir = out.resolve("content/speakers");
        Path companiesDir = out.resolve("content/companies");
        Path dataDir = out.resolve("data");
        Path imgDir = out.resolve("public/img");
        for (Path p : List.of(eventsDir, speakersDir, companiesDir, dataDir, imgDir)) Files.createDirectories(p);

        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            System.out.println("Connected to " + DB_URL);

            // Resolve slugs once over the whole population so references stay consistent and
            // the id suffix only appears where two base slugs would otherwise collide.
            speakerSlugs = resolveSlugs(c, "SELECT id, name FROM speaker", "name");
            companySlugs = resolveSlugs(c, "SELECT id, name FROM company", "name");
            eventSlugs = resolveEventSlugs(c);

            List<Map<String, Object>> events = exportEvents(c, imgDir);
            List<Map<String, Object>> speakers = exportSpeakers(c, imgDir, events);
            List<Map<String, Object>> sponsors = exportSponsors(c, imgDir);
            List<Map<String, Object>> companies = exportCompanies(c, imgDir);

            // One markdown file per event / speaker (front matter + prose body).
            for (Map<String, Object> e : events) writeEventMarkdown(eventsDir, e);
            for (Map<String, Object> s : speakers) writeSpeakerMarkdown(speakersDir, s);
            // One markdown file per company the list page links to: has speakers OR is a
            // sponsor (current or past — the Hall of Fame tab lists past sponsors too).
            int companyPages = 0;
            for (Map<String, Object> co : companies) {
                boolean hasSpeakers = !((List<?>) co.getOrDefault("speakers", List.of())).isEmpty();
                boolean isSponsor = Boolean.TRUE.equals(co.get("isCurrentSponsor"))
                        || Boolean.TRUE.equals(co.get("wasSponsor"));
                if (hasSpeakers || isSponsor) {
                    writeCompanyMarkdown(companiesDir, c, co);
                    companyPages++;
                }
            }

            // Site-wide data for the list/home pages (not prose -> stays JSON).
            // (companies has no JSON: the companies list page reads the markdown collection.)
            writeJson(dataDir.resolve("eventsByYear.json"), groupByYearDesc(events));
            writeJson(dataDir.resolve("home.json"), homeData(events));
            writeJson(dataDir.resolve("speakersList.json"), speakersList(speakers));
            writeJson(dataDir.resolve("sponsors.json"), sponsors);

            System.out.printf("Done: %d event files, %d speaker files, %d company files, %d sponsors, %d companies, %d images%n",
                    events.size(), speakers.size(), companyPages, sponsors.size(), companies.size(), imageCache.size());
            System.out.println("Output in " + out + " — next: ./02_sync.sh");
        }
    }

    // ---------------- Markdown writers ----------------

    static void writeEventMarkdown(Path dir, Map<String, Object> e) throws Exception {
        String description = str(e.get("description"));
        Map<String, Object> fm = new LinkedHashMap<>();
        fm.put("layout", "event");
        fm.put("title", e.get("title"));
        fm.put("slug", e.get("slug"));
        fm.put("dateDisplay", e.get("dateDisplay"));
        fm.put("startDateTime", e.get("startDateTime"));
        fm.put("year", e.get("year"));
        fm.put("venueType", e.get("venueType"));
        fm.put("meetupUrl", e.get("meetupUrl"));
        fm.put("isUpcoming", e.get("isUpcoming"));
        fm.put("hasDescription", !description.isBlank());
        fm.put("talks", e.get("talks"));       // always present (maybe [])
        fm.put("sponsors", e.get("sponsors")); // always present (maybe [])
        writeMarkdown(dir.resolve(e.get("slug") + ".md"), fm, description);
    }

    static void writeSpeakerMarkdown(Path dir, Map<String, Object> s) throws Exception {
        String bio = str(s.get("bio"));
        Map<String, Object> fm = new LinkedHashMap<>();
        fm.put("layout", "speaker");
        fm.put("title", s.get("name"));
        fm.put("name", s.get("name"));
        fm.put("slug", s.get("slug"));
        fm.put("photo", s.get("photo"));
        fm.put("company", s.get("company"));
        fm.put("jobTitle", s.get("jobTitle"));
        fm.put("talkCount", s.get("talkCount"));
        fm.put("linkedinUrl", s.get("linkedinUrl"));
        fm.put("twitter", s.get("twitter"));
        fm.put("github", s.get("github"));
        fm.put("website", s.get("website"));
        fm.put("hasBio", !bio.isBlank());
        fm.put("talks", s.get("talks"));   // always present (maybe [])
        fm.put("career", s.get("career")); // always present (maybe [])
        writeMarkdown(dir.resolve(s.get("slug") + ".md"), fm, bio);
    }

    /** Write "---\n<yaml>---\n\n<body>\n". */
    static void writeMarkdown(Path file, Map<String, Object> frontMatter, String body) throws Exception {
        String yaml = YAML.writeValueAsString(frontMatter);
        String content = "---\n" + yaml + "---\n\n" + (body == null ? "" : body.strip()) + "\n";
        Files.writeString(file, content);
    }

    static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    // ---------------- Events ----------------

    static List<Map<String, Object>> exportEvents(Connection c, Path imgDir) throws Exception {
        List<Map<String, Object>> events = new ArrayList<>();
        String sql = "SELECT id, title, description, start_date_time, venue_id, meetup_event_url, prizes_enabled " +
                "FROM event WHERE status = 'PUBLISHED' ORDER BY start_date_time DESC";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String title = rs.getString("title");
                OffsetDateTime start = rs.getObject("start_date_time", OffsetDateTime.class);
                var startVienna = start == null ? null : start.atZoneSameInstant(VIENNA);

                Map<String, Object> e = new LinkedHashMap<>();
                e.put("id", id);
                e.put("slug", eventSlug(id, title));
                e.put("title", title);
                e.put("description", rs.getString("description"));
                e.put("startDateTime", start == null ? null : start.toString());
                e.put("dateDisplay", startVienna == null ? null : startVienna.format(DATE_DISPLAY));
                e.put("year", startVienna == null ? null : startVienna.getYear());
                e.put("venueType", rs.getString("venue_id"));
                e.put("meetupUrl", rs.getString("meetup_event_url"));
                e.put("prizesEnabled", rs.getBoolean("prizes_enabled"));
                e.put("isUpcoming", start != null && start.isAfter(OffsetDateTime.now()));
                var eventDate = startVienna == null ? null : startVienna.toLocalDate();
                e.put("talks", loadTalks(c, imgDir, id, eventDate));
                e.put("sponsors", loadEventSponsors(c, imgDir, start));
                events.add(e);
            }
        }
        return events;
    }

    /** A minimal event card for list/home pages: title, date, link, talk count. */
    static Map<String, Object> slimEvent(Map<String, Object> e) {
        int talkCount = ((List<?>) e.getOrDefault("talks", List.of())).size();
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("title", e.get("title"));
        s.put("dateDisplay", e.get("dateDisplay"));
        s.put("url", "/events/" + e.get("slug") + "/");
        s.put("talkCount", talkCount);
        s.put("hasTalks", talkCount > 0);
        return s;
    }

    /** events (date-descending) grouped by year, newest year first. */
    static List<Map<String, Object>> groupByYearDesc(List<Map<String, Object>> events) {
        Map<Object, List<Map<String, Object>>> byYear = new LinkedHashMap<>();
        for (Map<String, Object> e : events) {
            byYear.computeIfAbsent(e.get("year"), k -> new ArrayList<>()).add(slimEvent(e));
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        for (var entry : byYear.entrySet()) {
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("year", entry.getKey());
            g.put("events", entry.getValue());
            groups.add(g);
        }
        return groups;
    }

    /** Home page: the 3 most recent PAST events + the next upcoming event (or null). */
    static Map<String, Object> homeData(List<Map<String, Object>> events) {
        List<Map<String, Object>> recentPast = new ArrayList<>();
        Map<String, Object> nextUpcoming = null; // nearest future = last upcoming in desc order
        for (Map<String, Object> e : events) {
            boolean upcoming = Boolean.TRUE.equals(e.get("isUpcoming"));
            if (upcoming) {
                nextUpcoming = slimEvent(e);
            } else if (recentPast.size() < 3) {
                recentPast.add(slimEvent(e));
            }
        }
        Map<String, Object> home = new LinkedHashMap<>();
        home.put("recentPast", recentPast);
        home.put("nextUpcoming", nextUpcoming);
        return home;
    }

    /** Slim speaker cards for the speakers list page, sorted by talk count desc then name. */
    static List<Map<String, Object>> speakersList(List<Map<String, Object>> speakers) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> s : speakers) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s.get("name"));
            m.put("slug", s.get("slug"));
            m.put("url", "/speakers/" + s.get("slug") + "/");
            m.put("photo", s.get("photo"));
            m.put("company", s.get("company"));
            m.put("jobTitle", s.get("jobTitle"));
            m.put("talkCount", s.get("talkCount"));
            list.add(m);
        }
        list.sort((a, b) -> {
            int ca = ((Number) a.getOrDefault("talkCount", 0)).intValue();
            int cb = ((Number) b.getOrDefault("talkCount", 0)).intValue();
            if (cb != ca) return Integer.compare(cb, ca);
            return String.valueOf(a.get("name")).compareToIgnoreCase(String.valueOf(b.get("name")));
        });
        return list;
    }

    static List<Map<String, Object>> loadTalks(Connection c, Path imgDir, long eventId,
                                               java.time.LocalDate eventDate) throws Exception {
        List<Map<String, Object>> talks = new ArrayList<>();
        String sql = "SELECT id, title, description, duration_minutes, talk_language, slides_language " +
                "FROM talk WHERE event_id = ? ORDER BY COALESCE(order_index, 0), id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long talkId = rs.getLong("id");
                    Map<String, Object> t = new LinkedHashMap<>();
                    t.put("title", rs.getString("title"));
                    t.put("description", rs.getString("description"));
                    t.put("durationMinutes", (Integer) rs.getObject("duration_minutes"));
                    t.put("talkLanguage", rs.getString("talk_language"));
                    t.put("slidesLanguage", rs.getString("slides_language"));
                    t.put("speakers", loadTalkSpeakers(c, imgDir, talkId, eventDate));
                    t.put("resources", loadTalkResources(c, talkId));
                    talks.add(t);
                }
            }
        }
        return talks;
    }

    // Per-talk speaker block, mirroring the Vaadin SpeakerInfo on the event detail:
    // bio falls back to the speaker's default when no per-talk override is set; company/
    // job title are resolved as they were at the event date; social profiles + photo come
    // from the speaker record.
    static List<Map<String, Object>> loadTalkSpeakers(Connection c, Path imgDir, long talkId,
                                                      java.time.LocalDate eventDate) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.bio, s.profile_image_id, s.profile_pic_url, " +
                "s.linkedin_url, s.twitter_handle, s.github_handle, s.website_url, tsb.bio AS talk_bio " +
                "FROM talk_speaker ts JOIN speaker s ON s.id = ts.speaker_id " +
                "LEFT JOIN talk_speaker_bio tsb ON tsb.talk_id = ts.talk_id AND tsb.speaker_id = ts.speaker_id " +
                "WHERE ts.talk_id = ? ORDER BY COALESCE(ts.speaker_order, 0), s.name";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, talkId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long sid = rs.getLong("id");
                    String name = rs.getString("name");
                    String talkBio = rs.getString("talk_bio");
                    String bio = talkBio != null && !talkBio.isBlank() ? talkBio : rs.getString("bio");
                    Map<String, Object> company = companyAtDate(c, sid, eventDate);

                    Map<String, Object> sp = new LinkedHashMap<>();
                    sp.put("name", name);
                    sp.put("slug", speakerSlug(sid, name));
                    sp.put("bio", bio);
                    sp.put("hasBio", bio != null && !bio.isBlank());
                    sp.put("photo", imageFor(c, imgDir, null,
                            (Long) rs.getObject("profile_image_id"), rs.getString("profile_pic_url")));
                    sp.put("company", company == null ? null : company.get("company"));
                    sp.put("jobTitle", company == null ? null : company.get("jobTitle"));
                    sp.put("linkedinUrl", rs.getString("linkedin_url"));
                    sp.put("twitter", rs.getString("twitter_handle"));
                    sp.put("github", rs.getString("github_handle"));
                    sp.put("website", rs.getString("website_url"));
                    list.add(sp);
                }
            }
        }
        return list;
    }

    // Company/job title the speaker held on a given date (mirrors the raffle app's
    // findCompanyBySpeakerIdAndDate). Null if the date is unknown or no match.
    static Map<String, Object> companyAtDate(Connection c, long speakerId,
                                             java.time.LocalDate date) throws Exception {
        if (date == null) return null;
        String sql = "SELECT co.name, sc.job_title FROM speaker_company sc JOIN company co ON co.id = sc.company_id " +
                "WHERE sc.speaker_id = ? AND (sc.start_date IS NULL OR sc.start_date <= ?::date) " +
                "AND (sc.end_date IS NULL OR sc.end_date >= ?::date) " +
                "ORDER BY sc.start_date DESC NULLS LAST LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, speakerId);
            ps.setString(2, date.toString());
            ps.setString(3, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("company", rs.getString("name"));
                    m.put("jobTitle", rs.getString("job_title"));
                    return m;
                }
            }
        }
        return null;
    }

    static List<Map<String, Object>> loadTalkResources(Connection c, long talkId) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT resource_type, url, title FROM talk_resource WHERE talk_id = ? " +
                "ORDER BY order_index, id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, talkId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("type", rs.getString("resource_type"));
                    r.put("url", rs.getString("url"));
                    r.put("title", rs.getString("title"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    // Sponsors active at the event date (or active now if event has no date)
    static List<Map<String, Object>> loadEventSponsors(Connection c, Path imgDir, OffsetDateTime eventStart) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.website_url, c.logo_image_id, c.logo_image_uuid, c.logo_url, " +
                "sl.name AS level, sl.display_order " +
                "FROM sponsorship sp JOIN company c ON c.id = sp.company_id " +
                "LEFT JOIN sponsorship_level sl ON sl.id = sp.sponsorship_level_id " +
                "WHERE sp.from_date <= ?::date AND (sp.to_date IS NULL OR sp.to_date >= ?::date) " +
                "ORDER BY COALESCE(sl.display_order, 999), c.name";
        String refDate = (eventStart == null
                ? java.time.LocalDate.now(VIENNA)
                : eventStart.atZoneSameInstant(VIENNA).toLocalDate()).toString();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, refDate);
            ps.setString(2, refDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("companyName", rs.getString("name"));
                    s.put("logo", imageFor(c, imgDir, rs.getString("logo_image_uuid"),
                            (Long) rs.getObject("logo_image_id"), rs.getString("logo_url")));
                    s.put("level", rs.getString("level"));
                    s.put("websiteUrl", rs.getString("website_url"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    // ---------------- Speakers ----------------

    static List<Map<String, Object>> exportSpeakers(Connection c, Path imgDir,
                                                     List<Map<String, Object>> events) throws Exception {
        List<Map<String, Object>> speakers = new ArrayList<>();
        String sql = "SELECT id, name, bio, profile_image_id, profile_pic_url, " +
                "linkedin_url, twitter_handle, github_handle, website_url " +
                "FROM speaker WHERE active = true ORDER BY name";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");

                Map<String, Object> current = currentCompany(c, id);
                List<Map<String, Object>> talks = speakerTalks(c, id);

                Map<String, Object> s = new LinkedHashMap<>();
                s.put("id", id);
                s.put("slug", speakerSlug(id, name));
                s.put("name", name);
                s.put("bio", rs.getString("bio"));
                s.put("photo", imageFor(c, imgDir, null,
                        (Long) rs.getObject("profile_image_id"), rs.getString("profile_pic_url")));
                s.put("company", current == null ? null : current.get("company"));
                s.put("jobTitle", current == null ? null : current.get("jobTitle"));
                s.put("talkCount", talks.size());
                s.put("linkedinUrl", rs.getString("linkedin_url"));
                s.put("twitter", rs.getString("twitter_handle"));
                s.put("github", rs.getString("github_handle"));
                s.put("website", rs.getString("website_url"));
                s.put("talks", talks);
                s.put("career", speakerCareer(c, id));
                speakers.add(s);
            }
        }
        return speakers;
    }

    static Map<String, Object> currentCompany(Connection c, long speakerId) throws Exception {
        String sql = "SELECT co.name, sc.job_title FROM speaker_company sc JOIN company co ON co.id = sc.company_id " +
                "WHERE sc.speaker_id = ? ORDER BY sc.is_current DESC NULLS LAST, " +
                "sc.start_date DESC NULLS LAST, sc.id DESC LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, speakerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("company", rs.getString("name"));
                    m.put("jobTitle", rs.getString("job_title"));
                    return m;
                }
            }
        }
        return null;
    }

    static List<Map<String, Object>> speakerCareer(Connection c, long speakerId) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT co.name, sc.job_title, sc.start_date, sc.end_date, sc.is_current " +
                "FROM speaker_company sc JOIN company co ON co.id = sc.company_id " +
                "WHERE sc.speaker_id = ? ORDER BY sc.is_current DESC NULLS LAST, sc.start_date DESC NULLS LAST";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, speakerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String startD = rs.getString("start_date");
                    String endD = rs.getString("end_date");
                    boolean current = rs.getBoolean("is_current");
                    String period;
                    if (startD == null && endD == null) {
                        period = current ? "Current" : "";
                    } else {
                        String from = startD == null ? "?" : startD.substring(0, 4);
                        String to = current ? "present" : (endD == null ? "?" : endD.substring(0, 4));
                        period = from + " – " + to;
                    }
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("company", rs.getString("name"));
                    m.put("jobTitle", rs.getString("job_title"));
                    m.put("period", period);
                    list.add(m);
                }
            }
        }
        return list;
    }

    // talks this speaker gave at PUBLISHED events
    static List<Map<String, Object>> speakerTalks(Connection c, long speakerId) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT t.title, t.duration_minutes, e.id AS event_id, e.title AS event_title, " +
                "e.start_date_time FROM talk_speaker ts " +
                "JOIN talk t ON t.id = ts.talk_id " +
                "JOIN event e ON e.id = t.event_id " +
                "WHERE ts.speaker_id = ? AND e.status = 'PUBLISHED' " +
                "ORDER BY e.start_date_time DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, speakerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long eid = rs.getLong("event_id");
                    String etitle = rs.getString("event_title");
                    OffsetDateTime start = rs.getObject("start_date_time", OffsetDateTime.class);
                    var sv = start == null ? null : start.atZoneSameInstant(VIENNA);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("title", rs.getString("title"));
                    m.put("eventTitle", etitle);
                    m.put("eventSlug", eventSlug(eid, etitle));
                    m.put("dateDisplay", sv == null ? null : sv.format(DATE_DISPLAY));
                    m.put("durationMinutes", (Integer) rs.getObject("duration_minutes"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    // ---------------- Sponsors (active now) ----------------

    static List<Map<String, Object>> exportSponsors(Connection c, Path imgDir) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.website_url, c.logo_image_id, c.logo_image_uuid, c.logo_url, " +
                "sl.name AS level, sl.display_order " +
                "FROM sponsorship sp JOIN company c ON c.id = sp.company_id " +
                "LEFT JOIN sponsorship_level sl ON sl.id = sp.sponsorship_level_id " +
                "WHERE (sp.from_date IS NULL OR sp.from_date <= CURRENT_DATE) " +
                "AND (sp.to_date IS NULL OR sp.to_date >= CURRENT_DATE) " +
                "ORDER BY COALESCE(sl.display_order, 999), c.name";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("companyName", rs.getString("name"));
                s.put("slug", companySlug(rs.getLong("id"), rs.getString("name")));
                s.put("logo", imageFor(c, imgDir, rs.getString("logo_image_uuid"),
                        (Long) rs.getObject("logo_image_id"), rs.getString("logo_url")));
                s.put("level", rs.getString("level"));
                s.put("websiteUrl", rs.getString("website_url"));
                list.add(s);
            }
        }
        return list;
    }

    // ---------------- Companies ----------------

    static List<Map<String, Object>> exportCompanies(Connection c, Path imgDir) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, name, description, website_url, logo_image_id, logo_image_uuid, logo_url " +
                "FROM company ORDER BY name";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                List<Map<String, Object>> compSpeakers = companySpeakers(c, id);
                int totalTalks = compSpeakers.stream()
                        .mapToInt(m -> ((Number) m.getOrDefault("talkCount", 0)).intValue()).sum();

                Map<String, Object> co = new LinkedHashMap<>();
                co.put("id", id);
                co.put("slug", companySlug(id, rs.getString("name")));
                co.put("name", rs.getString("name"));
                co.put("description", rs.getString("description"));
                co.put("logo", imageFor(c, imgDir, rs.getString("logo_image_uuid"),
                        (Long) rs.getObject("logo_image_id"), rs.getString("logo_url")));
                co.put("websiteUrl", rs.getString("website_url"));
                co.put("totalTalks", totalTalks);
                co.put("isCurrentSponsor", sponsorStatus(c, id, true));
                co.put("wasSponsor", sponsorStatus(c, id, false));
                co.put("speakers", compSpeakers);
                co.put("_lastTalkDate", companyLastTalkDate(c, id));
                list.add(co);
            }
        }
        list.sort(ExportContent::compareCompanies);
        list.forEach(co -> co.remove("_lastTalkDate"));
        return list;
    }

    // Mirrors jug-vienna-raffle DisplayCompanyCardsWithSpeakersUseCaseImpl.compareCompanies:
    // current sponsors -> total talks desc -> past sponsors -> last talk date desc -> name.
    static int compareCompanies(Map<String, Object> c1, Map<String, Object> c2) {
        boolean cur1 = Boolean.TRUE.equals(c1.get("isCurrentSponsor"));
        boolean cur2 = Boolean.TRUE.equals(c2.get("isCurrentSponsor"));
        if (cur1 != cur2) return cur1 ? -1 : 1;

        int talks1 = ((Number) c1.getOrDefault("totalTalks", 0)).intValue();
        int talks2 = ((Number) c2.getOrDefault("totalTalks", 0)).intValue();
        if (talks1 != talks2) return Integer.compare(talks2, talks1);

        boolean was1 = Boolean.TRUE.equals(c1.get("wasSponsor"));
        boolean was2 = Boolean.TRUE.equals(c2.get("wasSponsor"));
        if (was1 != was2) return was1 ? -1 : 1;

        OffsetDateTime d1 = (OffsetDateTime) c1.get("_lastTalkDate");
        OffsetDateTime d2 = (OffsetDateTime) c2.get("_lastTalkDate");
        if (d1 != null && d2 != null) {
            int byDate = d2.compareTo(d1);
            if (byDate != 0) return byDate;
        } else if (d1 != null) {
            return -1;
        } else if (d2 != null) {
            return 1;
        }

        return String.valueOf(c1.get("name")).compareToIgnoreCase(String.valueOf(c2.get("name")));
    }

    // Most recent talk date where the speaker was at the company during the talk's event.
    static OffsetDateTime companyLastTalkDate(Connection c, long companyId) throws Exception {
        String sql = "SELECT MAX(CASE " +
                "WHEN e.start_date_time::date >= sc.start_date " +
                "AND (sc.end_date IS NULL OR e.start_date_time::date <= sc.end_date) " +
                "THEN e.start_date_time END) AS last_talk_date " +
                "FROM company co " +
                "LEFT JOIN speaker_company sc ON co.id = sc.company_id " +
                "LEFT JOIN speaker s ON sc.speaker_id = s.id " +
                "LEFT JOIN talk_speaker ts ON s.id = ts.speaker_id " +
                "LEFT JOIN talk t ON ts.talk_id = t.id " +
                "LEFT JOIN event e ON t.event_id = e.id " +
                "WHERE co.id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject("last_talk_date", OffsetDateTime.class) : null;
            }
        }
    }

    // One markdown page per company (detail view). Mirrors the Vaadin CompanyDetailDialog:
    // current speakers (employment end_date IS NULL) + past speakers (ended, with a talk during
    // employment). Only active speakers are included so every speaker link resolves to a page.
    static void writeCompanyMarkdown(Path dir, Connection c, Map<String, Object> co) throws Exception {
        String description = str(co.get("description"));
        List<Map<String, Object>> current = new ArrayList<>();
        List<Map<String, Object>> past = new ArrayList<>();
        for (Map<String, Object> s : companyDetailSpeakers(c, (Long) co.get("id"))) {
            if (Boolean.TRUE.equals(s.get("_ended"))) {
                if (Boolean.TRUE.equals(s.get("_talkDuringEmployment"))) past.add(s);
            } else {
                current.add(s);
            }
        }
        current.forEach(ExportContent::stripSpeakerInternals);
        past.forEach(ExportContent::stripSpeakerInternals);

        Map<String, Object> fm = new LinkedHashMap<>();
        fm.put("layout", "company");
        fm.put("title", co.get("name"));
        fm.put("name", co.get("name"));
        fm.put("slug", co.get("slug"));
        fm.put("logo", co.get("logo"));
        fm.put("hasLogo", co.get("logo") != null);
        fm.put("websiteUrl", co.get("websiteUrl"));
        fm.put("isCurrentSponsor", co.get("isCurrentSponsor"));
        fm.put("wasSponsor", co.get("wasSponsor"));
        fm.put("totalTalks", co.get("totalTalks"));
        fm.put("hasDescription", !description.isBlank());
        fm.put("currentSpeakers", current);
        fm.put("pastSpeakers", past);
        writeMarkdown(dir.resolve(co.get("slug") + ".md"), fm, description);
    }

    static void stripSpeakerInternals(Map<String, Object> s) {
        s.remove("_ended");
        s.remove("_talkDuringEmployment");
    }

    static List<Map<String, Object>> companyDetailSpeakers(Connection c, long companyId) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT DISTINCT s.id, s.name, s.bio, s.linkedin_url, s.github_handle, " +
                "sc.job_title, sc.start_date, sc.end_date, sc.is_current " +
                "FROM speaker_company sc JOIN speaker s ON s.id = sc.speaker_id " +
                "WHERE sc.company_id = ? AND s.active = true ORDER BY s.name";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long sid = rs.getLong("id");
                    String name = rs.getString("name");
                    java.sql.Date start = rs.getDate("start_date");
                    java.sql.Date end = rs.getDate("end_date");
                    boolean isCurrent = rs.getBoolean("is_current");
                    String bio = rs.getString("bio");
                    String github = rs.getString("github_handle");

                    Map<String, Object> s = new LinkedHashMap<>();
                    String sslug = speakerSlug(sid, name);
                    s.put("speakerId", sid);
                    s.put("slug", sslug);
                    s.put("url", "/speakers/" + sslug + "/");
                    s.put("name", name);
                    s.put("jobTitle", rs.getString("job_title"));
                    s.put("dateRange", employmentRange(start, end, isCurrent));
                    s.put("bio", bio);
                    s.put("hasBio", bio != null && !bio.isBlank());
                    s.put("linkedinUrl", rs.getString("linkedin_url"));
                    s.put("githubUrl", github == null || github.isBlank() ? null : "https://github.com/" + github);
                    s.put("talkCount", speakerTalkCount(c, sid));
                    s.put("_ended", end != null);
                    s.put("_talkDuringEmployment", end == null || hasTalkDuringEmployment(c, sid, start, end));
                    list.add(s);
                }
            }
        }
        return list;
    }

    // "MMM yyyy" start; " - MMM yyyy" end, or " - Present" for a current open-ended employment.
    static String employmentRange(java.sql.Date start, java.sql.Date end, boolean current) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        String s = start == null ? "" : start.toLocalDate().format(f);
        if (end != null) {
            String e = end.toLocalDate().format(f);
            return s.isEmpty() ? e : s + " - " + e;
        }
        if (current) return s.isEmpty() ? "Present" : s + " - Present";
        return s;
    }

    static boolean hasTalkDuringEmployment(Connection c, long speakerId,
            java.sql.Date start, java.sql.Date end) throws Exception {
        String sql = "SELECT 1 FROM talk_speaker ts JOIN talk t ON t.id = ts.talk_id " +
                "JOIN event e ON e.id = t.event_id " +
                "WHERE ts.speaker_id = ? AND e.status = 'PUBLISHED' " +
                "AND (?::date IS NULL OR e.start_date_time::date >= ?::date) " +
                "AND (?::date IS NULL OR e.start_date_time::date <= ?::date) LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, speakerId);
            ps.setDate(2, start);
            ps.setDate(3, start);
            ps.setDate(4, end);
            ps.setDate(5, end);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static List<Map<String, Object>> companySpeakers(Connection c, long companyId) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT DISTINCT s.id, s.name, sc.job_title FROM speaker_company sc " +
                "JOIN speaker s ON s.id = sc.speaker_id " +
                "WHERE sc.company_id = ? AND sc.is_current = true AND s.active = true ORDER BY s.name";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long sid = rs.getLong("id");
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", rs.getString("name"));
                    m.put("jobTitle", rs.getString("job_title"));
                    m.put("talkCount", speakerTalkCount(c, sid));
                    list.add(m);
                }
            }
        }
        return list;
    }

    static int speakerTalkCount(Connection c, long speakerId) throws Exception {
        String sql = "SELECT COUNT(*) FROM talk_speaker ts JOIN talk t ON t.id = ts.talk_id " +
                "JOIN event e ON e.id = t.event_id WHERE ts.speaker_id = ? AND e.status = 'PUBLISHED'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, speakerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    static boolean sponsorStatus(Connection c, long companyId, boolean currentOnly) throws Exception {
        String cond = currentOnly
                ? "(sp.from_date IS NULL OR sp.from_date <= CURRENT_DATE) AND (sp.to_date IS NULL OR sp.to_date >= CURRENT_DATE)"
                : "sp.to_date IS NOT NULL AND sp.to_date < CURRENT_DATE";
        String sql = "SELECT 1 FROM sponsorship sp WHERE sp.company_id = ? AND " + cond + " LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ---------------- Images ----------------

    static String imageFor(Connection c, Path imgDir, String imageUuid, Long imageId, String externalUrl) throws Exception {
        String uuid = imageUuid;
        String contentType = null;
        if (uuid == null && imageId != null) {
            try (PreparedStatement ps = c.prepareStatement("SELECT uuid, content_type FROM image WHERE id = ?")) {
                ps.setLong(1, imageId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        uuid = rs.getString("uuid");
                        contentType = rs.getString("content_type");
                    }
                }
            }
        }
        if (uuid != null) {
            String cached = imageCache.get(uuid);
            if (cached != null) return cached;
            if (contentType == null) {
                try (PreparedStatement ps = c.prepareStatement("SELECT content_type FROM image WHERE uuid = ?::uuid")) {
                    ps.setString(1, uuid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) contentType = rs.getString("content_type");
                    }
                }
            }
            String fileName = uuid + extFor(contentType);
            Path target = imgDir.resolve(fileName);
            if (Files.exists(target) || download(uuid, target)) {
                String webPath = "/img/" + fileName;
                imageCache.put(uuid, webPath);
                return webPath;
            }
        }
        return externalUrl == null || externalUrl.isBlank() ? null : externalUrl;
    }

    static boolean download(String uuid, Path target) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(IMAGE_BASE + uuid))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET().build();
            HttpResponse<InputStream> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() != 200) {
                System.err.println("Image " + uuid + " -> HTTP " + resp.statusCode());
                return false;
            }
            try (InputStream in = resp.body()) {
                Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception ex) {
            System.err.println("Image " + uuid + " download failed: " + ex.getMessage());
            return false;
        }
    }

    static String extFor(String contentType) {
        if (contentType == null) return ".bin";
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> ".bin";
        };
    }

    // ---------------- Helpers ----------------

    // Human-readable slug WITHOUT the id suffix. Ascii, lowercase, max 60 chars.
    static String baseSlug(String text) {
        String base = Optional.ofNullable(text).orElse("item");
        String s = java.text.Normalizer.normalize(base, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (s.length() > 60) s = s.substring(0, 60).replaceAll("-$", "");
        if (s.isEmpty()) s = "item";
        return s;
    }

    // Slug with the id appended — the collision-proof fallback form.
    static String slug(String text, long id) {
        return baseSlug(text) + "-" + id;
    }

    // Event slugs are prefixed with the Vienna event date (YYYY-MM-DD-<title>), matching the
    // org README convention. The date makes slugs unique in practice; the collision resolver
    // still appends the id in the rare case two events share a date AND a title slug.
    static Map<Long, String> resolveEventSlugs(Connection c) throws Exception {
        LinkedHashMap<Long, String> idToName = new LinkedHashMap<>();
        String sql = "SELECT id, title, start_date_time FROM event WHERE status = 'PUBLISHED'";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OffsetDateTime start = rs.getObject("start_date_time", OffsetDateTime.class);
                String date = start == null ? null : start.atZoneSameInstant(VIENNA).toLocalDate().toString();
                String title = rs.getString("title");
                idToName.put(rs.getLong("id"), date == null ? title : date + " " + title);
            }
        }
        return resolveSlugCollisions(idToName);
    }

    // Load (id, name) for an entity type and resolve its slugs with collision-only suffixes.
    static Map<Long, String> resolveSlugs(Connection c, String sql, String nameCol) throws Exception {
        LinkedHashMap<Long, String> idToName = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) idToName.put(rs.getLong("id"), rs.getString(nameCol));
        }
        return resolveSlugCollisions(idToName);
    }

    // Collision-only suffix: a base slug that is unique across the population stays clean;
    // only when two ids share a base slug does each keep the "-<id>" suffix to disambiguate.
    static Map<Long, String> resolveSlugCollisions(LinkedHashMap<Long, String> idToName) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String name : idToName.values()) counts.merge(baseSlug(name), 1, Integer::sum);
        Map<Long, String> out = new LinkedHashMap<>();
        for (var e : idToName.entrySet()) {
            String base = baseSlug(e.getValue());
            out.put(e.getKey(), counts.get(base) == 1 ? base : base + "-" + e.getKey());
        }
        return out;
    }

    static void writeJson(Path file, Object data) throws Exception {
        JSON.writeValue(file.toFile(), data);
        System.out.println("Wrote " + file.getFileName() + " (" + Files.size(file) + " bytes)");
    }

    static String env(String key, String def) {
        String v = System.getenv(key);
        return v == null || v.isBlank() ? def : v;
    }
}
