package wien.java;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

import java.util.List;

/** Maps data/home.json (3 most recent past events + next upcoming event, may be null).
 *  Access in templates as {@code cdi:home.recentPast} / {@code cdi:home.nextUpcoming}. */
@DataMapping(value = "home", type = DataMapping.Type.OBJECT_FILE)
public record Home(List<EventsByYear.EventCard> recentPast,
                   EventsByYear.EventCard nextUpcoming) {
}
