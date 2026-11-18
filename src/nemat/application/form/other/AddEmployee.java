package nemat.application.form.other;

import java.awt.Window;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import nematullah.service.Check;
import nematullah.service.EmployeeService;

/** "Add Employee" screen. */
public class AddEmployee extends FormPage {

    public static final String[] JOBS = {"Manager", "Accountant", "Cook", "Guard", "Cleaner", "Other"};

    private final JTextField fname = addField("First name");
    private final JTextField lname = addField("Last name");
    private final JTextField fatherName = addField("Father name");
    private final JTextField phone = addField("Phone number");
    private final JComboBox<String> job = addCombo("Job", JOBS);
    private final JTextField residence = addField("Permanent residence");
    private final JTextField hireDate = addField("Hire date (yyyy-MM-dd)");

    public AddEmployee() {
        super("Add Employee");
        hireDate.setText(Check.today());
        addSaveButton("Save Employee", () -> {
            EmployeeService.Employee e = new EmployeeService.Employee();
            e.firstName = text(fname);
            e.lastName = text(lname);
            e.fatherName = text(fatherName);
            e.phone = text(phone);
            e.job = selected(job);
            e.residence = text(residence);
            e.hireDate = text(hireDate);
            EmployeeService.add(e);
            done("Employee added successfully.");
            clearFields();
            hireDate.setText(Check.today());
        });
    }
}
