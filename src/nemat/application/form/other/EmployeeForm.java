package nemat.application.form.other;

import javax.swing.JFrame;
import nematullah.service.EmployeeService;

/** Employee list with Update, Details, Activate / Deactivate and Delete. */
public class EmployeeForm extends BasePage {

    /** The id of the employee being edited (read by EditEmployee). */
    public static String Employee_id;

    public EmployeeForm() {
        super("Employee Form");
        addButton("Update", this::updateSelected);
        addButton("Details", this::showDetails);
        addButton("Activate / Deactivate", this::toggleStatus);
        addButton("Delete", this::deleteSelected);
        reload();
    }

    public void loadData() {
        reload();
    }

    @Override
    protected void reload() {
        attempt(() -> {
            String[][] rows = EmployeeService.list(searchText());
            showRows(EmployeeService.HEADER, rows);
            setInfo(rows.length + " employee(s)");
        });
    }

    private void updateSelected() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        Employee_id = id;
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true);
        frame.add(new EditEmployee(Integer.parseInt(id), this));
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    private void showDetails() {
        String id = selected(0);
        if (id != null) {
            attempt(() -> message(EmployeeService.details(Integer.parseInt(id))));
        }
    }

    private void toggleStatus() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        boolean active = "Active".equals(table.getModel().getValueAt(row, 8).toString());
        if (attempt(() -> EmployeeService.setActive(Integer.parseInt(id), !active))) {
            reload();
        }
    }

    private void deleteSelected() {
        String id = selected(0);
        if (id != null && confirm("Do you want to delete this employee?")) {
            if (attempt(() -> EmployeeService.delete(Integer.parseInt(id)))) {
                reload();
            }
        }
    }
}
