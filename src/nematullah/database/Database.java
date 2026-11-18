package nematullah.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nematullah.service.AppException;
import nematullah.service.Check;
import nematullah.service.DataEvents;
import nematullah.service.PasswordUtil;
import nematullah.service.RoomService;

/**
 * Central SQLite access: connection, schema creation, old-data migration and transactions.
 * The database file path is relative ("database/abdolhashem.db"), so the whole folder can be moved.
 */
public final class Database {

    /** Can be changed for tests with -Ddorm.db=some/other.db */
    private static final String DB_FILE = System.getProperty("dorm.db", "database" + File.separator + "abdolhashem.db");
    private static final int SCHEMA_VERSION = 2;

    private Database() {
    }

    /** A piece of work that runs inside one transaction. */
    public interface Work<T> {
        T run(Connection con) throws Exception;
    }

    // ------------------------------------------------------------------ connections

    public static Connection getConnection() throws SQLException {
        return open(true);
    }

    private static Connection open(boolean foreignKeys) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found. Make sure the lib folder contains sqlite-jdbc.", e);
        }
        File folder = new File(DB_FILE).getAbsoluteFile().getParentFile();
        if (!folder.exists() && !folder.mkdirs()) {
            throw new SQLException("Cannot create database folder: " + folder.getAbsolutePath());
        }
        Connection con = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = " + (foreignKeys ? "ON" : "OFF"));
            st.execute("PRAGMA busy_timeout = 5000");
        }
        return con;
    }

    public static String getDatabasePath() {
        return new File(DB_FILE).getAbsolutePath();
    }

    // ------------------------------------------------------------------ simple helpers used by the screens

    /** Runs a SELECT and returns all rows as text (NULL becomes an empty string). */
    public static String[][] query(String sql, Object... params) throws SQLException {
        try (Connection con = getConnection()) {
            return query(con, sql, params);
        }
    }

    /** Same as above but on an existing connection (used inside transactions). */
    public static String[][] query(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                int columns = rs.getMetaData().getColumnCount();
                List<String[]> rows = new ArrayList<>();
                while (rs.next()) {
                    String[] row = new String[columns];
                    for (int i = 0; i < columns; i++) {
                        String value = rs.getString(i + 1);
                        row[i] = value == null ? "" : value;
                    }
                    rows.add(row);
                }
                return rows.toArray(new String[0][]);
            }
        }
    }

    /** Returns the first column of the first row as a number (0 if there is no row). */
    public static long number(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    /** Runs INSERT / UPDATE / DELETE and returns the number of affected rows. */
    public static int update(String sql, Object... params) throws SQLException {
        try (Connection con = getConnection()) {
            return update(con, sql, params);
        }
    }

    public static int update(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    /** INSERT that returns the new row id. */
    public static long insert(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            ps.executeUpdate();
        }
        return number(con, "SELECT last_insert_rowid()");
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /**
     * Runs the work inside ONE transaction: everything is saved together, or nothing is saved.
     * After a successful commit the screens are told to refresh (see DataEvents).
     */
    public static <T> T inTransaction(Work<T> work) throws AppException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                T result = work.run(con);
                con.commit();
                DataEvents.fire();
                return result;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (AppException e) {
            throw e;
        } catch (SQLException e) {
            throw Check.fromSql(e);
        } catch (Exception e) {
            throw new AppException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /** Runs read-only work on a fresh connection (no commit, no refresh event). */
    public static <T> T inTransactionRead(Work<T> work) throws AppException {
        try (Connection con = getConnection()) {
            return work.run(con);
        } catch (AppException e) {
            throw e;
        } catch (SQLException e) {
            throw Check.fromSql(e);
        } catch (Exception e) {
            throw new AppException("Unexpected error: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ first run: schema, migration, seed data

    /** Called once when the program starts. Safe to call again: nothing is duplicated. */
    public static void initialize() {
        try {
            boolean legacy = hasLegacySchema();
            if (legacy) {
                backupBeforeMigration();
                migrateLegacy();
            }
            try (Connection con = getConnection()) {
                con.setAutoCommit(false);
                try {
                    createSchema(con);
                    RoomService.seedRooms(con);
                    seedAdmin(con);
                    con.commit();
                } catch (SQLException e) {
                    con.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot prepare the database: " + e.getMessage(), e);
        }
    }

    static void createSchema(Connection con) throws SQLException {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS room ("
            + " room_number INTEGER PRIMARY KEY,"
            + " Capacity INTEGER NOT NULL CHECK (Capacity > 0),"
            + " Rent INTEGER NOT NULL CHECK (Rent >= 0),"
            + " Status TEXT NOT NULL DEFAULT 'Available' CHECK (Status IN ('Available','Unavailable')),"
            + " Discount INTEGER NOT NULL DEFAULT 0 CHECK (Discount >= 0))",

            "CREATE TABLE IF NOT EXISTS students ("
            + " student_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " room_number INTEGER NOT NULL REFERENCES room(room_number),"
            + " FirstName TEXT NOT NULL, LastName TEXT NOT NULL, FName TEXT NOT NULL,"
            + " NIC TEXT NOT NULL UNIQUE,"
            + " PhoneNumber TEXT UNIQUE,"
            + " ArelativePhoneNumber TEXT,"
            + " Faculty TEXT NOT NULL,"
            + " Date_of_entry TEXT NOT NULL,"
            + " Date_of_exit TEXT,"
            + " Status TEXT NOT NULL DEFAULT 'Active' CHECK (Status IN ('Active','Inactive')))",

            "CREATE TABLE IF NOT EXISTS employee ("
            + " employee_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " FirstName TEXT NOT NULL, LastName TEXT NOT NULL, FName TEXT NOT NULL,"
            + " PhoneNumber TEXT NOT NULL UNIQUE,"
            + " Job TEXT NOT NULL,"
            + " PermanentResidence TEXT,"
            + " Hire_date TEXT,"
            + " Status TEXT NOT NULL DEFAULT 'Active' CHECK (Status IN ('Active','Inactive')))",

            // One row = the rent of ONE student for ONE financial period (a month, stored like 202611).
            "CREATE TABLE IF NOT EXISTS payment ("
            + " payment_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " student_id INTEGER NOT NULL REFERENCES students(student_id),"
            + " room_number INTEGER NOT NULL REFERENCES room(room_number),"
            + " Period INTEGER NOT NULL,"
            + " Amount_of_money INTEGER NOT NULL CHECK (Amount_of_money > 0),"
            + " Receipt INTEGER NOT NULL DEFAULT 0 CHECK (Receipt >= 0),"
            + " Remain INTEGER NOT NULL DEFAULT 0 CHECK (Remain >= 0),"
            + " Payment_date TEXT,"
            + " UNIQUE (student_id, Period))",

            "CREATE TABLE IF NOT EXISTS salary ("
            + " Salary_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " employee_id INTEGER NOT NULL REFERENCES employee(employee_id),"
            + " Period INTEGER NOT NULL,"
            + " Amount INTEGER NOT NULL CHECK (Amount > 0),"
            + " Receipt INTEGER NOT NULL DEFAULT 0 CHECK (Receipt >= 0),"
            + " Remain INTEGER NOT NULL DEFAULT 0 CHECK (Remain >= 0),"
            + " Payment_date TEXT,"
            + " UNIQUE (employee_id, Period))",

            "CREATE TABLE IF NOT EXISTS costs ("
            + " product_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " ProductName TEXT NOT NULL,"
            + " Category TEXT NOT NULL DEFAULT 'General',"
            + " price INTEGER NOT NULL CHECK (price > 0),"
            + " Date TEXT NOT NULL,"
            + " Description TEXT NOT NULL DEFAULT '',"
            + " UNIQUE (ProductName, price, Date, Description))",

            "CREATE TABLE IF NOT EXISTS user ("
            + " user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " UserName TEXT NOT NULL UNIQUE,"
            + " Password TEXT NOT NULL,"
            + " Role TEXT NOT NULL DEFAULT 'Admin' CHECK (Role IN ('Admin','User')),"
            + " Status TEXT NOT NULL DEFAULT 'Active' CHECK (Status IN ('Active','Inactive')))",

            "CREATE TABLE IF NOT EXISTS app_setting (key TEXT PRIMARY KEY, value TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS activity_log ("
            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " Created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),"
            + " Category TEXT NOT NULL, Message TEXT NOT NULL)",

            "CREATE INDEX IF NOT EXISTS idx_students_room ON students(room_number, Status)",
            "CREATE INDEX IF NOT EXISTS idx_payment_student ON payment(student_id)",
            "CREATE INDEX IF NOT EXISTS idx_salary_employee ON salary(employee_id)"
        };
        try (Statement st = con.createStatement()) {
            for (String sql : statements) {
                st.execute(sql);
            }
            st.execute("PRAGMA user_version = " + SCHEMA_VERSION);
        }
    }

    /** A default administrator for a brand-new database (password is stored hashed). */
    private static void seedAdmin(Connection con) throws SQLException {
        if (number(con, "SELECT COUNT(*) FROM user") == 0) {
            update(con, "INSERT INTO user (UserName, Password, Role) VALUES (?, ?, 'Admin')",
                    "admin", PasswordUtil.hash("admin123"));
        }
    }

    /** Copies the database to database/backups/ with the date in the name. Returns the new file path. */
    public static String backup() throws SQLException, IOException {
        File source = new File(DB_FILE).getAbsoluteFile();
        File folder = new File(source.getParentFile(), "backups");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("Cannot create " + folder);
        }
        File target = new File(folder, "backup_" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db");
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute("VACUUM INTO '" + target.getPath().replace("'", "''") + "'");   // consistent copy while the program runs
        }
        return target.getPath();
    }

    // ------------------------------------------------------------------ settings (e.g. Light / Dark mode)

    public static String getSetting(String key, String defaultValue) {
        try {
            String[][] rows = query("SELECT value FROM app_setting WHERE key = ?", key);
            return rows.length == 0 ? defaultValue : rows[0][0];
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    public static void setSetting(String key, String value) {
        try {
            update("INSERT INTO app_setting (key, value) VALUES (?, ?) "
                    + "ON CONFLICT(key) DO UPDATE SET value = excluded.value", key, value);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ migration of the old (version 1) database

    private static boolean hasLegacySchema() throws SQLException {
        File file = new File(DB_FILE);
        if (!file.exists()) {
            return false;
        }
        try (Connection con = getConnection()) {
            return columnExists(con, "room", "room_namber") || columnExists(con, "student", "room_namber");
        }
    }

    private static boolean columnExists(Connection con, String table, String column) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void backupBeforeMigration() throws IOException {
        File source = new File(DB_FILE);
        File backup = new File(source.getParentFile(), "backup_before_migration.db");
        Files.copy(source.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static List<Map<String, Object>> readAll(Connection con, String table) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i).toLowerCase(), rs.getObject(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private static String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : value.toString().trim();
    }

    /**
     * Moves the data of the old database layout into the new tables.
     * Rows that break a rule of the new structure (duplicate NIC, full room ...) are NOT lost:
     * they are written to database/migration_report.txt so they can be entered again.
     */
    private static void migrateLegacy() throws Exception {
        StringBuilder report = new StringBuilder("Migration report\n================\n");
        try (Connection con = open(false)) {
            con.setAutoCommit(false);
            try {
                List<Map<String, Object>> students = tableExists(con, "student") ? readAll(con, "student") : new ArrayList<>();
                List<Map<String, Object>> employees = tableExists(con, "employee") ? readAll(con, "employee") : new ArrayList<>();
                List<Map<String, Object>> users = tableExists(con, "user") ? readAll(con, "user") : new ArrayList<>();
                List<Map<String, Object>> costs = tableExists(con, "costs") ? readAll(con, "costs") : new ArrayList<>();
                List<Map<String, Object>> salaries = tableExists(con, "salary") ? readAll(con, "salary") : new ArrayList<>();
                List<Map<String, Object>> payments = tableExists(con, "payment") ? readAll(con, "payment") : new ArrayList<>();

                try (Statement st = con.createStatement()) {
                    for (String t : new String[]{"payment", "salary", "student", "employee", "costs", "user", "room"}) {
                        st.execute("DROP TABLE IF EXISTS " + t);
                    }
                }
                createSchema(con);
                RoomService.seedRooms(con);

                for (Map<String, Object> r : students) {
                    migrateStudent(con, r, report);
                }
                for (Map<String, Object> r : employees) {
                    migrateEmployee(con, r, report);
                }
                for (Map<String, Object> r : users) {
                    String name = text(r, "username");
                    String password = text(r, "password");
                    if (name.isEmpty() || password.isEmpty()) {
                        continue;
                    }
                    update(con, "INSERT OR IGNORE INTO user (UserName, Password, Role) VALUES (?, ?, 'Admin')",
                            name, PasswordUtil.isHashed(password) ? password : PasswordUtil.hash(password));
                }
                for (Map<String, Object> r : costs) {
                    migrateCost(con, r, report);
                }
                if (!salaries.isEmpty() || !payments.isEmpty()) {
                    report.append("Old salary rows: ").append(salaries.size()).append(", old payment rows: ")
                            .append(payments.size()).append(" were not copied (they have no link to a person in the old layout).\n");
                }
                RoomService.refreshAllStatuses(con);
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
        Files.writeString(new File(new File(DB_FILE).getParentFile(), "migration_report.txt").toPath(), report.toString());
    }

    private static boolean tableExists(Connection con, String table) throws SQLException {
        return number(con, "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name = ?", table) > 0;
    }

    private static void migrateStudent(Connection con, Map<String, Object> r, StringBuilder report) {
        try {
            int room = Integer.parseInt(text(r, "room_namber"));
            String entry = fixDate(text(r, "date_of_entry"));
            String exit = fixDate(text(r, "date_of_exit"));
            boolean active = exit == null;
            if (active && number(con, "SELECT COUNT(*) FROM students WHERE room_number = ? AND Status = 'Active'", room)
                    >= number(con, "SELECT Capacity FROM room WHERE room_number = ?", room)) {
                report.append("Student NOT copied (room ").append(room).append(" is already full): ")
                        .append(text(r, "firstname")).append(' ').append(text(r, "lastname"))
                        .append(", NIC ").append(text(r, "nic")).append('\n');
                return;
            }
            update(con, "INSERT INTO students (room_number, FirstName, LastName, FName, NIC, ArelativePhoneNumber, "
                    + "Faculty, Date_of_entry, Date_of_exit, Status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    room, text(r, "firstname"), text(r, "lastname"), text(r, "fname"), text(r, "nic"),
                    Check.optional(text(r, "arelativephonenamber")), text(r, "faculty"),
                    entry == null ? "2000-01-01" : entry, exit, active ? "Active" : "Inactive");
        } catch (Exception e) {
            report.append("Student NOT copied (").append(e.getMessage()).append("): ")
                    .append(text(r, "firstname")).append(' ').append(text(r, "lastname"))
                    .append(", NIC ").append(text(r, "nic")).append(", room ").append(text(r, "room_namber")).append('\n');
        }
    }

    private static void migrateEmployee(Connection con, Map<String, Object> r, StringBuilder report) {
        try {
            update(con, "INSERT INTO employee (FirstName, LastName, FName, PhoneNumber, Job, PermanentResidence, Status) "
                    + "VALUES (?,?,?,?,?,?, 'Active')",
                    text(r, "firstname"), text(r, "lastname"), text(r, "fname"), text(r, "phonenamber"),
                    text(r, "job"), Check.optional(text(r, "permanentresidence")));
        } catch (Exception e) {
            report.append("Employee NOT copied (").append(e.getMessage()).append("): ")
                    .append(text(r, "firstname")).append(' ').append(text(r, "lastname")).append('\n');
        }
    }

    private static void migrateCost(Connection con, Map<String, Object> r, StringBuilder report) {
        try {
            int price = (int) Math.round(Double.parseDouble(text(r, "price")));
            update(con, "INSERT OR IGNORE INTO costs (ProductName, price, Date) VALUES (?,?,?)",
                    text(r, "productname"), price, fixDate(text(r, "date")) == null ? "2000-01-01" : fixDate(text(r, "date")));
        } catch (Exception e) {
            report.append("Cost NOT copied: ").append(text(r, "productname")).append('\n');
        }
    }

    /** Old data had dates like 1405-1-1. Turns them into 1405-01-01; returns null for empty values. */
    private static String fixDate(String value) {
        try {
            return Check.date(value, "Date", false);
        } catch (AppException e) {
            return null;
        }
    }
}
