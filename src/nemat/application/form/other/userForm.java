package nemat.application.form.other;

import nematullah.service.UserService;

/** System users: edit (name, password, role, active) and delete. */
public class userForm extends BasePage {

    public userForm() {
        super("Users");
        addButton("Update", this::editUser);
        addButton("Delete", this::deleteUser);
        reload();
    }

    @Override
    protected void reload() {
        attempt(() -> {
            String[][] rows = UserService.list(searchText());
            showRows(UserService.HEADER, rows);
            setInfo(rows.length + " user(s)");
        });
    }

    private void editUser() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        FormDialog.show(this, "Edit User", new FormDialog.Field[]{
            new FormDialog.Field("User name", table.getModel().getValueAt(row, 1).toString()),
            FormDialog.Field.password("New password (empty = keep)"),
            new FormDialog.Field("Role", table.getModel().getValueAt(row, 2).toString(), "Admin", "User"),
            new FormDialog.Field("Status", table.getModel().getValueAt(row, 3).toString(), "Active", "Inactive")},
                v -> {
                    UserService.update(Integer.parseInt(id), v[0], v[1], v[2], "Active".equals(v[3]));
                    reload();
                });
    }

    private void deleteUser() {
        String id = selected(0);
        if (id != null && confirm("Do you want to delete this user?")) {
            if (attempt(() -> UserService.delete(Integer.parseInt(id)))) {
                reload();
            }
        }
    }
}
