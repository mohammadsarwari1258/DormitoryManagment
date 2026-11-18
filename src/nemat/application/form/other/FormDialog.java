package nemat.application.form.other;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import nematullah.service.AppException;

/** A small "fill in these fields" window used by Edit, Exit, Transfer, Add Payment ... */
public final class FormDialog {

    /** One input: a text box, or a drop-down list when options are given, or a password box. */
    public static class Field {
        final String label;
        final String value;
        final String[] options;
        final boolean password;

        public Field(String label, String value) {
            this(label, value, false, (String[]) null);
        }

        public Field(String label, String value, String... options) {
            this(label, value, false, options);
        }

        private Field(String label, String value, boolean password, String[] options) {
            this.label = label;
            this.value = value == null ? "" : value;
            this.password = password;
            this.options = options;
        }

        public static Field password(String label) {
            return new Field(label, "", true, null);
        }
    }

    /** Saves the typed values. Throw AppException to keep the window open with a message. */
    public interface Saver {
        void save(String[] values) throws AppException;
    }

    private FormDialog() {
    }

    /** Shows the window until the values are saved or the user cancels. Returns true when saved. */
    public static boolean show(Component parent, String title, Field[] fields, Saver saver) {
        String[] current = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            current[i] = fields[i].value;
        }
        while (true) {
            JComponent[] inputs = new JComponent[fields.length];
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.fill = GridBagConstraints.HORIZONTAL;
            for (int i = 0; i < fields.length; i++) {
                c.gridx = 0;
                c.gridy = i;
                c.weightx = 0;
                panel.add(new JLabel(fields[i].label), c);
                inputs[i] = createInput(fields[i], current[i]);
                c.gridx = 1;
                c.weightx = 1;
                panel.add(inputs[i], c);
            }
            int answer = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return false;
            }
            for (int i = 0; i < fields.length; i++) {
                current[i] = read(inputs[i]);
            }
            try {
                saver.save(current.clone());
                return true;
            } catch (AppException e) {
                JOptionPane.showMessageDialog(parent, e.getMessage(), "Cannot save", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static JComponent createInput(Field f, String value) {
        if (f.options != null) {
            JComboBox<String> box = new JComboBox<>(f.options);
            box.setSelectedItem(value);
            return box;
        }
        JTextField text = f.password ? new JPasswordField(value, 18) : new JTextField(value, 18);
        return text;
    }

    private static String read(JComponent input) {
        if (input instanceof JComboBox) {
            Object item = ((JComboBox<?>) input).getSelectedItem();
            return item == null ? "" : item.toString();
        }
        if (input instanceof JPasswordField) {
            return new String(((JPasswordField) input).getPassword());
        }
        return ((JTextField) input).getText().trim();
    }
}
