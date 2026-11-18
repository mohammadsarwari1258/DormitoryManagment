package nemat.application.form.other;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import nematullah.application.form.PanelLogin;
import nematullah.service.AppException;
import nematullah.service.EmployeeService;
import nematullah.service.I18n;

/**
 * "Edit Employee" window. It has the same design as "Edit Student" (two columns in a rounded card)
 * and works in English or Persian: the language box on the form switches all texts at once.
 */
public class EditEmployee extends JPanel {

    private static final String[] JOBS = {"Manager", "Accountant", "Cook", "Guard", "Cleaner", "Other"};

    private final int employeeId;
    private final EmployeeForm parent;

    private final JLabel lb = new JLabel();
    private final PanelLogin card = new PanelLogin();
    private final JLabel lbFirst = new JLabel(), lbLast = new JLabel(), lbFather = new JLabel(), lbPhone = new JLabel();
    private final JLabel lbJob = new JLabel(), lbResidence = new JLabel(), lbHire = new JLabel(), lbLanguage = new JLabel();
    private final JTextField fname = new JTextField(), lname = new JTextField(), fatherName = new JTextField();
    private final JTextField phone = new JTextField(), residence = new JTextField(), hireDate = new JTextField();
    private final JComboBox<String> job = new JComboBox<>();
    private final JComboBox<String> languageBox = new JComboBox<>(new String[]{"English", "فارسی"});
    private final JButton save = new JButton(), cancel = new JButton();
    /** The job exactly as stored in the database (English), parallel to the items of the job box. */
    private final java.util.List<String> jobValues = new java.util.ArrayList<>();
    private boolean loading = true;

    public EditEmployee(int employeeId, EmployeeForm parent) {
        this.employeeId = employeeId;
        this.parent = parent;
        buildLayout();
        loadEmployee();
        languageBox.setSelectedIndex(I18n.isPersian() ? 1 : 0);
        applyLanguage();
        loading = false;

        languageBox.addActionListener(e -> {
            if (!loading) {
                I18n.setLanguage(languageBox.getSelectedIndex() == 1 ? I18n.PERSIAN : I18n.ENGLISH);
                applyLanguage();
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.pack();
                }
            }
        });
        save.addActionListener(e -> saveChanges());
        cancel.addActionListener(e -> close());
    }

    /** Same card layout as EditForm: left column and right column, 72 px apart. */
    private void buildLayout() {
        card.setBorder(new javax.swing.border.MatteBorder(null));
        JComponent[] left = {lbFirst, fname, lbLast, lname, lbFather, fatherName, lbPhone, phone, new JLabel(" "), cancel};
        JComponent[] right = {lbJob, job, lbResidence, residence, lbHire, hireDate, lbLanguage, languageBox, new JLabel(" "), save};

        GroupLayout layout = new GroupLayout(card);
        card.setLayout(layout);
        GroupLayout.ParallelGroup leftColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
        GroupLayout.ParallelGroup rightColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
        for (int i = 0; i < left.length; i++) {
            leftColumn.addComponent(left[i], GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE);
            rightColumn.addComponent(right[i], GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE);
        }
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(32).addGroup(leftColumn).addGap(72).addGroup(rightColumn).addContainerGap(32, Short.MAX_VALUE));
        GroupLayout.SequentialGroup rows = layout.createSequentialGroup().addGap(32);
        for (int i = 0; i < left.length; i++) {
            rows.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(left[i]).addComponent(right[i]));
            rows.addPreferredGap(i % 2 == 0 ? LayoutStyle.ComponentPlacement.RELATED : LayoutStyle.ComponentPlacement.UNRELATED);
        }
        layout.setVerticalGroup(rows.addContainerGap(32, Short.MAX_VALUE));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.add(card);
        JScrollPane scroll = new JScrollPane(centerPanel);

        lb.setHorizontalAlignment(SwingConstants.CENTER);
        GroupLayout main = new GroupLayout(this);
        setLayout(main);
        main.setHorizontalGroup(main.createParallelGroup()
                .addGroup(main.createSequentialGroup().addContainerGap().addComponent(lb, 0, 586, Short.MAX_VALUE).addContainerGap())
                .addComponent(scroll));
        main.setVerticalGroup(main.createSequentialGroup().addContainerGap()
                .addComponent(lb, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(scroll).addContainerGap());
    }

    private void loadEmployee() {
        try {
            String[] e = EmployeeService.find(employeeId);
            if (e != null) {
                fname.setText(e[0]);
                lname.setText(e[1]);
                fatherName.setText(e[2]);
                phone.setText(e[3]);
                residence.setText(e[5]);
                hireDate.setText(e[6]);
                for (String j : JOBS) {
                    jobValues.add(j);
                }
                if (!jobValues.contains(e[4])) {
                    jobValues.add(e[4]);   // a job typed earlier that is not in the list
                }
                fillJobBox();
                job.setSelectedIndex(jobValues.indexOf(e[4]));
            }
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void fillJobBox() {
        int selected = job.getSelectedIndex();
        job.removeAllItems();
        for (String value : jobValues) {
            job.addItem(I18n.job(value));
        }
        job.setSelectedIndex(Math.max(selected, 0));
    }

    /** Sets every text, the writing direction and a font that can show Persian letters. */
    private void applyLanguage() {
        boolean fa = I18n.isPersian();
        lb.setText(I18n.t("edit_employee"));
        lbFirst.setText(I18n.t("first_name"));
        lbLast.setText(I18n.t("last_name"));
        lbFather.setText(I18n.t("father_name"));
        lbPhone.setText(I18n.t("phone"));
        lbJob.setText(I18n.t("job"));
        lbResidence.setText(I18n.t("residence"));
        lbHire.setText(I18n.t("hire_date"));
        lbLanguage.setText(I18n.t("language"));
        save.setText(I18n.t("save_changes"));
        cancel.setText(I18n.t("cancel"));
        fillJobBox();

        // "Dialog" is a Java logical font: it falls back to a system font that has Persian letters.
        Font persian = new Font(Font.DIALOG, Font.PLAIN, 14);
        for (JComponent c : new JComponent[]{lbFirst, lbLast, lbFather, lbPhone, lbJob, lbResidence, lbHire, lbLanguage,
            fname, lname, fatherName, phone, residence, hireDate, job, languageBox, save, cancel}) {
            c.setFont(fa ? persian : null);
        }
        if (fa) {
            lb.putClientProperty(FlatClientProperties.STYLE, null);
            lb.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        } else {
            lb.setFont(null);
            lb.putClientProperty(FlatClientProperties.STYLE, "font:$h1.font");
        }
        applyComponentOrientation(fa ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        revalidate();
        repaint();
    }

    private void saveChanges() {
        EmployeeService.Employee e = new EmployeeService.Employee();
        e.firstName = fname.getText();
        e.lastName = lname.getText();
        e.fatherName = fatherName.getText();
        e.phone = phone.getText();
        int index = job.getSelectedIndex();
        e.job = index >= 0 && index < jobValues.size() ? jobValues.get(index) : "";
        e.residence = residence.getText();
        e.hireDate = hireDate.getText();
        try {
            EmployeeService.update(employeeId, e);
            JOptionPane.showMessageDialog(this, I18n.t("saved"));
            close();
            parent.loadData();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), I18n.t("cannot_save"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void close() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }
}
