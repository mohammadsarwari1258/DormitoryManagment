package nematullah;

import java.sql.Connection;
import nematullah.database.Database;
import nematullah.service.*;

/** Headless test of the database and service layer (no windows). Run: see README "Running the tests". */
public class ServiceTest {
    static int passed = 0, failed = 0;

    interface Step { void run() throws Exception; }

    static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("PASS  " + name); }
        else { failed++; System.out.println("FAIL  " + name); }
    }

    /** The step must throw an AppException (a refused operation). */
    static void refused(String name, Step step) {
        try { step.run(); check(name + " (should be refused)", false); }
        catch (AppException e) { check(name + " -> \"" + e.getMessage() + "\"", true); }
        catch (Exception e) { check(name + " threw " + e, false); }
    }

    static long n(String sql, Object... p) throws Exception {
        try (Connection c = Database.getConnection()) { return Database.number(c, sql, p); }
    }

    static StudentService.Student student(String room, String first, String nic) {
        StudentService.Student s = new StudentService.Student();
        s.room = room; s.firstName = first; s.lastName = "Test"; s.fatherName = "Father"; s.nic = nic;
        s.faculty = "Computer Science"; s.entryDate = "2026-01-05"; s.phone = null; s.relativePhone = "0700000000";
        return s;
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "logic";
        if (mode.equals("migrate")) migration(); else logic();
        System.out.println("\nRESULT: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }

    static void migration() throws Exception {
        String[][] oldUsers = Database.query("SELECT username, Password FROM user");
        long oldStudents = n("SELECT COUNT(*) FROM student");
        Database.initialize();
        check("room count is 15", n("SELECT COUNT(*) FROM room") == 15);
        check("4 rooms of 4 / 6 rooms of 5 / 5 rooms of 3",
                n("SELECT COUNT(*) FROM room WHERE Capacity=4") == 4 && n("SELECT COUNT(*) FROM room WHERE Capacity=5") == 6
                && n("SELECT COUNT(*) FROM room WHERE Capacity=3") == 5);
        check("rents 1200/1500/1800", n("SELECT COUNT(*) FROM room WHERE (Capacity=3 AND Rent=1200) OR (Capacity=4 AND Rent=1500) OR (Capacity=5 AND Rent=1800)") == 15);
        long copied = n("SELECT COUNT(*) FROM students");
        System.out.println("INFO  old students: " + oldStudents + ", copied: " + copied + " (the rest are in migration_report.txt)");
        check("no room is over capacity", n("SELECT COUNT(*) FROM room r WHERE (SELECT COUNT(*) FROM students s WHERE s.room_number=r.room_number AND s.Status='Active') > r.Capacity") == 0);
        check("room status matches occupancy", n("SELECT COUNT(*) FROM room r WHERE r.Status <> CASE WHEN (SELECT COUNT(*) FROM students s WHERE s.room_number=r.room_number AND s.Status='Active') >= r.Capacity THEN 'Unavailable' ELSE 'Available' END") == 0);
        check("old users kept", n("SELECT COUNT(*) FROM user") == oldUsers.length);
        check("no plain-text passwords", n("SELECT COUNT(*) FROM user WHERE Password NOT LIKE 'pbkdf2$%'") == 0);
        check("old password still logs in", UserService.login(oldUsers[0][0], oldUsers[0][1]));
        check("wrong password refused", !UserService.login(oldUsers[0][0], "wrong-password"));
        Database.initialize();
        check("second start creates nothing twice", n("SELECT COUNT(*) FROM room") == 15 && n("SELECT COUNT(*) FROM user") == oldUsers.length && n("SELECT COUNT(*) FROM students") == copied);
        check("foreign keys are ON", n("PRAGMA foreign_keys") == 1);
    }

    static void logic() throws Exception {
        Database.initialize();
        check("15 rooms on first start", n("SELECT COUNT(*) FROM room") == 15);
        Database.initialize();
        check("still 15 rooms after second start", n("SELECT COUNT(*) FROM room") == 15);
        check("default admin can log in", UserService.login("admin", "admin123"));
        check("admin password is hashed", n("SELECT COUNT(*) FROM user WHERE Password LIKE 'pbkdf2$%'") == 1);

        // ---- rooms and students
        int a = StudentService.add(student("11", "Ali", "N-001"));   // room 11 holds 3
        int b = StudentService.add(student("11", "Bashir", "N-002"));
        int c = StudentService.add(student("11", "Cyrus", "N-003"));
        check("room 11 full -> Unavailable", Database.query("SELECT Status FROM room WHERE room_number=11")[0][0].equals("Unavailable"));
        refused("4th student in a 3-bed room", () -> StudentService.add(student("11", "Dawood", "N-004")));
        refused("duplicate NIC", () -> StudentService.add(student("1", "Eve", "N-001")));
        refused("missing first name", () -> StudentService.add(student("1", " ", "N-005")));
        refused("unknown room", () -> StudentService.add(student("99", "Zed", "N-006")));
        refused("bad date", () -> { StudentService.Student s = student("1", "Zed", "N-007"); s.entryDate = "21/3/2026"; StudentService.add(s); });
        StudentService.transfer(a, "1");
        check("transfer frees room 11", Database.query("SELECT Status FROM room WHERE room_number=11")[0][0].equals("Available"));
        refused("transfer to a full room", () -> { for (int i = 0; i < 3; i++) StudentService.add(student("12", "Fill" + i, "F-" + i)); StudentService.transfer(a, "12"); });
        refused("transfer to the same room", () -> StudentService.transfer(a, "1"));
        check("occupants of room 1 = 1", n("SELECT COUNT(*) FROM students WHERE room_number=1 AND Status='Active'") == 1);

        // ---- payments
        PaymentService.add(a, "2026-01", "1500", "1500", "2026-01-10");
        PaymentService.add(b, "202601", "1200", "500", "2026-01-11");
        PaymentService.add(c, "202601", "1200", "0", "");
        refused("duplicate payment for same student+period", () -> PaymentService.add(a, "202601", "1500", "100", "2026-01-12"));
        refused("negative amount", () -> PaymentService.add(a, "202602", "-5", "0", ""));
        refused("zero amount", () -> PaymentService.add(a, "202602", "0", "0", ""));
        refused("paying more than due", () -> PaymentService.add(a, "202602", "1500", "1600", "2026-02-01"));
        refused("bad period", () -> PaymentService.add(a, "202613", "1500", "0", ""));
        refused("paid amount without a date", () -> PaymentService.add(a, "202602", "1500", "100", ""));
        String[][] list = PaymentService.list("", 0);
        check("statuses Paid/Partial/Unpaid", has(list, "Paid") && has(list, "Partial") && has(list, "Unpaid"));
        check("student b debt = 700", PaymentService.summary(con(), b)[3] == 700);
        check("default due of student a = room 1 rent (1500)", PaymentService.defaultDue(a) == 1500);
        refused("delete student with payments", () -> StudentService.delete(a));
        StudentService.exit(b, "2026-02-01");
        check("exit frees the bed", n("SELECT COUNT(*) FROM students WHERE room_number=11 AND Status='Active'") == 1);
        check("payment history kept after exit", PaymentService.summary(con(), b)[0] == 1 && PaymentService.summary(con(), b)[3] == 700);
        refused("exit twice", () -> StudentService.exit(b, "2026-02-02"));
        int created = PaymentService.generateDues("202602");
        check("dues generated only for active students", created == n("SELECT COUNT(*) FROM students WHERE Status='Active'"));
        check("generating twice creates nothing", PaymentService.generateDues("202602") == 0);
        PaymentService.update((int) n("SELECT payment_id FROM payment WHERE student_id=? AND Period=202601", b), "202601", "1200", "1200", "2026-02-03");
        check("partial -> paid after editing", PaymentService.summary(con(), b)[3] == 0);

        // ---- employees and salary
        EmployeeService.Employee e = new EmployeeService.Employee();
        e.firstName = "Hamid"; e.lastName = "Karimi"; e.fatherName = "Rahim"; e.phone = "0799000111"; e.job = "Guard"; e.hireDate = "2026-01-01";
        EmployeeService.add(e);
        int eid = (int) n("SELECT employee_id FROM employee WHERE PhoneNumber='0799000111'");
        refused("duplicate employee phone", () -> EmployeeService.add(e));
        SalaryService.add(eid, "202601", "10000", "10000", "2026-01-30");
        SalaryService.add(eid, "202602", "10000", "4000", "2026-02-28");
        refused("duplicate salary period", () -> SalaryService.add(eid, "202601", "10000", "0", ""));
        refused("salary for unknown employee", () -> SalaryService.add(9999, "202601", "10000", "0", ""));
        long[] sal = SalaryService.totals();
        check("salary totals paid 14000 / unpaid 6000", sal[0] == 14000 && sal[1] == 6000);
        refused("delete employee with salary history", () -> EmployeeService.delete(eid));
        EmployeeService.setActive(eid, false);
        refused("salary for inactive employee", () -> SalaryService.add(eid, "202603", "10000", "0", ""));

        // ---- costs
        CostService.add("Rice", "Food", "3000", "2026-01-15", "");
        CostService.add("Lamp", "Repair", "500", "2026-02-02", "");
        refused("duplicate cost", () -> CostService.add("Rice", "Food", "3000", "2026-01-15", ""));
        refused("negative cost", () -> CostService.add("Bad", "Food", "-1", "2026-01-15", ""));
        check("cost total 3500", CostService.total("", "") == 3500);
        check("cost report for January = 3000", CostService.total("2026-01-01", "2026-01-31") == 3000);

        // ---- users
        UserService.add("manager", "pass1234", "User");
        refused("duplicate username", () -> UserService.add("manager", "other1234", "User"));
        refused("short password", () -> UserService.add("tiny", "1", "User"));
        check("new user can log in", UserService.login("manager", "pass1234"));
        int adminId = (int) n("SELECT user_id FROM user WHERE UserName='admin'");
        refused("demote the only admin", () -> UserService.update(adminId, "admin", "", "User", true));
        UserService.update((int) n("SELECT user_id FROM user WHERE UserName='manager'"), "manager", "", "User", false);
        check("inactive user cannot log in", !UserService.login("manager", "pass1234"));

        // ---- dashboard matches the database
        DashboardService.Stats s = DashboardService.load();
        check("dashboard students = db", s.totalStudents == n("SELECT COUNT(*) FROM students") && s.activeStudents == n("SELECT COUNT(*) FROM students WHERE Status='Active'"));
        check("dashboard capacity 61 (4*4 + 6*5 + 5*3)", s.totalCapacity == 4 * 4 + 6 * 5 + 5 * 3);
        check("dashboard occupancy % from real data", s.occupancyPercent == Math.round(s.currentOccupancy * 100.0 / s.totalCapacity));
        check("income = money received", s.totalIncome == n("SELECT SUM(Receipt) FROM payment"));
        check("unpaid is not income", s.totalUnpaidAmount == n("SELECT SUM(Remain) FROM payment") && s.totalIncome + s.totalUnpaidAmount == n("SELECT SUM(Amount_of_money) FROM payment"));
        check("net = income - costs - salary", s.netBalance == s.totalIncome - s.totalCosts - s.totalSalary);
        check("recent activities exist", s.recent.length > 0);
        //check("months chart has data", s.months.size() > 0);
        try (Connection con = Database.getConnection()) {
            try { Database.update(con, "INSERT INTO students (room_number, FirstName, LastName, FName, NIC, Faculty, Date_of_entry) VALUES (777,'x','x','x','x','x','2026-01-01')"); check("foreign key blocks unknown room", false); }
            catch (java.sql.SQLException ex) { check("foreign key blocks unknown room", true); }
        }
        check("no room over capacity", n("SELECT COUNT(*) FROM room r WHERE (SELECT COUNT(*) FROM students s WHERE s.room_number=r.room_number AND s.Status='Active') > r.Capacity") == 0);
    }

    static Connection con() throws Exception { return Database.getConnection(); }

    static boolean has(String[][] rows, String status) {
        for (String[] r : rows) if (r[8].equals(status)) return true;
        return false;
    }
}
