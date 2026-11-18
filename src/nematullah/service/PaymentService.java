package nematullah.service;

import java.sql.Connection;
import java.sql.SQLException;
import nematullah.database.Database;

/**
 * Rent payments. One row = what ONE student must pay for ONE period (month) and how much he really paid.
 * Status: Paid (nothing left), Partial (some money paid), Unpaid (nothing paid).
 */
public final class PaymentService {

    /** The status calculated from the numbers (never stored, so it can never be wrong). */
    private static final String STATUS_SQL =
            "CASE WHEN p.Receipt >= p.Amount_of_money THEN 'Paid' WHEN p.Receipt = 0 THEN 'Unpaid' ELSE 'Partial' END";

    public static final String[] HEADER = {"Id", "Student Id", "Student", "Room", "Period", "Amount Due",
        "Paid", "Remaining", "Status", "Payment Date"};

    private PaymentService() {
    }

    /** Suggested amount = monthly rent of the student's room minus the room discount. */
    public static int defaultDue(int studentId) throws AppException {
        return Database.inTransactionRead(con -> (int) Database.number(con,
                "SELECT MAX(r.Rent - r.Discount, 0) FROM students s JOIN room r ON r.room_number = s.room_number "
                + "WHERE s.student_id = ?", studentId));
    }

    private static void checkAmounts(int due, int receipt) throws AppException {
        if (receipt > due) {
            throw new AppException("The paid amount cannot be larger than the amount due.");
        }
    }

    private static String paymentDate(int receipt, String text) throws AppException {
        String date = Check.date(text, "Payment date", receipt > 0);
        return receipt > 0 ? date : null;
    }

    public static void add(int studentId, String periodText, String dueText, String receiptText, String dateText) throws AppException {
        int period = Check.period(periodText, "Period");
        int due = Check.positiveInt(dueText, "Amount due");
        int receipt = receiptText == null || receiptText.trim().isEmpty() ? 0 : Check.nonNegativeInt(receiptText, "Paid amount");
        checkAmounts(due, receipt);
        String date = paymentDate(receipt, dateText);
        Database.inTransaction(con -> {
            String[][] st = Database.query(con, "SELECT room_number, FirstName || ' ' || LastName FROM students WHERE student_id = ?", studentId);
            if (st.length == 0) {
                throw new AppException("Student not found.");
            }
            Database.update(con, "INSERT INTO payment (student_id, room_number, Period, Amount_of_money, Receipt, Remain, Payment_date) "
                    + "VALUES (?,?,?,?,?,?,?)", studentId, Integer.parseInt(st[0][0]), period, due, receipt, due - receipt, date);
            Activity.log(con, "Payment", "Payment for " + st[0][1] + ", period " + Check.showPeriod(period)
                    + ": paid " + receipt + " of " + due + " AFN");
            return null;
        });
    }

    public static void update(int paymentId, String periodText, String dueText, String receiptText, String dateText) throws AppException {
        int period = Check.period(periodText, "Period");
        int due = Check.positiveInt(dueText, "Amount due");
        int receipt = Check.nonNegativeInt(receiptText, "Paid amount");
        checkAmounts(due, receipt);
        String date = paymentDate(receipt, dateText);
        Database.inTransaction(con -> {
            int changed = Database.update(con, "UPDATE payment SET Period=?, Amount_of_money=?, Receipt=?, Remain=?, Payment_date=? "
                    + "WHERE payment_id=?", period, due, receipt, due - receipt, date, paymentId);
            if (changed == 0) {
                throw new AppException("Payment not found.");
            }
            Activity.log(con, "Payment", "Payment #" + paymentId + " edited (period " + Check.showPeriod(period)
                    + ", paid " + receipt + " of " + due + " AFN)");
            return null;
        });
    }

    /** Deleting a payment is written to the activity log so the change can be traced. */
    public static void delete(int paymentId) throws AppException {
        Database.inTransaction(con -> {
            String[][] p = Database.query(con, "SELECT Period, Receipt, Amount_of_money FROM payment WHERE payment_id = ?", paymentId);
            if (p.length == 0) {
                throw new AppException("Payment not found.");
            }
            Database.update(con, "DELETE FROM payment WHERE payment_id = ?", paymentId);
            Activity.log(con, "Payment", "Payment #" + paymentId + " deleted (period " + Check.showPeriod(Integer.parseInt(p[0][0]))
                    + ", paid " + p[0][1] + " of " + p[0][2] + " AFN)");
            return null;
        });
    }

    /** List for the Payment screen. studentId = 0 means all students. */
    public static String[][] list(String search, int studentId) throws AppException {
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT p.payment_id, p.student_id, s.FirstName || ' ' || s.LastName, p.room_number, "
                + "substr(p.Period,1,4) || '-' || substr(p.Period,5,2), p.Amount_of_money, p.Receipt, p.Remain, "
                + STATUS_SQL + ", COALESCE(p.Payment_date, '') "
                + "FROM payment p JOIN students s ON s.student_id = p.student_id "
                + "WHERE (? = 0 OR p.student_id = ?) AND ((s.FirstName || ' ' || s.LastName) LIKE ? OR CAST(p.room_number AS TEXT) LIKE ? "
                + "OR CAST(p.Period AS TEXT) LIKE ? OR (" + STATUS_SQL + ") LIKE ? OR p.Payment_date LIKE ?) "
                + "ORDER BY p.Period DESC, p.payment_id DESC", studentId, studentId, like, like, like, like, like));
    }

    /** {number of periods, total due, total paid, remaining debt} of one student. */
    public static long[] summary(Connection con, int studentId) throws SQLException {
        String[][] r = Database.query(con, "SELECT COUNT(*), COALESCE(SUM(Amount_of_money),0), COALESCE(SUM(Receipt),0), "
                + "COALESCE(SUM(Remain),0) FROM payment WHERE student_id = ?", studentId);
        return new long[]{Long.parseLong(r[0][0]), Long.parseLong(r[0][1]), Long.parseLong(r[0][2]), Long.parseLong(r[0][3])};
    }

    public static final String[] BALANCE_HEADER = {"Student Id", "Student", "Room", "Student Status", "Periods",
        "Total Due", "Total Paid", "Debt", "Payment Status"};

    /** Debt of every student, calculated from the real payment rows. */
    public static String[][] balances() throws AppException {
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT s.student_id, s.FirstName || ' ' || s.LastName, s.room_number, s.Status, COUNT(p.payment_id), "
                + "COALESCE(SUM(p.Amount_of_money),0), COALESCE(SUM(p.Receipt),0), COALESCE(SUM(p.Remain),0), "
                + "CASE WHEN COUNT(p.payment_id) = 0 THEN 'Unpaid' WHEN SUM(p.Remain) = 0 THEN 'Paid' "
                + "WHEN SUM(p.Receipt) = 0 THEN 'Unpaid' ELSE 'Partial' END "
                + "FROM students s LEFT JOIN payment p ON p.student_id = s.student_id GROUP BY s.student_id ORDER BY s.student_id"));
    }

    /**
     * Creates an "Unpaid" row for every active student for the given period. Students who already have
     * a row for that period are skipped (the UNIQUE rule), so running it twice is safe.
     */
    public static int generateDues(String periodText) throws AppException {
        int period = Check.period(periodText, "Period");
        return Database.inTransaction(con -> {
            int created = Database.update(con, "INSERT OR IGNORE INTO payment (student_id, room_number, Period, Amount_of_money, Receipt, Remain) "
                    + "SELECT s.student_id, s.room_number, ?, r.Rent - r.Discount, 0, r.Rent - r.Discount FROM students s "
                    + "JOIN room r ON r.room_number = s.room_number WHERE s.Status = 'Active' AND r.Rent - r.Discount > 0", period);
            Activity.log(con, "Payment", "Dues generated for period " + Check.showPeriod(period) + ": " + created + " new records");
            return created;
        });
    }
}
