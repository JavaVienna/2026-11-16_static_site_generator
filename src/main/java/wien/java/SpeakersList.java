package wien.java;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

import java.util.List;

/** Maps data/speakersList.json (slim speaker cards for the speakers list page,
 *  sorted by talk count). Access in templates as {@code cdi:speakersList.list}.
 *  Speaker DETAIL pages come from the markdown files in content/speakers/. */
@DataMapping(value = "speakersList", type = DataMapping.Type.ARRAY_FILE)
public record SpeakersList(List<SpeakerCard> list) {

    public record SpeakerCard(String name, String slug, String url, String photo,
                              String company, String jobTitle, int talkCount) {
    }
}
