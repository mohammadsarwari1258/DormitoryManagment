package nemat.application.form.other;

import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import nematullah.service.UserService;

/** "Add User" screen. The password is stored as a secure hash. */
public class AddUser extends FormPage {

    private final JTextField userName = addField("User name");
    private final JPasswordField password = addPassword("Password (at least 4 characters)");
    private final JComboBox<String> role = addCombo("Role", "Admin", "User");

    public AddUser() {
        super("Add User");
        addSaveButton("Save User", () -> {
            UserService.add(text(userName), new String(password.getPassword()), selected(role));
            done("User added successfully.");
            userName.setText("");
            password.setText("");
        });
    }
}
