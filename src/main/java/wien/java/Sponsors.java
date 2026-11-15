package wien.java;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

import java.util.List;

/** Maps the root array in data/sponsors.json. Access in templates as {@code cdi:sponsors}. */
@DataMapping(value = "sponsors", type = DataMapping.Type.ARRAY_FILE)
public record Sponsors(List<Sponsor> list) {

    public record Sponsor(String companyName, String slug, String logo, String level, String websiteUrl) {
        public boolean hasLocalLogo() {
            return logo != null && logo.startsWith("/");
        }
    }
}
