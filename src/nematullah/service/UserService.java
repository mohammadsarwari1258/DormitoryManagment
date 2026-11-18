package nematullah.service;

import nematullah.database.Database;

/** System users: login, create, edit, deactivate, delete. Passwords are stored hashed. */
public final class UserService {

    public static final String[] HEADER = {"Id", "User Name", "Role", "Status"};
    private static final int MIN_PASSWORD_LENGTH = 4;

    private UserService() {
    }

    /** Returns true and starts a session when the name and password are right and the user is Active. */
    public static boolean login(String userName, String password) throws AppException {
        if (userName == null || userName.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        return Database.inTransactionRead(con -> {
            String[][] r = Database.query(con, "SELECT Password, Role, Status FROM user WHERE UserName = ?", userName.trim());
            if (r.length == 0 || !"Active".equals(r[0][2]) || !PasswordUtil.verify(password, r[0][0])) {
                return false;
            }
            Session.start(userName.trim(), r[0][1]);
            return true;
        });
    }

    private static void checkPassword(String password) throws AppException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new AppException("Password must have at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    public static void add(String userName, String password, String role) throws AppException {
        String name = Check.required(userName, "User name");
        checkPassword(password);
        String r = "User".equals(role) ? "User" : "Admin";
        Database.inTransaction(con -> {
            Database.update(con, "INSERT INTO user (UserName, Password, Role) VALUES (?,?,?)", name, PasswordUtil.hash(password), r);
            Activity.log(con, "User", "User account created: " + name + " (" + r + ")");
            return null;
        });
    }

    /** newPassword may be empty: then the old password stays. */
    public static void update(int id, String userName, String newPassword, String role, boolean active) throws AppException {
        String name = Check.required(userName, "User name");
        boolean changePassword = newPassword != null && !newPassword.isEmpty();
        if (changePassword) {
            checkPassword(newPassword);
        }
        String r = "User".equals(role) ? "User" : "Admin";
        Database.inTransaction(con -> {
            String[][] old = Database.query(con, "SELECT Role, Status FROM user WHERE user_id = ?", id);
            if (old.length == 0) {
                throw new AppException("User not found.");
            }
            boolean wasActiveAdmin = "Admin".equals(old[0][0]) && "Active".equals(old[0][1]);
            if (wasActiveAdmin && (!r.equals("Admin") || !active)) {
                requireAnotherActiveAdmin(con, id);
            }
            Database.update(con, "UPDATE user SET UserName=?, Role=?, Status=? WHERE user_id=?", name, r, active ? "Active" : "Inactive", id);
            if (changePassword) {
                Database.update(con, "UPDATE user SET Password=? WHERE user_id=?", PasswordUtil.hash(newPassword), id);
            }
            Activity.log(con, "User", "User account updated: " + name);
            return null;
        });
    }

    /** A user changes his own password. */
    public static void changeOwnPassword(String userName, String newPassword) throws AppException {
        checkPassword(newPassword);
        Database.inTransaction(con -> {
            if (Database.update(con, "UPDATE user SET Password = ? WHERE UserName = ?", PasswordUtil.hash(newPassword), userName) == 0) {
                throw new AppException("User not found.");
            }
            Activity.log(con, "User", "Password changed for " + userName);
            return null;
        });
    }

    public static void delete(int id) throws AppException {
        Database.inTransaction(con -> {
            String[][] old = Database.query(con, "SELECT UserName, Role, Status FROM user WHERE user_id = ?", id);
            if (old.length == 0) {
                throw new AppException("User not found.");
            }
            if (old[0][0].equals(Session.getUserName())) {
                throw new AppException("You cannot delete the account you are logged in with.");
            }
            if ("Admin".equals(old[0][1]) && "Active".equals(old[0][2])) {
                requireAnotherActiveAdmin(con, id);
            }
            Database.update(con, "DELETE FROM user WHERE user_id = ?", id);
            Activity.log(con, "User", "User account deleted: " + old[0][0]);
            return null;
        });
    }

    private static void requireAnotherActiveAdmin(java.sql.Connection con, int exceptId) throws Exception {
        if (Database.number(con, "SELECT COUNT(*) FROM user WHERE Role='Admin' AND Status='Active' AND user_id <> ?", exceptId) == 0) {
            throw new AppException("There must always be at least one active Admin.");
        }
    }

    public static String[][] list(String search) throws AppException {
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT user_id, UserName, Role, Status FROM user WHERE UserName LIKE ? OR Role LIKE ? ORDER BY user_id", like, like));
    }
}
