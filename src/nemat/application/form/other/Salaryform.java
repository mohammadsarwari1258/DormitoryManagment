package nemat.application.form.other;

import nematullah.service.AppException;
import nematullah.service.Check;
import nematullah.service.EmployeeService;
import nematullah.service.SalaryService;
import nematullah.service.StudentService;

/** Salary history with Add, Edit, Delete and the history of one employee. */
public class Salaryform extends BasePage {

    private int employeeFilter = 0;

    public Salaryform() {
        super("Salary Form");
        addButton("Add Salary", this::addSalary);
        addButton("Edit", this::editSalary);
        addButton("Delete", this::deleteSalary);
        addButton("Employee History", this::employeeHistory);
        addButton("Show All", () -> {
            employeeFilter = 0;
            reload();
        });
        reload();
    }

    @Override
    protected void reload() {
        attempt(() -> {
            String[][] rows = SalaryService.list(searchText(), employeeFilter);
            showRows(SalaryService.HEADER, rows);
            long salary = 0, paid = 0, unpaid = 0;
            for (String[] r : rows) {
                salary += Long.parseLong(r[4]);
                paid += Long.parseLong(r[5]);
                unpaid += Long.parseLong(r[6]);
            }
            setInfo(rows.length + " record(s)   |   Salary: " + salary + "   Paid: " + paid + "   Unpaid: " + unpaid + " AFN");
        });
    }

    private void addSalary() {
        attempt(() -> {
            String[] employees = EmployeeService.options();
            if (employees.length == 0) {
                throw new AppException("There is no active employee.");
            }
            FormDialog.show(this, "Add Salary", new FormDialog.Field[]{
                new FormDialog.Field("Employee", employees[0], employees),
                new FormDialog.Field("Period (yyyy-MM)", Check.today().substring(0, 7)),
                new FormDialog.Field("Salary amount", ""), new FormDialog.Field("Paid amount", ""),
                new FormDialog.Field("Payment date (yyyy-MM-dd)", Check.today())},
                    v -> {
                        SalaryService.add(StudentService.idOf(v[0]), v[1], v[2], v[3], v[4]);
                        reload();
                    });
        });
    }

    private void editSalary() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String date = table.getModel().getValueAt(row, 7).toString();
        FormDialog.show(this, "Edit Salary", new FormDialog.Field[]{
            new FormDialog.Field("Period (yyyy-MM)", table.getModel().getValueAt(row, 3).toString()),
            new FormDialog.Field("Salary amount", table.getModel().getValueAt(row, 4).toString()),
            new FormDialog.Field("Paid amount", table.getModel().getValueAt(row, 5).toString()),
            new FormDialog.Field("Payment date (yyyy-MM-dd)", date.isEmpty() ? Check.today() : date)},
                v -> {
                    SalaryService.update(Integer.parseInt(id), v[0], v[1], v[2], v[3]);
                    reload();
                });
    }

    private void deleteSalary() {
        String id = selected(0);
        if (id != null && confirm("Delete this salary record? The deletion is written to the activity log.")) {
            if (attempt(() -> SalaryService.delete(Integer.parseInt(id)))) {
                reload();
            }
        }
    }

    private void employeeHistory() {
        String employeeId = selected(1);
        if (employeeId != null) {
            employeeFilter = Integer.parseInt(employeeId);
            reload();
        }
    }
}
