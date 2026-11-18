package nemat.application.form.other;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class Sitting extends javax.swing.JPanel {

    public Sitting() {
        initComponents();
        lb.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:$h1.font");
createSettings();
    }
private void createSettings() {
    removeAll();

    setLayout(new BorderLayout());
    setOpaque(false);

    // Header
    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
    header.setOpaque(false);
    header.setBorder(new EmptyBorder(25, 30, 10, 30));

    JLabel title = new JLabel("Settings");

    title.putClientProperty(
        FlatClientProperties.STYLE,
        "font:$h1.font"
    );

    JLabel subtitle = new JLabel(
        "Manage your account and system preferences"
    );

    subtitle.putClientProperty(
        FlatClientProperties.STYLE,
        "foreground:$Label.disabledForeground"
    );

    header.add(title);
    header.add(Box.createVerticalStrut(5));
    header.add(subtitle);

    add(header, BorderLayout.NORTH);

    // Cards
    JPanel card = new JPanel(
        new GridLayout(4, 2, 18, 18)
    );

    card.setOpaque(false);
    card.setBorder(
        new EmptyBorder(20, 30, 30, 30)
    );

    card.add(createCard(
        "My Profile",
        "Manage your personal account",
        "Profile"
    ));

    card.add(createCard(
        "Security",
        "Password and account security",
        "Security"
    ));

    card.add(createCard(
        "System Information",
        "system information",
        "System"
    ));

    card.add(createCard(
        "Notifications",
        "Manage system notifications",
        "Notifications"
    ));

    card.add(createCard(
        "Database",
        "Backup and restore database",
        "Database"
    ));

    card.add(createCard(
        "Language",
        "Choose system language",
        "Language"
    ));

    card.add(createCard(
        "Preferences",
        "General system preferences",
        "Preferences"
    ));

    card.add(createCard(
        "About",
        "About Abdolhashem Management System",
        "About"
    ));

    add(card, BorderLayout.CENTER);

    revalidate();
    repaint();
}
private JPanel createCard(
        String title,
        String description,
        String type) {

    JPanel card = new JPanel(
        new BorderLayout(15, 0)
    );

    // "borderWidth"/"borderColor" are not valid style keys for a JPanel; "border" is.
    card.putClientProperty(
        FlatClientProperties.STYLE,
        "arc:18;"
        + "border:20,20,20,20,$Component.borderColor,1,18;"
        + "background:$Panel.background"
    );

    JPanel text = new JPanel();

    text.setLayout(
        new BoxLayout(text, BoxLayout.Y_AXIS)
    );

    text.setOpaque(false);

    JLabel titleLabel = new JLabel(title);

    titleLabel.putClientProperty(
        FlatClientProperties.STYLE,
        "font:bold +2"
    );

    JLabel descriptionLabel =
        new JLabel(description);

    descriptionLabel.putClientProperty(
        FlatClientProperties.STYLE,
        "foreground:$Label.disabledForeground"
    );

    text.add(titleLabel);
    text.add(Box.createVerticalStrut(7));
    text.add(descriptionLabel);

    JButton button = new JButton("Open");

    button.setCursor(
        new Cursor(Cursor.HAND_CURSOR)
    );

    button.addActionListener(e ->
        openSetting(type)
    );

    card.add(text, BorderLayout.CENTER);
    card.add(button, BorderLayout.EAST);

    return card;
}
private void openSetting(String type) {

    switch (type) {

        case "Profile":
            JOptionPane.showMessageDialog(
                this,
                "This option extended in future",
                "My Profile",
                JOptionPane.INFORMATION_MESSAGE
            );
            break;

        case "Security":
            changePassword();
            break;

        case "System":
            showSystemInfo();
            break;

        case "Notifications":
            JOptionPane.showMessageDialog(
                this,
                "Payment Notifications\n"
                + "Salary Notifications\n"
                + "Cost Notifications\n"
                + "System Notifications",
                "Notifications",
                JOptionPane.INFORMATION_MESSAGE
            );
            break;

        case "Database":
            databaseOptions();
            break;

        case "Language":
            FormDialog.show(this, "Language / زبان", new FormDialog.Field[]{
                new FormDialog.Field("Language / زبان", nematullah.service.I18n.isPersian() ? "فارسی" : "English", "English", "فارسی")},
                    v -> {
                        nematullah.service.I18n.setLanguage("فارسی".equals(v[0]) ? nematullah.service.I18n.PERSIAN : nematullah.service.I18n.ENGLISH);
                        JOptionPane.showMessageDialog(this, "Saved. The Edit Employee page now uses this language.\nذخیره شد.");
                    });
            break;

        case "Preferences":
            preferences();
            break;

        case "About":

            JOptionPane.showMessageDialog(
                this,
                "Abdolhashem Management System\n\n"
                + "Version 1\n\n"
                + "Developed by nematullah.dev\n\n"
                +"Mohammad Sarwari, Mohammad Hossain Mohammadi\n\n"
                 +"Amin Mohammadi And Nor Ahmad Naderi\n\n",
                "About",
                JOptionPane.INFORMATION_MESSAGE
            );
            break;
    }
}

    // ---------------------------------------------------------------- real settings actions

    /** Security: the logged-in user changes his own password (stored as a hash). */
    private void changePassword() {
        if (nematullah.service.Session.getUserName() == null) {
            JOptionPane.showMessageDialog(this, "Nobody is logged in.");
            return;
        }
        FormDialog.show(this, "Change Password", new FormDialog.Field[]{
            FormDialog.Field.password("Current password"), FormDialog.Field.password("New password"),
            FormDialog.Field.password("Repeat new password")},
                v -> {
                    if (!v[1].equals(v[2])) {
                        throw new nematullah.service.AppException("The two new passwords are not the same.");
                    }
                    if (!nematullah.service.UserService.login(nematullah.service.Session.getUserName(), v[0])) {
                        throw new nematullah.service.AppException("The current password is wrong.");
                    }
                    nematullah.service.UserService.changeOwnPassword(nematullah.service.Session.getUserName(), v[1]);
                    JOptionPane.showMessageDialog(this, "Password changed.");
                });
    }

    private void showSystemInfo() {
        try {
            String[][] counts = nematullah.database.Database.query(
                    "SELECT (SELECT COUNT(*) FROM students), (SELECT COUNT(*) FROM employee), (SELECT COUNT(*) FROM room)");
            JOptionPane.showMessageDialog(this,
                    "Abdolhashem Management System\n\nLogged in as: " + nematullah.service.Session.getUserName()
                    + "\nDatabase file: " + nematullah.database.Database.getDatabasePath()
                    + "\nStudents: " + counts[0][0] + "   Employees: " + counts[0][1] + "   Rooms: " + counts[0][2],
                    "System Information", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(this, "Cannot read the database: " + e.getMessage());
        }
    }

    private void databaseOptions() {
        Object[] options = {"Test Connection", "Backup Database", "Close"};
        int choice = JOptionPane.showOptionDialog(this, "Database file:\n" + nematullah.database.Database.getDatabasePath(),
                "Database", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        try {
            if (choice == 0) {
                nematullah.database.Database.query("SELECT 1");
                JOptionPane.showMessageDialog(this, "Connection to SQLite works.");
            } else if (choice == 1) {
                String file = nematullah.database.Database.backup();
                JOptionPane.showMessageDialog(this, "Backup saved:\n" + file);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    /** Preferences: Light / Dark mode is saved in the database and used at the next start too. */
    private void preferences() {
        FormDialog.show(this, "Preferences", new FormDialog.Field[]{
            new FormDialog.Field("Theme", com.formdev.flatlaf.FlatLaf.isLafDark() ? "Dark" : "Light", "Light", "Dark")},
                v -> {
                    boolean dark = "Dark".equals(v[0]);
                    nematullah.database.Database.setSetting("theme", dark ? "dark" : "light");
                    if (com.formdev.flatlaf.FlatLaf.isLafDark() != dark) {
                        java.awt.EventQueue.invokeLater(() -> {
                            if (dark) {
                                com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();
                            } else {
                                com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
                            }
                            com.formdev.flatlaf.FlatLaf.updateUI();
                        });
                    }
                });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lb = new javax.swing.JLabel();

        lb.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lb.setText("Sitting");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lb, javax.swing.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lb, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(410, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lb;
    // End of variables declaration//GEN-END:variables
}
