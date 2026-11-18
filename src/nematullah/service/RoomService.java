package nematullah.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import nematullah.database.Database;

/** Everything about rooms: first-run data, capacity checks and the Available / Unavailable status. */
public final class RoomService {

    /** Monthly rent per student (Afghani) for each room size. Change here if prices change. */
    public static final int RENT_3_PERSON = 1200;
    public static final int RENT_4_PERSON = 1500;
    public static final int RENT_5_PERSON = 1800;

    private RoomService() {
    }

    /**
     * Creates the 15 rooms of the dormitory: rooms 1-4 hold 4 students, rooms 5-10 hold 5,
     * rooms 11-15 hold 3. INSERT OR IGNORE means running it again never creates duplicates.
     */
    public static void seedRooms(Connection con) throws SQLException {
        for (int room = 1; room <= 15; room++) {
            int capacity = room <= 4 ? 4 : (room <= 10 ? 5 : 3);
            Database.update(con, "INSERT OR IGNORE INTO room (room_number, Capacity, Rent, Status) VALUES (?, ?, ?, 'Available')",
                    room, capacity, rentFor(capacity));
        }
    }

    public static int rentFor(int capacity) {
        if (capacity == 3) {
            return RENT_3_PERSON;
        }
        if (capacity == 4) {
            return RENT_4_PERSON;
        }
        return RENT_5_PERSON;
    }

    /** Number of Active students living in the room (always calculated, never stored). */
    public static int residents(Connection con, int room, int exceptStudentId) throws SQLException {
        return (int) Database.number(con,
                "SELECT COUNT(*) FROM students WHERE room_number = ? AND Status = 'Active' AND student_id <> ?",
                room, exceptStudentId);
    }

    public static void requireRoom(Connection con, int room) throws AppException, SQLException {
        if (Database.number(con, "SELECT COUNT(*) FROM room WHERE room_number = ?", room) == 0) {
            throw new AppException("Room " + room + " does not exist.");
        }
    }

    /** Throws when the room is full. exceptStudentId lets a student be "checked in" to his own room again. */
    public static void requireFreeSpace(Connection con, int room, int exceptStudentId) throws AppException, SQLException {
        requireRoom(con, room);
        int capacity = (int) Database.number(con, "SELECT Capacity FROM room WHERE room_number = ?", room);
        if (residents(con, room, exceptStudentId) >= capacity) {
            throw new AppException("Room " + room + " is full (capacity " + capacity + ").");
        }
    }

    /** Sets Available / Unavailable from the real number of active students. */
    public static void refreshStatus(Connection con, int room) throws SQLException {
        Database.update(con, "UPDATE room SET Status = CASE WHEN "
                + "(SELECT COUNT(*) FROM students WHERE room_number = room.room_number AND Status = 'Active') >= Capacity "
                + "THEN 'Unavailable' ELSE 'Available' END WHERE room_number = ?", room);
    }

    public static void refreshAllStatuses(Connection con) throws SQLException {
        Database.update(con, "UPDATE room SET Status = CASE WHEN "
                + "(SELECT COUNT(*) FROM students WHERE room_number = room.room_number AND Status = 'Active') >= Capacity "
                + "THEN 'Unavailable' ELSE 'Available' END");
    }

    /** Table for the Rooms screen. */
    public static String[][] list(String search) throws AppException {
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT r.room_number, r.Capacity, "
                + "(SELECT COUNT(*) FROM students s WHERE s.room_number = r.room_number AND s.Status = 'Active') AS current_residents, "
                + "r.Capacity - (SELECT COUNT(*) FROM students s WHERE s.room_number = r.room_number AND s.Status = 'Active') AS free_beds, "
                + "r.Rent, r.Discount, r.Status FROM room r "
                + "WHERE CAST(r.room_number AS TEXT) LIKE ? OR r.Status LIKE ? ORDER BY r.room_number", like, like));
    }

    /** Active students of one room, for the "Residents" button. */
    public static String[][] residentsOf(int room) throws AppException {
        return Database.inTransactionRead(con -> Database.query(con,
                "SELECT student_id, FirstName || ' ' || LastName, FName, Faculty, Date_of_entry "
                + "FROM students WHERE room_number = ? AND Status = 'Active' ORDER BY FirstName", room));
    }

    /** Changes the monthly rent and the discount of a room. */
    public static void updateRent(int room, String rentText, String discountText) throws AppException {
        int rent = Check.nonNegativeInt(rentText, "Rent");
        int discount = Check.nonNegativeInt(discountText, "Discount");
        if (discount > rent) {
            throw new AppException("Discount cannot be larger than the rent.");
        }
        Database.inTransaction(con -> {
            requireRoom(con, room);
            Database.update(con, "UPDATE room SET Rent = ?, Discount = ? WHERE room_number = ?", rent, discount, room);
            Activity.log(con, "Room", "Room " + room + " rent set to " + rent + " AFN (discount " + discount + ")");
            return null;
        });
    }

    /** Room numbers that still have a free bed. */
    public static List<Integer> roomsWithFreeBeds() throws AppException {
        return Database.inTransactionRead(con -> {
            String[][] rows = Database.query(con, "SELECT room_number FROM room WHERE Status = 'Available' ORDER BY room_number");
            List<Integer> rooms = new java.util.ArrayList<>();
            for (String[] row : rows) {
                rooms.add(Integer.parseInt(row[0]));
            }
            return rooms;
        });
    }
}
