package nematullah.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small helpers that validate user input and turn SQL errors into friendly messages. */
public final class Check {

    private static final Pattern DATE = Pattern.compile("^(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})$");
    private static final Pattern PERIOD = Pattern.compile("^(\\d{4})[-/]?(\\d{1,2})$");

    private Check() {
    }

    /** Returns the trimmed text, or throws if it is empty. */
    public static String required(String value, String label) throws AppException {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new AppException(label + " is required.");
        }
        return text;
    }

    /** Returns the trimmed text, or null when it is empty (stored as NULL in the database). */
    public static String optional(String value) {
        String text = value == null ? "" : value.trim();
        return text.isEmpty() ? null : text;
    }

    /** Whole number greater than zero (money amounts, room numbers ...). */
    public static int positiveInt(String value, String label) throws AppException {
        int number = wholeNumber(value, label);
        if (number <= 0) {
            throw new AppException(label + " must be greater than zero.");
        }
        return number;
    }

    /** Whole number that is zero or more. */
    public static int nonNegativeInt(String value, String label) throws AppException {
        int number = wholeNumber(value, label);
        if (number < 0) {
            throw new AppException(label + " cannot be negative.");
        }
        return number;
    }

    private static int wholeNumber(String value, String label) throws AppException {
        String text = required(value, label);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new AppException(label + " must be a whole number.");
        }
    }

    /**
     * Checks a date written as yyyy-MM-dd (Gregorian or Solar Hijri) and returns it zero-padded.
     * An empty value gives null when it is optional.
     */
    public static String date(String value, String label, boolean mandatory) throws AppException {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            if (mandatory) {
                throw new AppException(label + " is required (format yyyy-MM-dd).");
            }
            return null;
        }
        Matcher m = DATE.matcher(text);
        if (!m.matches()) {
            throw new AppException(label + " must look like 2025-03-21 (yyyy-MM-dd).");
        }
        int year = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int day = Integer.parseInt(m.group(3));
        if (year < 1300 || year > 2200 || month < 1 || month > 12 || day < 1 || day > 31) {
            throw new AppException(label + " is not a valid date.");
        }
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /** A financial period is a month written as yyyyMM or yyyy-MM, stored as a number such as 202611. */
    public static int period(String value, String label) throws AppException {
        String text = required(value, label);
        Matcher m = PERIOD.matcher(text);
        if (!m.matches()) {
            throw new AppException(label + " must look like 202611 or 2026-11 (year and month).");
        }
        int year = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        if (year < 1300 || year > 2200 || month < 1 || month > 12) {
            throw new AppException(label + " is not a valid year and month.");
        }
        return year * 100 + month;
    }

    /** Shows a stored period number (202611) as 2026-11. */
    public static String showPeriod(int period) {
        return String.format("%04d-%02d", period / 100, period % 100);
    }

    public static String today() {
        return LocalDate.now().toString();
    }

    /** Converts a technical SQL error into a short sentence the user can understand. */
    public static AppException fromSql(SQLException e) {
        String m = String.valueOf(e.getMessage());
        if (m.contains("UNIQUE constraint failed")) {
            if (m.contains("students.NIC")) {
                return new AppException("A student with this NIC already exists.");
            }
            if (m.contains("students.PhoneNumber")) {
                return new AppException("A student with this phone number already exists.");
            }
            if (m.contains("employee.PhoneNumber")) {
                return new AppException("An employee with this phone number already exists.");
            }
            if (m.contains("user.UserName")) {
                return new AppException("This username already exists.");
            }
            if (m.contains("payment.student_id")) {
                return new AppException("A payment record for this student and period already exists. Edit it instead.");
            }
            if (m.contains("salary.employee_id")) {
                return new AppException("The salary for this employee and period was already recorded.");
            }
            if (m.contains("costs.")) {
                return new AppException("This exact cost was already recorded.");
            }
            return new AppException("This record already exists.", e);
        }
        if (m.contains("FOREIGN KEY")) {
            return new AppException("This record is linked to other data, so it cannot be saved or removed.", e);
        }
        if (m.contains("CHECK constraint")) {
            return new AppException("One of the values is not valid.", e);
        }
        return new AppException("Database error: " + m, e);
    }
}
