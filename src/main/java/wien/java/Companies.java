package wien.java;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

import java.util.List;

/** Maps the root array in data/companies.json. Access in templates as {@code cdi:companies}. */
@DataMapping(value = "companies", type = DataMapping.Type.ARRAY_FILE)
public record Companies(List<Company> list) {

    public record Company(
            long id,
            String slug,
            String name,
            String description,
            String logo,
            String websiteUrl,
            int totalTalks,
            boolean isCurrentSponsor,
            boolean wasSponsor,
            List<CompanySpeaker> speakers) {

        public boolean hasLocalLogo() {
            return logo != null && logo.startsWith("/");
        }
    }

    public record CompanySpeaker(String name, String jobTitle, int talkCount) {
    }
}
