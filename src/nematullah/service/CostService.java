package nematullah.service;

import nematullah.database.Database;

/** Dormitory costs (expenses). */
public final class CostService {

    public static final String[] CATEGORIES = {"General", "Food", "Electricity", "Water", "Gas", "Internet",
        "Repair", "Cleaning", "Furniture", "Other"};
    public static final String[] HEADER = {"Id", "Title", "Category", "Price", "Date", "Description"};

    private CostService() {
    }

    public static void add(String title, String category, String priceText, String dateText, String description) throws AppException {
        String name = Check.required(title, "Title");
        int price = Check.positiveInt(priceText, "Price");
        String date = Check.date(dateText, "Date", true);
        String cat = category == null || category.trim().isEmpty() ? "General" : category.trim();
        String desc = description == null ? "" : description.trim();
        Database.inTransaction(con -> {
            Database.update(con, "INSERT INTO costs (ProductName, Category, price, Date, Description) VALUES (?,?,?,?,?)",
                    name, cat, price, date, desc);
            Activity.log(con, "Cost", "Cost recorded: " + name + " (" + price + " AFN)");
            return null;
        });
    }

    public static void update(int id, String title, String category, String priceText, String dateText, String description) throws AppException {
        String name = Check.required(title, "Title");
        int price = Check.positiveInt(priceText, "Price");
        String date = Check.date(dateText, "Date", true);
        String cat = category == null || category.trim().isEmpty() ? "General" : category.trim();
        String desc = description == null ? "" : description.trim();
        Database.inTransaction(con -> {
            if (Database.update(con, "UPDATE costs SET ProductName=?, Category=?, price=?, Date=?, Description=? WHERE product_id=?",
                    name, cat, price, date, desc, id) == 0) {
                throw new AppException("Cost not found.");
            }
            Activity.log(con, "Cost", "Cost edited: " + name + " (" + price + " AFN)");
            return null;
        });
    }

    public static void delete(int id) throws AppException {
        Database.inTransaction(con -> {
            String[][] r = Database.query(con, "SELECT ProductName, price FROM costs WHERE product_id = ?", id);
            if (r.length == 0) {
                throw new AppException("Cost not found.");
            }
            Database.update(con, "DELETE FROM costs WHERE product_id = ?", id);
            Activity.log(con, "Cost", "Cost deleted: " + r[0][0] + " (" + r[0][1] + " AFN)");
            return null;
        });
    }

    /** from / to are optional dates (yyyy-MM-dd); empty means "no limit". */
    public static String[][] list(String search, String from, String to) throws AppException {
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        String f = Check.date(from, "From date", false);
        String t = Check.date(to, "To date", false);
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT product_id, ProductName, Category, price, Date, Description FROM costs "
                + "WHERE (ProductName LIKE ? OR Category LIKE ? OR Description LIKE ?) AND (? IS NULL OR Date >= ?) AND (? IS NULL OR Date <= ?) "
                + "ORDER BY Date DESC, product_id DESC", like, like, like, f, f, t, t));
    }

    public static long total(String from, String to) throws AppException {
        String f = Check.date(from, "From date", false);
        String t = Check.date(to, "To date", false);
        return Database.inTransactionRead(con -> Database.number(con,
                "SELECT COALESCE(SUM(price),0) FROM costs WHERE (? IS NULL OR Date >= ?) AND (? IS NULL OR Date <= ?)", f, f, t, t));
    }

    /** Totals per category for a date range (cost report). */
    public static String[][] reportByCategory(String from, String to) throws AppException {
        String f = Check.date(from, "From date", false);
        String t = Check.date(to, "To date", false);
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT Category, COUNT(*), SUM(price) FROM costs WHERE (? IS NULL OR Date >= ?) AND (? IS NULL OR Date <= ?) "
                + "GROUP BY Category ORDER BY SUM(price) DESC", f, f, t, t));
    }
}
