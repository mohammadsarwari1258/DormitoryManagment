package nemat.application.form.other;

import nematullah.service.CostService;

/** Costs with search, date range report, Edit and Delete. */
public class CostForm extends BasePage {

    private String from = "";
    private String to = "";

    public CostForm() {
        super("Cost Form");
        addButton("Edit", this::editCost);
        addButton("Delete", this::deleteCost);
        addButton("Date Range", this::chooseRange);
        addButton("Report By Category", this::showReport);
        addButton("Show All", () -> {
            from = "";
            to = "";
            reload();
        });
        reload();
    }

    @Override
    protected void reload() {
        attempt(() -> {
            String[][] rows = CostService.list(searchText(), from, to);
            showRows(CostService.HEADER, rows);
            long total = 0;
            for (String[] r : rows) {
                total += Long.parseLong(r[3]);
            }
            String range = from.isEmpty() && to.isEmpty() ? "all dates" : (from.isEmpty() ? "..." : from) + " to " + (to.isEmpty() ? "..." : to);
            setInfo(rows.length + " cost(s)   |   Total: " + total + " AFN   |   " + range);
        });
    }

    private void chooseRange() {
        FormDialog.show(this, "Date Range", new FormDialog.Field[]{
            new FormDialog.Field("From (yyyy-MM-dd, empty = no limit)", from),
            new FormDialog.Field("To (yyyy-MM-dd, empty = no limit)", to)},
                v -> {
                    CostService.list("", v[0], v[1]);   // only to check the dates
                    from = v[0];
                    to = v[1];
                    reload();
                });
    }

    private void showReport() {
        attempt(() -> showTableDialog("Costs by category", new String[]{"Category", "Count", "Total (AFN)"},
                CostService.reportByCategory(from, to)));
    }

    private void editCost() {
        String id = selected(0);
        if (id == null) {
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        FormDialog.show(this, "Edit Cost", new FormDialog.Field[]{
            new FormDialog.Field("Title", table.getModel().getValueAt(row, 1).toString()),
            new FormDialog.Field("Category", table.getModel().getValueAt(row, 2).toString(), CostService.CATEGORIES),
            new FormDialog.Field("Price", table.getModel().getValueAt(row, 3).toString()),
            new FormDialog.Field("Date (yyyy-MM-dd)", table.getModel().getValueAt(row, 4).toString()),
            new FormDialog.Field("Description", table.getModel().getValueAt(row, 5).toString())},
                v -> {
                    CostService.update(Integer.parseInt(id), v[0], v[1], v[2], v[3], v[4]);
                    reload();
                });
    }

    private void deleteCost() {
        String id = selected(0);
        if (id != null && confirm("Delete this cost? The deletion is written to the activity log.")) {
            if (attempt(() -> CostService.delete(Integer.parseInt(id)))) {
                reload();
            }
        }
    }
}
