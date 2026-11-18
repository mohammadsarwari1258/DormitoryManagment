package nematullah.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Writes one line into the activity_log table (shown as "Recent Activities" on the Dashboard). */
public final class Activity {

    private Activity() {
    }

    public static void log(Connection con, String category, String message) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO activity_log (Category, Message) VALUES (?, ?)")) {
            ps.setString(1, category);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }
}
