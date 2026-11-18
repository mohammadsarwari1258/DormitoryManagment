package nematullah.service;

/** Remembers who is logged in (needed for role checks such as "only Admin can manage users"). */
public final class Session {

    private static String userName;
    private static String role;

    private Session() {
    }

    public static void start(String name, String userRole) {
        userName = name;
        role = userRole;
    }

    public static void end() {
        userName = null;
        role = null;
    }

    public static String getUserName() {
        return userName;
    }

    public static boolean isAdmin() {
        return "Admin".equals(role);
    }
}
