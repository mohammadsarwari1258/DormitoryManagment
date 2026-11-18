package nemat.application.form.other;

import nematullah.service.AppException;
import nematullah.service.Check;
import nematullah.service.PaymentService;
import nematullah.service.StudentService;

/** Rent payments: add, edit, delete, history of one student, debts and monthly dues. */
public class PaymentForm extends BasePage {

    /** 0 = show every student, otherwise only the payments of this student. */
    private int studentFilter = 0;

    public PaymentForm() {
        super("Payment");
        addButton("Add Payment", this::addPayment);
        addButton("Edit", this::editPayment);
        addButton("Delete", this::deletePayment);
        addButton("Student History", this::studentHistory);
        addButton("Show All", () -> {
            studentFilter = 0;
            reload();
        });
        addButton("Student Debts", this::showBalances);
        addButton("Generate Dues", this::generateDues);
        reload();
    }

    @Override
    protected void reload() {
        attempt(() -> {
            String[][] rows = PaymentService.list(searchText(), studentFilter);
            showRows(PaymentService.HEADER, rows);
            long due = 0, paid = 0, remain = 0;
            for (String[] r : rows) {
                due += Long.parseLong(r[5]);
                paid += Long.parseLong(r[6]);
                remain += Long.parseLong(r[7]);
            }
            setInfo(rows.length + " record(s)   |   Due: " + due + "   Paid: " + paid + "   Remaining: " + remain + " AFN"
                    + (studentFilter != 0 ? "   (one student)" : ""));
        });
    }

    private void addPayment() {
        attempt(() -> {
            String[] students = StudentService.options(false);
            if (students.length == 0) {
                throw new AppException("There is no student yet.");
            }
            FormDialog.show(this, "Add Payment", new FormDialog.Field[]{
                new FormDialog.Field("Student", students[0], students),
                new FormDialog.Field("Period (yyyy-MM)", Check.today().substring(0, 7)),
                new FormDialog.Field("Amount due (empty = room rent)", ""),
                new FormDialog.Field("Paid amount", ""),
                new FormDialog.Field("Payment date (yyyy-MM-dd)", Check.today())},
                    v -> {
                        int id = StudentService.idOf(v[0]);
                        String due = v[2].isEmpty() ? String.valueOf(PaymentService.defaultDue(id)) : v[2];
                        PaymentService.add(id, v[1], due, v[3], v[4]);
                        reload();
                    });
        });
    }

    private void editPayment() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String period = table.getModel().getValueAt(row, 4).toString();
        String due = table.getModel().getValueAt(row, 5).toString();
        String paid = table.getModel().getValueAt(row, 6).toString();
        String date = table.getModel().getValueAt(row, 9).toString();
        FormDialog.show(this, "Edit Payment", new FormDialog.Field[]{
            new FormDialog.Field("Period (yyyy-MM)", period), new FormDialog.Field("Amount due", due),
            new FormDialog.Field("Paid amount", paid),
            new FormDialog.Field("Payment date (yyyy-MM-dd)", date.isEmpty() ? Check.today() : date)},
                v -> {
                    PaymentService.update(Integer.parseInt(id), v[0], v[1], v[2], v[3]);
                    reload();
                });
    }

    private void deletePayment() {
        String id = selected(0);
        if (id != null && confirm("Delete this payment record? The deletion is written to the activity log.")) {
            if (attempt(() -> PaymentService.delete(Integer.parseInt(id)))) {
                reload();
            }
        }
    }

    private void studentHistory() {
        String studentId = selected(1);
        if (studentId != null) {
            studentFilter = Integer.parseInt(studentId);
            reload();
        }
    }

    private void showBalances() {
        attempt(() -> showTableDialog("Student debts and payment status", PaymentService.BALANCE_HEADER, PaymentService.balances()));
    }

    private void generateDues() {
        FormDialog.show(this, "Generate Dues", new FormDialog.Field[]{
            new FormDialog.Field("Period (yyyy-MM)", Check.today().substring(0, 7))},
                v -> {
                    int created = PaymentService.generateDues(v[0]);
                    message(created + " new unpaid record(s) created for active students.");
                    reload();
                });
    }
}
