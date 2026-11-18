package nemat.application.form.other;

import nematullah.service.RoomService;

/** Rooms: capacity, current residents, free beds, rent and the automatic Available / Unavailable status. */
public class RoomNamber extends BasePage {

    private static final String[] HEADER = {"Room", "Capacity", "Current Residents", "Free Beds", "Monthly Rent (AFN)", "Discount", "Status"};

    public RoomNamber() {
        super("Room");
        addButton("Edit Rent", this::editRent);
        addButton("Residents", this::showResidents);
        reload();
    }

    @Override
    protected void reload() {
        attempt(() -> {
            String[][] rows = RoomService.list(searchText());
            showRows(HEADER, rows);
            int free = 0;
            for (String[] row : rows) {
                free += Integer.parseInt(row[3]);
            }
            setInfo(rows.length + " room(s), " + free + " free bed(s)");
        });
    }

    private void editRent() {
        String room = selected(0);
        if (room == null) {
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String rent = table.getModel().getValueAt(row, 4).toString();
        String discount = table.getModel().getValueAt(row, 5).toString();
        FormDialog.show(this, "Room " + room + " - Rent", new FormDialog.Field[]{
            new FormDialog.Field("Monthly rent (AFN)", rent), new FormDialog.Field("Discount (AFN)", discount)},
                v -> {
                    RoomService.updateRent(Integer.parseInt(room), v[0], v[1]);
                    reload();
                });
    }

    private void showResidents() {
        String room = selected(0);
        if (room != null) {
            attempt(() -> showTableDialog("Residents of room " + room,
                    new String[]{"Id", "Name", "Father Name", "Faculty", "Date Of Entry"},
                    RoomService.residentsOf(Integer.parseInt(room))));
        }
    }
}
