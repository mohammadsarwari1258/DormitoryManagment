package nemat.application.form.other;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import nematullah.service.AppException;
import nematullah.service.Check;
import nematullah.service.RoomService;
import nematullah.service.StudentService;

/** Student list with Update, Exit, Transfer, Details and Delete. */
public class StudentForm extends BasePage {

    /** The id of the student being edited (read by EditForm). */
    public static String student_id;

    public StudentForm() {
        super("Student Information");
        addButton("Update", this::updateSelected);
        addButton("Exit", this::exitSelected);
        addButton("Transfer Room", this::transferSelected);
        addButton("Details", this::showDetails);
        addButton("Delete", this::deleteSelected);
        reload();
    }

    /** Kept with the old name because EditForm calls it after saving. */
    public void loadData() {
        reload();
    }

    @Override
    protected void reload() {
        attempt(() -> {
            String[][] rows = StudentService.list(searchText());
            showRows(StudentService.HEADER, rows);
            setInfo(rows.length + " student(s)");
        });
    }

    private void updateSelected() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        student_id = id;
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        EditForm panel = new EditForm(value(row, 1), value(row, 2), value(row, 3), value(row, 4), value(row, 5),
                value(row, 6), value(row, 7), value(row, 8), value(row, 9), this);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    private String value(int modelRow, int column) {
        Object v = table.getModel().getValueAt(modelRow, column);
        return v == null ? "" : v.toString();
    }

    private void exitSelected() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        FormDialog.show(this, "Student Exit", new FormDialog.Field[]{
            new FormDialog.Field("Date of exit (yyyy-MM-dd)", Check.today())},
                v -> {
                    StudentService.exit(Integer.parseInt(id), v[0]);
                    reload();
                });
    }

    private void transferSelected() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        attempt(() -> {
            List<Integer> rooms = RoomService.roomsWithFreeBeds();
            List<String> items = new ArrayList<>();
            for (Integer room : rooms) {
                items.add(String.valueOf(room));
            }
            if (items.isEmpty()) {
                throw new AppException("No room has a free bed.");
            }
            FormDialog.show(this, "Transfer To Another Room", new FormDialog.Field[]{
                new FormDialog.Field("New room", items.get(0), items.toArray(new String[0]))},
                    v -> {
                        StudentService.transfer(Integer.parseInt(id), v[0]);
                        reload();
                    });
        });
    }

    private void showDetails() {
        String id = selected(0);
        if (id != null) {
            attempt(() -> message(StudentService.details(Integer.parseInt(id))));
        }
    }

    private void deleteSelected() {
        String id = selected(0);
        if (id != null && confirm("Do you want to delete this student?")) {
            if (attempt(() -> StudentService.delete(Integer.parseInt(id)))) {
                message("The student was removed.");
                reload();
            }
        }
    }
}
