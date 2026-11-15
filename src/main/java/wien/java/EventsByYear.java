package wien.java;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

import java.util.List;

/** Maps data/eventsByYear.json (events grouped by year, newest year first).
 *  Access in templates as {@code cdi:eventsByYear.list}. */
@DataMapping(value = "eventsByYear", type = DataMapping.Type.ARRAY_FILE)
public record EventsByYear(List<YearGroup> list) {

    public record YearGroup(int year, List<EventCard> events) {
    }

    public record EventCard(String title, String dateDisplay, String url,
                            int talkCount, boolean hasTalks) {
    }
}
