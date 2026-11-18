package nematullah.service;

import nematullah.database.Database;

/** Employee operations. An employee with salary history is deactivated instead of deleted. */
public final class EmployeeService {

    public static class Employee {
        public String firstName, lastName, fatherName, phone, job, residence, hireDate;
    }

    public static final String[] HEADER = {"Id", "First Name", "Last Name", "Father Name", "Phone", "Job",
        "Permanent Residence", "Hire Date", "Status"};

    private EmployeeService() {
    }

    private static void validate(Employee e) throws AppException {
        e.firstName = Check.required(e.firstName, "First name");
        e.lastName = Check.required(e.lastName, "Last name");
        e.fatherName = Check.required(e.fatherName, "Father name");
        e.phone = Check.required(e.phone, "Phone number");
        e.job = Check.required(e.job, "Job");
        e.residence = Check.optional(e.residence);
        e.hireDate = Check.date(e.hireDate, "Hire date", true);
    }

    public static void add(Employee e) throws AppException {
        validate(e);
        Database.inTransaction(con -> {
            Database.update(con, "INSERT INTO employee (FirstName, LastName, FName, PhoneNumber, Job, PermanentResidence, Hire_date, Status) "
                    + "VALUES (?,?,?,?,?,?,?, 'Active')", e.firstName, e.lastName, e.fatherName, e.phone, e.job, e.residence, e.hireDate);
            Activity.log(con, "Employee", "Employee registered: " + e.firstName + " " + e.lastName + " (" + e.job + ")");
            return null;
        });
    }

    public static void update(int id, Employee e) throws AppException {
        validate(e);
        Database.inTransaction(con -> {
            int changed = Database.update(con, "UPDATE employee SET FirstName=?, LastName=?, FName=?, PhoneNumber=?, Job=?, "
                    + "PermanentResidence=?, Hire_date=? WHERE employee_id=?",
                    e.firstName, e.lastName, e.fatherName, e.phone, e.job, e.residence, e.hireDate, id);
            if (changed == 0) {
                throw new AppException("Employee not found.");
            }
            Activity.log(con, "Employee", "Employee updated: " + e.firstName + " " + e.lastName);
            return null;
        });
    }

    public static void setActive(int id, boolean active) throws AppException {
        Database.inTransaction(con -> {
            String[][] r = Database.query(con, "SELECT FirstName || ' ' || LastName FROM employee WHERE employee_id = ?", id);
            if (r.length == 0) {
                throw new AppException("Employee not found.");
            }
            Database.update(con, "UPDATE employee SET Status = ? WHERE employee_id = ?", active ? "Active" : "Inactive", id);
            Activity.log(con, "Employee", "Employee " + (active ? "activated: " : "deactivated: ") + r[0][0]);
            return null;
        });
    }

    public static void delete(int id) throws AppException {
        Database.inTransaction(con -> {
            String[][] r = Database.query(con, "SELECT FirstName || ' ' || LastName FROM employee WHERE employee_id = ?", id);
            if (r.length == 0) {
                throw new AppException("Employee not found.");
            }
            if (Database.number(con, "SELECT COUNT(*) FROM salary WHERE employee_id = ?", id) > 0) {
                throw new AppException("This employee has salary records, so he cannot be deleted. Deactivate him instead.");
            }
            Database.update(con, "DELETE FROM employee WHERE employee_id = ?", id);
            Activity.log(con, "Employee", "Employee deleted: " + r[0][0]);
            return null;
        });
    }

    public static String[][] list(String search) throws AppException {
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT employee_id, FirstName, LastName, FName, PhoneNumber, Job, COALESCE(PermanentResidence,''), "
                + "COALESCE(Hire_date,''), Status FROM employee WHERE FirstName LIKE ? OR LastName LIKE ? "
                + "OR (FirstName || ' ' || LastName) LIKE ? OR PhoneNumber LIKE ? OR Job LIKE ? OR Status LIKE ? ORDER BY employee_id",
                like, like, like, like, like, like));
    }

    /** Drop-down items like "2 - Hamid Karimi (Guard)" for ACTIVE employees. */
    public static String[] options() throws AppException {
        return Database.inTransactionRead(con -> {
            String[][] rows = Database.query(con, "SELECT employee_id || ' - ' || FirstName || ' ' || LastName || ' (' || Job || ')' "
                    + "FROM employee WHERE Status = 'Active' ORDER BY employee_id");
            String[] items = new String[rows.length];
            for (int i = 0; i < rows.length; i++) {
                items[i] = rows[i][0];
            }
            return items;
        });
    }

    public static String[] find(int id) throws AppException {
        return Database.inTransactionRead(con -> {
            String[][] r = Database.query(con, "SELECT FirstName, LastName, FName, PhoneNumber, Job, PermanentResidence, Hire_date "
                    + "FROM employee WHERE employee_id = ?", id);
            return r.length == 0 ? null : r[0];
        });
    }

    public static String details(int id) throws AppException {
        return Database.inTransactionRead(con -> {
            String[][] r = Database.query(con, "SELECT FirstName, LastName, FName, PhoneNumber, Job, PermanentResidence, Hire_date, Status "
                    + "FROM employee WHERE employee_id = ?", id);
            if (r.length == 0) {
                throw new AppException("Employee not found.");
            }
            String[] e = r[0];
            String[][] pay = Database.query(con, "SELECT COUNT(*), COALESCE(SUM(Amount),0), COALESCE(SUM(Receipt),0), "
                    + "COALESCE(SUM(Remain),0) FROM salary WHERE employee_id = ?", id);
            return "Name: " + e[0] + " " + e[1] + "\nFather name: " + e[2] + "\nPhone: " + e[3] + "\nJob: " + e[4]
                    + "\nPermanent residence: " + e[5] + "\nHire date: " + e[6] + "\nStatus: " + e[7]
                    + "\n\nSalary periods: " + pay[0][0] + "\nTotal salary: " + pay[0][1] + " AFN\nTotal paid: " + pay[0][2]
                    + " AFN\nUnpaid: " + pay[0][3] + " AFN";
        });
    }
}
