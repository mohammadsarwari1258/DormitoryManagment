package nematullah.service;

import nematullah.database.Database;

public final class DashboardService {

    public static class Stats {
        public long totalStudents, activeStudents, inactiveStudents;
        public long totalRooms, occupiedRooms, availableRooms, totalCapacity, currentOccupancy, occupancyPercent;
        public long totalPayments, totalPaidAmount, totalUnpaidAmount, partialPayments;
        public long totalEmployees, activeEmployees;
        public long totalIncome, totalSalary, totalCosts, netBalance;
        public String[][] recent = new String[0][];
    }

    private DashboardService() {
    }

    public static Stats load() throws AppException {
        return Database.inTransactionRead(con -> {
            Stats s = new Stats();
            s.totalStudents = Database.number(con, "SELECT COUNT(*) FROM students");
            s.activeStudents = Database.number(con, "SELECT COUNT(*) FROM students WHERE Status = 'Active'");
            s.inactiveStudents = s.totalStudents - s.activeStudents;

            s.totalRooms = Database.number(con, "SELECT COUNT(*) FROM room");
            s.occupiedRooms = Database.number(con, "SELECT COUNT(DISTINCT room_number) FROM students WHERE Status = 'Active'");
            s.availableRooms = Database.number(con, "SELECT COUNT(*) FROM room WHERE Status = 'Available'");
            s.totalCapacity = Database.number(con, "SELECT COALESCE(SUM(Capacity),0) FROM room");
            s.currentOccupancy = s.activeStudents;
            s.occupancyPercent = s.totalCapacity == 0 ? 0 : Math.round(s.currentOccupancy * 100.0 / s.totalCapacity);

            s.totalPayments = Database.number(con, "SELECT COUNT(*) FROM payment");
            s.totalPaidAmount = Database.number(con, "SELECT COALESCE(SUM(Receipt),0) FROM payment");
            s.totalUnpaidAmount = Database.number(con, "SELECT COALESCE(SUM(Remain),0) FROM payment");
            s.partialPayments = Database.number(con, "SELECT COUNT(*) FROM payment WHERE Receipt > 0 AND Receipt < Amount_of_money");

            s.totalEmployees = Database.number(con, "SELECT COUNT(*) FROM employee");
            s.activeEmployees = Database.number(con, "SELECT COUNT(*) FROM employee WHERE Status = 'Active'");

            s.totalIncome = s.totalPaidAmount;
            s.totalSalary = Database.number(con, "SELECT COALESCE(SUM(Receipt),0) FROM salary");
            s.totalCosts = Database.number(con, "SELECT COALESCE(SUM(price),0) FROM costs");
            s.netBalance = s.totalIncome - s.totalCosts - s.totalSalary;

            s.recent = Database.query(con, "SELECT Created_at, Category, Message FROM activity_log ORDER BY id DESC LIMIT 12");
            return s;
        });
    }
}
