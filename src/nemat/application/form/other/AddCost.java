package nemat.application.form.other;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import nematullah.service.Check;
import nematullah.service.CostService;

/** "Add Cost" screen. */
public class AddCost extends FormPage {

    private final JTextField title = addField("Title");
    private final JComboBox<String> category = addCombo("Category", CostService.CATEGORIES);
    private final JTextField price = addField("Price (AFN)");
    private final JTextField date = addField("Date (yyyy-MM-dd)");
    private final JTextField description = addField("Description (optional)");

    public AddCost() {
        super("Add Cost");
        date.setText(Check.today());
        addSaveButton("Save Cost", () -> {
            CostService.add(text(title), selected(category), text(price), text(date), text(description));
            done("Cost added successfully.");
            clearFields();
            date.setText(Check.today());
        });
    }
}
