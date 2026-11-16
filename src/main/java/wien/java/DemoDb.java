package wien.java;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Talk demo for "Generating HTML from Markdown and from your Database".
 *
 * Reads the committed SQLite file {@code demo/demo.db} LIVE at build time and exposes it to
 * Qute via the {@code cdi:} namespace (same pattern as {@link CompanyView}). The sponsors
 * right-hand column renders from this query: {@code {#for sp in cdi:demoDb.sponsors} ...}.
 *
 * This is the "...from your Database" half of the talk — real SQL at generate time, but with
 * no external DB, no network and no Docker: the database is one file in the repo. Plain JDBC
 * via the xerial driver; no Quarkus datasource/Agroal needed. The path is repo-relative
 * because {@code roq generate} and {@code quarkus:dev} both run from the project root.
 */
@Named("demoDb")
@ApplicationScoped
@Unremovable
public class DemoDb {

    private static final String URL = "jdbc:sqlite:demo/demo.db";

    public record Sponsor(String slug, String companyName, String level, String logo, String websiteUrl) {
    }

    public List<Sponsor> getSponsors() {
        List<Sponsor> list = new ArrayList<>();
        String sql = "SELECT slug, name, level, logo, website_url FROM sponsor ORDER BY display_order";
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Sponsor(
                        rs.getString("slug"),
                        rs.getString("name"),
                        rs.getString("level"),
                        rs.getString("logo"),
                        rs.getString("website_url")));
            }
        } catch (SQLException e) {
            // A broken demo DB must fail the build loudly, not render an empty column.
            throw new IllegalStateException("Demo SQLite read failed (" + URL + ")", e);
        }
        return list;
    }
}
