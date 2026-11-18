package nemat.application.form.other;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import nemat.menu.mode.ModelCard;
import nematullah.service.AppException;
import nematullah.service.DashboardService;
import nematullah.service.DashboardService.Stats;
import nematullah.service.DataEvents;

/**
 * Dashboard. Every number comes from the SQLite database (DashboardService).
 * The data is loaded in the background (SwingWorker) so the window never freezes, and it refreshes
 * itself whenever any screen saves a change (see DataEvents).
 */
public class FormDashboard extends JPanel {

    private static final Color BLUE = new Color(37, 99, 235), CYAN = new Color(34, 211, 238);
    private static final Color PURPLE = new Color(124, 58, 237), PINK = new Color(236, 72, 153);
    private static final Color GREEN = new Color(22, 163, 74), LIME = new Color(132, 204, 22);
    private static final Color ORANGE = new Color(234, 88, 12), YELLOW = new Color(251, 191, 36);
    private static final Color RED = new Color(220, 38, 38), ROSE = new Color(251, 113, 133);

    private final Runnable refreshListener = this::refresh;
    private final DecimalFormat money = new DecimalFormat("#,##0");

    // Students
    private final card totalStudents = makeCard(BLUE, CYAN), activeStudents = makeCard(GREEN, LIME), inactiveStudents = makeCard(ORANGE, YELLOW);
    // Rooms
    private final card totalRooms = makeCard(BLUE, CYAN), occupiedRooms = makeCard(PURPLE, PINK), availableRooms = makeCard(GREEN, LIME);
    private final card totalCapacity = makeCard(BLUE, CYAN), currentOccupancy = makeCard(PURPLE, PINK), occupancyPercent = makeCard(ORANGE, YELLOW);
    // Payments
    private final card totalPayments = makeCard(BLUE, CYAN), paidAmount = makeCard(GREEN, LIME),
            unpaidAmount = makeCard(RED, ROSE), partialPayments = makeCard(ORANGE, YELLOW);
    // Employees
    private final card totalEmployees = makeCard(BLUE, CYAN), activeEmployees = makeCard(GREEN, LIME);
    // Finance
    private final card income = makeCard(GREEN, LIME), salary = makeCard(ORANGE, YELLOW), costs = makeCard(RED, ROSE),
            netBalance = makeCard(PURPLE, PINK);

    private final DefaultTableModel activityModel = new DefaultTableModel(new String[]{"Time", "Type", "Activity"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JLabel status = new JLabel(" ");
    /** How many times the dashboard started a reload (used by the tests). */
    int refreshCount = 0;
    private boolean loading = false;
    private boolean loadAgain = false;

    public FormDashboard() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lb = new JLabel("Dashboard");
        lb.putClientProperty(FlatClientProperties.STYLE, "font:$h1.font");
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lb, BorderLayout.WEST);
        top.add(refreshButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel content = new WidthTrackingPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(section("Students", totalStudents, activeStudents, inactiveStudents));
        content.add(section("Rooms", totalRooms, occupiedRooms, availableRooms, totalCapacity, currentOccupancy, occupancyPercent));
        content.add(section("Payments", totalPayments, paidAmount, unpaidAmount, partialPayments));
        content.add(section("Employees", totalEmployees, activeEmployees));
        content.add(section("Finance", income, salary, costs, netBalance));

        JTable activities = new JTable(activityModel);
        activities.setFillsViewportHeight(true);
        JScrollPane activityScroll = new JScrollPane(activities);
        activityScroll.setBorder(BorderFactory.createTitledBorder("Recent Activities"));
        activityScroll.setAlignmentX(LEFT_ALIGNMENT);
        activityScroll.setPreferredSize(new Dimension(100, 280));
        activityScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, 280));
        content.add(activityScroll);

        JLabel intro = new JLabel("<html><div style='width:650px;'><b>Abdolhashem Management System</b><br><br>"
                + "Abdolhashem Management System is a modern and user-friendly application designed to simplify and organize daily "
                + "management tasks. It allows administrators to manage students, employees, rooms, payments, salaries, costs, and "
                + "users from one centralized system.<br><br>"
                + "The dashboard provides a clear overview of important information, while features such as notifications, user "
                + "management, settings, and Light/Dark Mode make the system easier and more efficient to use.</div></html>");
        intro.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        intro.setAlignmentX(LEFT_ALIGNMENT);
        content.add(intro);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        status.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
        add(status, BorderLayout.SOUTH);

        refresh();
    }

    // The dashboard listens for changes only while it is on screen.
    @Override
    public void addNotify() {
        super.addNotify();
        DataEvents.addListener(refreshListener);
    }

    @Override
    public void removeNotify() {
        DataEvents.removeListener(refreshListener);
        super.removeNotify();
    }

    /** A panel that always has the width of the window, so only the height scrolls. */
    private static class WidthTrackingPanel extends JPanel implements javax.swing.Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientation, int direction) {
            return 120;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static card makeCard(Color color, Color gradient) {
        card c = new card();
        c.setBackground(color);
        c.setColorGradient(gradient);
        return c;
    }

    private JPanel section(String title, card... cards) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setOpaque(false);
        box.setAlignmentX(LEFT_ALIGNMENT);
        JLabel label = new JLabel(title);
        label.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        box.add(label, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
        grid.setOpaque(false);
        for (card c : cards) {
            grid.add(c);
        }
        box.add(grid, BorderLayout.CENTER);
        int rows = (cards.length + 2) / 3;
        box.setMaximumSize(new Dimension(Short.MAX_VALUE, 30 + rows * 130));
        box.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        return box;
    }

    /** Loads all numbers in the background and then shows them. Safe to call many times. */
    public void refresh() {
        if (loading) {
            loadAgain = true;
            return;
        }
        loading = true;
        refreshCount++;
        new SwingWorker<Stats, Void>() {
            @Override
            protected Stats doInBackground() throws Exception {
                return DashboardService.load();
            }

            @Override
            protected void done() {
                loading = false;
                try {
                    show(get());
                } catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    status.setText("Could not load data: " + (cause instanceof AppException ? cause.getMessage() : cause));
                }
                if (loadAgain) {
                    loadAgain = false;
                    refresh();
                }
            }
        }.execute();
    }

    private static int pct(long part, long total) {
        return total == 0 ? 0 : (int) Math.round(part * 100.0 / total);
    }

    private void put(card c, String title, long value, Integer percent, String note) {
        c.setData(new ModelCard(title, value, percent == null ? 0 : percent, null));
        if (percent == null) {
            c.setNote(note);
        }
    }

    private void show(Stats s) {
        put(totalStudents, "Total Students", s.totalStudents, null, "all registered students");
        put(activeStudents, "Active Students", s.activeStudents, pct(s.activeStudents, s.totalStudents), null);
        put(inactiveStudents, "Inactive Students", s.inactiveStudents, pct(s.inactiveStudents, s.totalStudents), null);

        put(totalRooms, "Total Rooms", s.totalRooms, null, "rooms in the dormitory");
        put(occupiedRooms, "Occupied Rooms", s.occupiedRooms, pct(s.occupiedRooms, s.totalRooms), null);
        put(availableRooms, "Available Rooms", s.availableRooms, pct(s.availableRooms, s.totalRooms), null);
        put(totalCapacity, "Total Capacity", s.totalCapacity, null, "beds");
        put(currentOccupancy, "Current Occupancy", s.currentOccupancy, pct(s.currentOccupancy, s.totalCapacity), null);
        put(occupancyPercent, "Occupancy Percentage", s.occupancyPercent, null, "% of all beds are in use");

        put(totalPayments, "Total Payments", s.totalPayments, null, "payment records");
        put(paidAmount, "Total Paid Amount", s.totalPaidAmount, pct(s.totalPaidAmount, s.totalPaidAmount + s.totalUnpaidAmount), null);
        put(unpaidAmount, "Total Unpaid Amount", s.totalUnpaidAmount, pct(s.totalUnpaidAmount, s.totalPaidAmount + s.totalUnpaidAmount), null);
        put(partialPayments, "Partial Payments", s.partialPayments, pct(s.partialPayments, s.totalPayments), null);

        put(totalEmployees, "Total Employees", s.totalEmployees, null, "all registered employees");
        put(activeEmployees, "Active Employees", s.activeEmployees, pct(s.activeEmployees, s.totalEmployees), null);

        put(income, "Total Income", s.totalIncome, null, "AFN received from students");
        put(salary, "Total Salary", s.totalSalary, null, "AFN paid to employees");
        put(costs, "Total Costs", s.totalCosts, null, "AFN spent");
        put(netBalance, "Net Balance", s.netBalance, null, "AFN = income - costs - salary");

        activityModel.setRowCount(0);
        for (String[] row : s.recent) {
            activityModel.addRow(row);
        }
        status.setText("Updated " + java.time.LocalTime.now().withNano(0) + "  |  Net balance: " + money.format(s.netBalance) + " AFN");
    }
}
