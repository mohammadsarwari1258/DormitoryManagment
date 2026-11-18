package nemat.application.form.other;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import nematullah.service.AppException;
import nematullah.service.Check;
import nematullah.service.EmployeeService;
import nematullah.service.SalaryService;
import nematullah.service.StudentService;

/** "Add Salary" screen. The unpaid part is calculated automatically (salary - paid). */
public class AddSalary extends FormPage {

    private final JComboBox<String> employee;
    private final JTextField period = addField("Period (yyyy-MM)");
    private final JTextField amount = addField("Salary amount (AFN)");
    private final JTextField paid = addField("Paid amount (AFN, 0 if not paid yet)");
    private final JTextField date = addField("Payment date (yyyy-MM-dd)");

    public AddSalary() {
        super("Add Salary");
        String[] employees = new String[0];
        try {
            employees = EmployeeService.options();
        } catch (AppException e) {
            done(e.getMessage());
        }
        employee = addCombo("Employee", employees);
        period.setText(Check.today().substring(0, 7));
        date.setText(Check.today());
        addSaveButton("Save Salary", () -> {
            int id = StudentService.idOf(selected(employee));
            SalaryService.add(id, text(period), text(amount), text(paid), text(date));
            done("Salary added successfully.");
            amount.setText("");
            paid.setText("");
        });
    }
}
