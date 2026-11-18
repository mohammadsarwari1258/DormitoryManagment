package nematullah.service;

import java.sql.Connection;
import java.sql.SQLException;
import nematullah.database.Database;

/** All student operations: register, edit, exit, transfer, delete, search. Each change is one transaction. */
public final class StudentService {

    /** The data typed in the student forms. */
    public static class Student {
        public String room, firstName, lastName, fatherName, nic, phone, relativePhone, faculty, entryDate, exitDate;
    }

    private StudentService() {
    }

    /** Checks and cleans every field. After this call exitDate is null for an active student. */
    private static int validate(Student s) throws AppException {
        s.firstName = Check.required(s.firstName, "First name");
        s.lastName = Check.required(s.lastName, "Last name");
        s.fatherName = Check.required(s.fatherName, "Father name");
        s.nic = Check.required(s.nic, "NIC");
        s.faculty = Check.required(s.faculty, "Faculty");
        s.phone = Check.optional(s.phone);
        s.relativePhone = Check.optional(s.relativePhone);
        s.entryDate = Check.date(s.entryDate, "Date of entry", true);
        s.exitDate = Check.date(s.exitDate, "Date of exit", false);
        if (s.exitDate != null && s.exitDate.compareTo(s.entryDate) < 0) {
            throw new AppException("Date of exit cannot be before the date of entry.");
        }
        return Check.positiveInt(s.room, "Room number");
    }

    public static int add(Student s) throws AppException {
        int room = validate(s);
        return Database.inTransaction(con -> {
            boolean active = s.exitDate == null;
            if (active) {
                RoomService.requireFreeSpace(con, room, -1);
            } else {
                RoomService.requireRoom(con, room);
            }
            long id = Database.insert(con, "INSERT INTO students (room_number, FirstName, LastName, FName, NIC, PhoneNumber, "
                    + "ArelativePhoneNumber, Faculty, Date_of_entry, Date_of_exit, Status) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    room, s.firstName, s.lastName, s.fatherName, s.nic, s.phone, s.relativePhone, s.faculty,
                    s.entryDate, s.exitDate, active ? "Active" : "Inactive");
            RoomService.refreshStatus(con, room);
            Activity.log(con, "Student", "Student registered: " + s.firstName + " " + s.lastName + " (room " + room + ")");
            return (int) id;
        });
    }

    /** Edits a student. Changing the room is a transfer, so the new room capacity is checked. */
    public static void update(int studentId, Student s) throws AppException {
        int newRoom = validate(s);
        Database.inTransaction(con -> {
            String[][] old = Database.query(con, "SELECT room_number FROM students WHERE student_id = ?", studentId);
            if (old.length == 0) {
                throw new AppException("Student not found.");
            }
            int oldRoom = Integer.parseInt(old[0][0]);
            boolean active = s.exitDate == null;
            if (active) {
                RoomService.requireFreeSpace(con, newRoom, studentId);
            } else {
                RoomService.requireRoom(con, newRoom);
            }
            Database.update(con, "UPDATE students SET room_number=?, FirstName=?, LastName=?, FName=?, NIC=?, "
                    + "PhoneNumber=?, ArelativePhoneNumber=?, Faculty=?, Date_of_entry=?, Date_of_exit=?, Status=? WHERE student_id=?",
                    newRoom, s.firstName, s.lastName, s.fatherName, s.nic, s.phone, s.relativePhone, s.faculty,
                    s.entryDate, s.exitDate, active ? "Active" : "Inactive", studentId);
            RoomService.refreshStatus(con, oldRoom);
            RoomService.refreshStatus(con, newRoom);
            String text = "Student updated: " + s.firstName + " " + s.lastName;
            if (oldRoom != newRoom) {
                text += " (moved from room " + oldRoom + " to room " + newRoom + ")";
            }
            Activity.log(con, "Student", text);
            return null;
        });
    }

    /** The student leaves the dormitory: exit date is saved, the bed is freed, payments stay. */
    public static void exit(int studentId, String exitDateText) throws AppException {
        String exitDate = Check.date(exitDateText, "Date of exit", true);
        Database.inTransaction(con -> {
            String[][] row = Database.query(con,
                    "SELECT room_number, Date_of_entry, Status, FirstName || ' ' || LastName FROM students WHERE student_id = ?", studentId);
            if (row.length == 0) {
                throw new AppException("Student not found.");
            }
            if (!"Active".equals(row[0][2])) {
                throw new AppException("This student has already left the dormitory.");
            }
            if (exitDate.compareTo(row[0][1]) < 0) {
                throw new AppException("Date of exit cannot be before the date of entry (" + row[0][1] + ").");
            }
            Database.update(con, "UPDATE students SET Date_of_exit = ?, Status = 'Inactive' WHERE student_id = ?", exitDate, studentId);
            RoomService.refreshStatus(con, Integer.parseInt(row[0][0]));
            Activity.log(con, "Student", "Student left: " + row[0][3] + " (room " + row[0][0] + ")");
            return null;
        });
    }

    /** Moves an active student to another room. Old payments keep the room they were made for. */
    public static void transfer(int studentId, String newRoomText) throws AppException {
        int newRoom = Check.positiveInt(newRoomText, "New room number");
        Database.inTransaction(con -> {
            String[][] row = Database.query(con,
                    "SELECT room_number, Status, FirstName || ' ' || LastName FROM students WHERE student_id = ?", studentId);
            if (row.length == 0) {
                throw new AppException("Student not found.");
            }
            if (!"Active".equals(row[0][1])) {
                throw new AppException("Only an active student can be moved to another room.");
            }
            int oldRoom = Integer.parseInt(row[0][0]);
            if (oldRoom == newRoom) {
                throw new AppException("The student is already in room " + newRoom + ".");
            }
            RoomService.requireFreeSpace(con, newRoom, studentId);
            Database.update(con, "UPDATE students SET room_number = ? WHERE student_id = ?", newRoom, studentId);
            RoomService.refreshStatus(con, oldRoom);
            RoomService.refreshStatus(con, newRoom);
            Activity.log(con, "Student", "Student transferred: " + row[0][2] + " (room " + oldRoom + " -> room " + newRoom + ")");
            return null;
        });
    }

    /** Deleting is only allowed when the student has no payment history (otherwise use Exit). */
    public static void delete(int studentId) throws AppException {
        Database.inTransaction(con -> {
            String[][] row = Database.query(con,
                    "SELECT room_number, FirstName || ' ' || LastName FROM students WHERE student_id = ?", studentId);
            if (row.length == 0) {
                throw new AppException("Student not found.");
            }
            if (Database.number(con, "SELECT COUNT(*) FROM payment WHERE student_id = ?", studentId) > 0) {
                throw new AppException("This student has payment records, so he cannot be deleted. Use Exit instead; "
                        + "the financial history is kept.");
            }
            Database.update(con, "DELETE FROM students WHERE student_id = ?", studentId);
            RoomService.refreshStatus(con, Integer.parseInt(row[0][0]));
            Activity.log(con, "Student", "Student deleted: " + row[0][1]);
            return null;
        });
    }

    public static final String[] HEADER = {"Id", "Room", "First Name", "Last Name", "Father Name", "NIC",
        "Relative Phone", "Faculty", "Date Of Entry", "Date Of Exit", "Status"};

    /** Table rows; the search text is looked for in names, NIC, phones, room and status. */
    public static String[][] list(String search) throws AppException {
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT student_id, room_number, FirstName, LastName, FName, NIC, COALESCE(ArelativePhoneNumber,''), "
                + "Faculty, Date_of_entry, Date_of_exit, Status FROM students "
                + "WHERE FirstName LIKE ? OR LastName LIKE ? OR (FirstName || ' ' || LastName) LIKE ? OR NIC LIKE ? "
                + "OR PhoneNumber LIKE ? OR ArelativePhoneNumber LIKE ? OR CAST(room_number AS TEXT) LIKE ? OR Status LIKE ? "
                + "ORDER BY student_id", like, like, like, like, like, like, like, like));
    }

    /** Human readable details of one student, including his payment totals. */
    public static String details(int studentId) throws AppException {
        return Database.inTransactionRead(con -> {
            String[][] r = Database.query(con, "SELECT FirstName, LastName, FName, NIC, PhoneNumber, ArelativePhoneNumber, "
                    + "Faculty, room_number, Date_of_entry, Date_of_exit, Status FROM students WHERE student_id = ?", studentId);
            if (r.length == 0) {
                throw new AppException("Student not found.");
            }
            long[] money = PaymentService.summary(con, studentId);
            String[] s = r[0];
            return "Name: " + s[0] + " " + s[1] + "\nFather name: " + s[2] + "\nNIC: " + s[3]
                    + "\nStudent phone: " + s[4] + "\nRelative phone: " + s[5] + "\nFaculty: " + s[6]
                    + "\nRoom: " + s[7] + "\nDate of entry: " + s[8] + "\nDate of exit: " + (s[9].isEmpty() ? "-" : s[9])
                    + "\nStatus: " + s[10]
                    + "\n\nPayment periods: " + money[0] + "\nTotal due: " + money[1] + " AFN"
                    + "\nTotal paid: " + money[2] + " AFN\nRemaining debt: " + money[3] + " AFN";
        });
    }

    /** Drop-down items like "3 - Ali Test (room 11)". The id is the text before " - ". */
    public static String[] options(boolean activeOnly) throws AppException {
        return Database.inTransactionRead(con -> {
            String[][] rows = Database.query(con, "SELECT student_id || ' - ' || FirstName || ' ' || LastName || ' (room ' || room_number || ')' "
                    + "FROM students " + (activeOnly ? "WHERE Status = 'Active' " : "") + "ORDER BY student_id");
            String[] items = new String[rows.length];
            for (int i = 0; i < rows.length; i++) {
                items[i] = rows[i][0];
            }
            return items;
        });
    }

    /** Reads the number in front of " - " from a drop-down item. */
    public static int idOf(String option) throws AppException {
        if (option == null || !option.contains(" - ")) {
            throw new AppException("Please choose an item from the list.");
        }
        return Integer.parseInt(option.substring(0, option.indexOf(" - ")).trim());
    }

    /** Used by forms: reads one student (for the Edit screen). */
    public static String[] find(Connection con, int studentId) throws SQLException {
        String[][] r = Database.query(con, "SELECT room_number, FirstName, LastName, FName, NIC, PhoneNumber, "
                + "ArelativePhoneNumber, Faculty, Date_of_entry, Date_of_exit FROM students WHERE student_id = ?", studentId);
        return r.length == 0 ? null : r[0];
    }
}
