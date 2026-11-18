package nematullah.service;

import nematullah.database.Database;

/** Employee salaries. One row = the salary of ONE employee for ONE period (month). */
public final class SalaryService {

    public static final String[] HEADER = {"Id", "Employee Id", "Employee", "Period", "Salary", "Paid", "Unpaid", "Payment Date"};

    private SalaryService() {
    }

    public static void add(int employeeId, String periodText, String amountText, String receiptText, String dateText) throws AppException {
        int period = Check.period(periodText, "Period");
        int amount = Check.positiveInt(amountText, "Salary amount");
        int receipt = receiptText == null || receiptText.trim().isEmpty() ? 0 : Check.nonNegativeInt(receiptText, "Paid amount");
        if (receipt > amount) {
            throw new AppException("The paid amount cannot be larger than the salary.");
        }
        String date = Check.date(dateText, "Payment date", receipt > 0);
        Database.inTransaction(con -> {
            String[][] e = Database.query(con, "SELECT FirstName || ' ' || LastName, Status FROM employee WHERE employee_id = ?", employeeId);
            if (e.length == 0) {
                throw new AppException("No employee with this ID exists.");
            }
            if (!"Active".equals(e[0][1])) {
                throw new AppException("This employee is inactive.");
            }
            Database.update(con, "INSERT INTO salary (employee_id, Period, Amount, Receipt, Remain, Payment_date) VALUES (?,?,?,?,?,?)",
                    employeeId, period, amount, receipt, amount - receipt, receipt > 0 ? date : null);
            Activity.log(con, "Salary", "Salary for " + e[0][0] + ", period " + Check.showPeriod(period) + ": paid " + receipt + " of " + amount + " AFN");
            return null;
        });
    }

    public static void update(int salaryId, String periodText, String amountText, String receiptText, String dateText) throws AppException {
        int period = Check.period(periodText, "Period");
        int amount = Check.positiveInt(amountText, "Salary amount");
        int receipt = Check.nonNegativeInt(receiptText, "Paid amount");
        if (receipt > amount) {
            throw new AppException("The paid amount cannot be larger than the salary.");
        }
        String date = Check.date(dateText, "Payment date", receipt > 0);
        Database.inTransaction(con -> {
            int changed = Database.update(con, "UPDATE salary SET Period=?, Amount=?, Receipt=?, Remain=?, Payment_date=? WHERE Salary_id=?",
                    period, amount, receipt, amount - receipt, receipt > 0 ? date : null, salaryId);
            if (changed == 0) {
                throw new AppException("Salary record not found.");
            }
            Activity.log(con, "Salary", "Salary #" + salaryId + " edited (paid " + receipt + " of " + amount + " AFN)");
            return null;
        });
    }

    public static void delete(int salaryId) throws AppException {
        Database.inTransaction(con -> {
            if (Database.update(con, "DELETE FROM salary WHERE Salary_id = ?", salaryId) == 0) {
                throw new AppException("Salary record not found.");
            }
            Activity.log(con, "Salary", "Salary record #" + salaryId + " deleted");
            return null;
        });
    }

    /** employeeId = 0 shows all employees. */
    public static String[][] list(String search, int employeeId) throws AppException {
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT s.Salary_id, s.employee_id, e.FirstName || ' ' || e.LastName, substr(s.Period,1,4) || '-' || substr(s.Period,5,2), "
                + "s.Amount, s.Receipt, s.Remain, COALESCE(s.Payment_date,'') FROM salary s JOIN employee e ON e.employee_id = s.employee_id "
                + "WHERE (? = 0 OR s.employee_id = ?) AND ((e.FirstName || ' ' || e.LastName) LIKE ? OR CAST(s.Period AS TEXT) LIKE ? "
                + "OR CAST(s.employee_id AS TEXT) LIKE ? OR s.Payment_date LIKE ?) ORDER BY s.Period DESC, s.Salary_id DESC",
                employeeId, employeeId, like, like, like, like));
    }

    /** {total salary paid, total salary not yet paid}. */
    public static long[] totals() throws AppException {
        return Database.inTransactionRead(con -> {
            String[][] r = Database.query(con, "SELECT COALESCE(SUM(Receipt),0), COALESCE(SUM(Remain),0) FROM salary");
            return new long[]{Long.parseLong(r[0][0]), Long.parseLong(r[0][1])};
        });
    }
}
