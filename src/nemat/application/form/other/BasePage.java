package nemat.application.form.other;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import nematullah.service.AppException;

/**
 * Common look of the list screens (Student, Rooms, Payment ...):
 * a title on top, a search box with buttons on the left, the table on the right.
 * Every screen only says which buttons it needs and how to load its rows.
 */
public abstract class BasePage extends JPanel {

    /** A piece of work that may fail with a message for the user. */
    protected interface Step {
        void run() throws AppException;
    }

    protected final JLabel lb = new JLabel();
    protected final JTextField txtSearch = new JTextField();
    protected final JTable table = new JTable();
    private final JPanel left = new JPanel();
    private final JLabel info = new JLabel(" ");

    protected BasePage(String title) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        lb.setText(title);
        lb.putClientProperty(FlatClientProperties.STYLE, "font:$h1.font");
        add(lb, BorderLayout.NORTH);

        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(170, 100));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search...");
        txtSearch.addActionListener(e -> reload());
        addToLeft(txtSearch);
        addButton("Search", this::reload);
        add(left, BorderLayout.WEST);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        add(new JScrollPane(table), BorderLayout.CENTER);

        info.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
        add(info, BorderLayout.SOUTH);
    }

    /** Loads the rows again (called at start, after every change and when Search is pressed). */
    protected abstract void reload();

    protected String searchText() {
        return txtSearch.getText().trim();
    }

    private void addToLeft(JTextField field) {
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 32));
        left.add(field);
        left.add(Box.createVerticalStrut(8));
    }

    protected JButton addButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        button.addActionListener(e -> action.run());
        left.add(button);
        left.add(Box.createVerticalStrut(8));
        return button;
    }

    /** Fills the table with new data (the cells cannot be edited by typing). */
    protected void showRows(String[] header, String[][] rows) {
        table.setModel(new DefaultTableModel(rows, header) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    protected void setInfo(String text) {
        info.setText(text);
    }

    /** Value of a column in the selected row, or null (with a message) when no row is selected. */
    protected String selected(int column) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return null;
        }
        return table.getValueAt(row, column).toString();
    }

    protected boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(this, question, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    protected void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    /** Runs the work; an AppException is shown to the user as a plain message. */
    protected boolean attempt(Step step) {
        try {
            step.run();
            return true;
        } catch (AppException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot do this", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    /** Shows a table in a small window (reports, balances ...). */
    protected void showTableDialog(String title, String[] header, String[][] rows) {
        JTable t = new JTable(new DefaultTableModel(rows, header) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        t.setAutoCreateRowSorter(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(720, 360));
        panel.add(new JScrollPane(t));
        JOptionPane.showMessageDialog(this, panel, title, JOptionPane.PLAIN_MESSAGE);
    }
}
