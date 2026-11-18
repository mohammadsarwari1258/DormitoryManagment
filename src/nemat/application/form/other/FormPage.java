package nemat.application.form.other;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import nematullah.service.AppException;

/**
 * Common look of the "Add ..." screens: a title and a rounded card with labelled fields and a Save button
 * (the same card style as the login screen).
 */
public abstract class FormPage extends JPanel {

    protected interface Action {
        void run() throws AppException;
    }

    protected final JLabel lb = new JLabel();
    private final JPanel card = new JPanel(new MigLayout("fillx,wrap,insets 30 40 40 40,width 380", "[fill]"));
    private final List<JComponent> inputs = new ArrayList<>();

    protected FormPage(String title) {
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        lb.setText(title);
        lb.putClientProperty(FlatClientProperties.STYLE, "font:$h1.font");
        add(lb, BorderLayout.NORTH);

        card.putClientProperty(FlatClientProperties.STYLE, "background:$Login.background;arc:20");
        JPanel holder = new JPanel(new MigLayout("insets 20", "[grow,center]"));
        holder.setOpaque(false);
        holder.add(card);
        JScrollPane scroll = new JScrollPane(holder);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    protected JTextField addField(String label) {
        JTextField field = new JTextField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, label);
        card.add(new JLabel(label), "gapy 8 0");
        card.add(field);
        inputs.add(field);
        return field;
    }

    protected JPasswordField addPassword(String label) {
        JPasswordField field = new JPasswordField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, label);
        card.add(new JLabel(label), "gapy 8 0");
        card.add(field);
        inputs.add(field);
        return field;
    }

    protected JComboBox<String> addCombo(String label, String... options) {
        JComboBox<String> box = new JComboBox<>(options);
        card.add(new JLabel(label), "gapy 8 0");
        card.add(box);
        inputs.add(box);
        return box;
    }

    protected void addSaveButton(String text, Action action) {
        JButton button = new JButton(text);
        button.putClientProperty(FlatClientProperties.STYLE, "borderWidth:0;focusWidth:0;innerFocusWidth:0");
        button.addActionListener(e -> {
            try {
                action.run();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot save", JOptionPane.WARNING_MESSAGE);
            }
        });
        card.add(button, "gapy 18 0");
    }

    /** Empties every text box (combos go back to the first item). */
    protected void clearFields() {
        for (JComponent input : inputs) {
            if (input instanceof JTextField) {
                ((JTextField) input).setText("");
            } else if (input instanceof JComboBox) {
                ((JComboBox<?>) input).setSelectedIndex(0);
            }
        }
    }

    protected static String text(JTextField field) {
        return field.getText().trim();
    }

    protected static String selected(JComboBox<String> box) {
        Object item = box.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    protected void done(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
