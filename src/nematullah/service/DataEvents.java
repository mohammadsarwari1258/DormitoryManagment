package nematullah.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

/**
 * One central place that tells the screens "the data changed, please refresh".
 * Every successful database transaction calls fire(); the Dashboard listens and reloads itself.
 */
public final class DataEvents {

    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    private DataEvents() {
    }

    public static void addListener(Runnable listener) {
        LISTENERS.add(listener);
    }

    public static void removeListener(Runnable listener) {
        LISTENERS.remove(listener);
    }

    /** Runs all listeners on the Swing thread. */
    public static void fire() {
        for (Runnable listener : LISTENERS) {
            SwingUtilities.invokeLater(listener);
        }
    }
}
