package wien.java;

import io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage;
import io.quarkiverse.roq.frontmatter.runtime.model.RoqCollection;
import io.quarkiverse.roq.frontmatter.runtime.model.Site;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * View logic for the companies list page, as a CDI bean (accessed in Qute via the
 * {@code cdi:} namespace). The sort (A→Z) and the Hall-of-Fame filter (sponsors only) live
 * here in Java, not in the template:
 *   {#for co in cdi:companyView.sortedCompanies} ...
 *   {#for co in cdi:companyView.hallOfFameCompanies} ...
 *
 * The companies collection is read from the injected {@link Site} (Roq registers it as a
 * synthetic bean; the class itself is {@code @Vetoed}). We inject {@code Instance<Site>} and
 * resolve per call so dev-mode live-reload always sees the current site, not a stale one.
 */
@Named("companyView")
@ApplicationScoped
@Unremovable
public class CompanyView {

    @Inject
    Instance<Site> site;

    /** All companies, A→Z by name. */
    public List<DocumentPage> getSortedCompanies() {
        return byTitle(companies());
    }

    /** Hall of Fame = sponsors only (current or past), A→Z by name. */
    public List<DocumentPage> getHallOfFameCompanies() {
        return byTitle(companies()).stream()
                .filter(page -> truthy(page.data("isCurrentSponsor")) || truthy(page.data("wasSponsor")))
                .toList();
    }

    private RoqCollection companies() {
        return site.get().collections().get("companies");
    }

    private static List<DocumentPage> byTitle(RoqCollection companies) {
        List<DocumentPage> sorted = new ArrayList<>(companies);
        sorted.sort(Comparator.comparing(
                page -> page.title() == null ? "" : page.title(),
                String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    private static boolean truthy(Object value) {
        return Boolean.TRUE.equals(value) || "true".equals(String.valueOf(value));
    }
}
