///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21
//SOURCES ExportContent.java
//DEPS org.postgresql:postgresql:42.7.4
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.1
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.1
//DEPS org.junit.jupiter:junit-jupiter:5.10.2
//DEPS org.junit.platform:junit-platform-launcher:1.10.2

// Pure-function tests for the exporter. Run with:  jbang ExportContentTest.java  (or ./03_test.sh)
// These cover the DB-independent logic only (ordering, slugs, date ranges). The SQL
// (dedup via is_current, sponsor status, during-employment filter) needs the raffle DB
// and is verified by running ./01_export.sh + a clean Roq generate.

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class ExportContentTest {

    public static void main(String[] args) {
        LauncherDiscoveryRequest req = request().selectors(selectClass(ExportContentTest.class)).build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(req);
        var summary = listener.getSummary();
        var out = new java.io.PrintWriter(System.out, true);
        summary.printTo(out);
        summary.printFailuresTo(out);
        System.out.printf("Tests: %d run, %d failed%n",
                summary.getTestsStartedCount(), summary.getTotalFailureCount());
        if (summary.getTotalFailureCount() > 0) System.exit(1);
    }

    static Map<String, Object> company(String name, boolean current, boolean past, int talks, String lastTalk) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("isCurrentSponsor", current);
        m.put("wasSponsor", past);
        m.put("totalTalks", talks);
        m.put("_lastTalkDate", lastTalk == null ? null : java.time.OffsetDateTime.parse(lastTalk));
        return m;
    }

    static List<String> order(List<Map<String, Object>> in) {
        return in.stream().sorted(ExportContent::compareCompanies)
                .map(m -> (String) m.get("name")).collect(Collectors.toList());
    }

    @Test
    void currentSponsorsComeFirst() {
        List<String> got = order(List.of(
                company("Zeta", false, false, 99, null),       // many talks, no sponsorship
                company("Alpha", true, false, 1, null)));      // current sponsor, few talks
        assertEquals(List.of("Alpha", "Zeta"), got);
    }

    @Test
    void amongNonSponsorsHigherTalkCountWins() {
        List<String> got = order(List.of(
                company("Few", false, false, 3, null),
                company("Many", false, false, 30, null)));
        assertEquals(List.of("Many", "Few"), got);
    }

    @Test
    void pastSponsorBeatsNonSponsorOnTalkTie() {
        List<String> got = order(List.of(
                company("Plain", false, false, 5, null),
                company("WasSponsor", false, true, 5, null)));
        assertEquals(List.of("WasSponsor", "Plain"), got);
    }

    @Test
    void newerLastTalkWinsThenName() {
        List<String> got = order(List.of(
                company("Older", false, false, 5, "2020-01-01T18:00:00Z"),
                company("Newer", false, false, 5, "2026-01-01T18:00:00Z"),
                company("NoTalks", false, false, 5, null)));
        // Newer date first, then older, then the one with no dated talk last.
        assertEquals(List.of("Newer", "Older", "NoTalks"), got);
    }

    @Test
    void fullOrderingMatchesVaadin() {
        List<String> got = order(List.of(
                company("Posedio", true, false, 9, null),
                company("Dynatrace", true, false, 21, null),
                company("RedHat", false, false, 20, null),
                company("Greyflower", false, true, 14, null)));
        // current sponsors first (by talks), then non-sponsors by talks.
        assertEquals(List.of("Dynatrace", "Posedio", "RedHat", "Greyflower"), got);
    }

    @Test
    void slugIsAsciiLowercaseWithId() {
        assertEquals("doubleslash-net-business-gmbh-41",
                ExportContent.slug("doubleSlash.net Business GmbH", 41));
        assertEquals("research-industrial-systems-engineering-rise-7",
                ExportContent.slug("Research Industrial Systems Engineering (RISE)", 7));
    }

    @Test
    void uniqueBaseSlugHasNoIdSuffix() {
        LinkedHashMap<Long, String> in = new LinkedHashMap<>();
        in.put(7L, "Jan Kotek");
        in.put(42L, "Thomas Schneider");
        Map<Long, String> got = ExportContent.resolveSlugCollisions(in);
        assertEquals("jan-kotek", got.get(7L));
        assertEquals("thomas-schneider", got.get(42L));
    }

    @Test
    void collidingBaseSlugsKeepIdSuffix() {
        LinkedHashMap<Long, String> in = new LinkedHashMap<>();
        in.put(5L, "Thomas Schneider");
        in.put(96L, "Thomas Schneider"); // same name -> both disambiguated by id
        in.put(7L, "Jan Kotek");          // unique -> stays clean
        Map<Long, String> got = ExportContent.resolveSlugCollisions(in);
        assertEquals("thomas-schneider-5", got.get(5L));
        assertEquals("thomas-schneider-96", got.get(96L));
        assertEquals("jan-kotek", got.get(7L));
    }

    @Test
    void employmentRangeFormatsCurrentAndClosed() {
        Date jan2020 = Date.valueOf(LocalDate.of(2020, 1, 15));
        Date jun2022 = Date.valueOf(LocalDate.of(2022, 6, 1));
        assertEquals("Jan 2020 - Present", ExportContent.employmentRange(jan2020, null, true));
        assertEquals("Jan 2020 - Jun 2022", ExportContent.employmentRange(jan2020, jun2022, false));
        assertEquals("Jan 2020", ExportContent.employmentRange(jan2020, null, false));
        assertEquals("", ExportContent.employmentRange(null, null, false));
    }
}
