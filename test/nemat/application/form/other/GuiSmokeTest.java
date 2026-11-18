package nemat.application.form.other;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import nematullah.database.Database;
import nematullah.service.*;

/** Creates every screen without a display, checks the tables are filled from SQLite and saves pictures. */
public class GuiSmokeTest {
    static int passed, failed;

    static void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
    }

    static StudentService.Student st(String room, String name, String nic) {
        StudentService.Student s = new StudentService.Student();
        s.room = room; s.firstName = name; s.lastName = "Ahmadi"; s.fatherName = "Karim"; s.nic = nic;
        s.faculty = "Engineering"; s.entryDate = "2026-09-01"; s.relativePhone = "0700123456";
        return s;
    }

    static void shot(JComponent c, String file) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                c.setSize(1100, 760);
                c.doLayout();
                java.awt.Container p = c;
                layoutAll(c);
                BufferedImage img = new BufferedImage(1100, 760, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(UIManager.getColor("Panel.background"));
                g.fillRect(0, 0, 1100, 760);
                c.paint(g);
                g.dispose();
                new File("/tmp/shots").mkdirs();
                ImageIO.write(img, "png", new File("/tmp/shots/" + file));
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    static void layoutAll(java.awt.Component c) {
        if (c instanceof java.awt.Container) {
            ((java.awt.Container) c).doLayout();
            for (java.awt.Component k : ((java.awt.Container) c).getComponents()) layoutAll(k);
        }
    }

    interface Maker<T> { T make(); }

    static <T> T onEdt(Maker<T> m) throws Exception {
        Object[] box = new Object[1];
        SwingUtilities.invokeAndWait(() -> box[0] = m.make());
        @SuppressWarnings("unchecked") T t = (T) box[0];
        return t;
    }

    public static void main(String[] args) throws Exception {
        Database.initialize();
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("nemat.theme");
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));
        FlatMacDarkLaf.setup();

        // sample data
        for (int i = 1; i <= 7; i++) StudentService.add(st(String.valueOf(i <= 3 ? 11 : 1 + i % 4), "Student" + i, "NIC-" + i));
        int s1 = (int) number("SELECT MIN(student_id) FROM students");
        PaymentService.add(s1, "2026-09", "1200", "1200", "2026-09-05");
        PaymentService.add(s1 + 3, "2026-09", "1500", "600", "2026-09-06");
        PaymentService.generateDues("2026-10");
        EmployeeService.Employee e = new EmployeeService.Employee();
        e.firstName = "Hamid"; e.lastName = "Karimi"; e.fatherName = "Rahim"; e.phone = "0799000111"; e.job = "Guard"; e.hireDate = "2026-01-01";
        EmployeeService.add(e);
        SalaryService.add(1, "2026-09", "9000", "9000", "2026-09-30");
        CostService.add("Rice", "Food", "4200", "2026-09-12", "");
        CostService.add("Lamp", "Repair", "650", "2026-10-02", "");
        StudentService.exit(s1 + 5, "2026-10-10");

        FormDashboard dash = onEdt(FormDashboard::new);
        Thread.sleep(2500);
        shot(dash, "dashboard_dark.png");
        check("dashboard created", dash != null);

        StudentForm sf = onEdt(StudentForm::new);
        check("student table filled (7 rows)", sf.table.getRowCount() == 7);
        shot(sf, "students.png");
        RoomNamber rf = onEdt(RoomNamber::new);
        check("room table has 15 rows", rf.table.getRowCount() == 15);
        shot(rf, "rooms.png");
        PaymentForm pf = onEdt(PaymentForm::new);
        check("payment table filled", pf.table.getRowCount() >= 3);
        shot(pf, "payments.png");
        EmployeeForm ef = onEdt(EmployeeForm::new);
        check("employee table 1 row", ef.table.getRowCount() == 1);
        Salaryform sal = onEdt(Salaryform::new);
        check("salary table 1 row", sal.table.getRowCount() == 1);
        CostForm cf = onEdt(CostForm::new);
        check("cost table 2 rows", cf.table.getRowCount() == 2);
        userForm uf = onEdt(userForm::new);
        check("user table 1 row", uf.table.getRowCount() == 1);
        for (Maker<javax.swing.JPanel> m : new Maker[]{AddEmployee::new, AddSalary::new, AddCost::new, AddUser::new, FormAdd::new, Sitting::new}) {
            javax.swing.JPanel p = onEdt(m);
            check("screen opens: " + p.getClass().getSimpleName(), p != null);
        }
        shot(onEdt(AddEmployee::new), "add_employee.png");
        check("edit employee opens", onEdt(() -> new EditEmployee(1, ef)) != null);
        check("login form opens", onEdt(nematullah.application.form.LoginForm::new) != null);

        // search works
        SwingUtilities.invokeAndWait(() -> { sf.txtSearch.setText("Student3"); sf.reload(); });
        check("search finds 1 student", sf.table.getRowCount() == 1);

        // light mode render
        SwingUtilities.invokeAndWait(() -> { FlatMacLightLaf.setup(); FlatLaf.updateUI(); });
        FormDashboard dashLight = onEdt(FormDashboard::new);
        Thread.sleep(2500);
        shot(dashLight, "dashboard_light.png");

        // live refresh: while the dashboard is "on screen" (addNotify), any saved change must reload it by itself
        SwingUtilities.invokeAndWait(dashLight::addNotify);
        int before = dashLight.refreshCount;
        StudentService.add(st("5", "Live", "NIC-LIVE"));
        Thread.sleep(1500);
        check("dashboard reloads itself after a student is added", dashLight.refreshCount > before);
        before = dashLight.refreshCount;
        CostService.add("Live cost", "Other", "100", "2026-10-20", "");
        Thread.sleep(1500);
        check("dashboard reloads itself after a cost is added", dashLight.refreshCount > before);
        SwingUtilities.invokeAndWait(dashLight::removeNotify);
        before = dashLight.refreshCount;
        CostService.add("Other cost", "Other", "100", "2026-10-21", "");
        Thread.sleep(800);
        check("dashboard stops listening when it is not shown", dashLight.refreshCount == before);
        System.out.println("\nRESULT: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }

    static long number(String sql) throws Exception {
        try (java.sql.Connection c = Database.getConnection()) { return Database.number(c, sql); }
    }
}
