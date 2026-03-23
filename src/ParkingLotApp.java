import java.util.*;

public class ParkingLotApp {

    // 🔹 Parking Spot Status
    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    // 🔹 Parking Slot
    static class Slot {
        String licensePlate;
        long entryTime;
        Status status;

        Slot() {
            status = Status.EMPTY;
        }
    }

    // 🔹 Parking System
    static class ParkingLot {

        private Slot[] table;
        private int capacity;
        private int size = 0;

        // Stats
        private int totalProbes = 0;
        private int totalOperations = 0;

        public ParkingLot(int capacity) {
            this.capacity = capacity;
            table = new Slot[capacity];
            for (int i = 0; i < capacity; i++) {
                table[i] = new Slot();
            }
        }

        // 🔹 Hash Function
        private int hash(String key) {
            return Math.abs(key.hashCode()) % capacity;
        }

        // 🔹 Park Vehicle (Linear Probing)
        public void parkVehicle(String licensePlate) {

            int index = hash(licensePlate);
            int probes = 0;

            while (table[index].status == Status.OCCUPIED) {
                index = (index + 1) % capacity;
                probes++;
            }

            table[index].licensePlate = licensePlate;
            table[index].entryTime = System.currentTimeMillis();
            table[index].status = Status.OCCUPIED;

            size++;
            totalProbes += probes;
            totalOperations++;

            System.out.println("parkVehicle(\"" + licensePlate + "\") → Assigned spot #"
                    + index + " (" + probes + " probes)");
        }

        // 🔹 Exit Vehicle
        public void exitVehicle(String licensePlate) {

            int index = hash(licensePlate);
            int probes = 0;

            while (table[index].status != Status.EMPTY) {

                if (table[index].status == Status.OCCUPIED &&
                        table[index].licensePlate.equals(licensePlate)) {

                    long durationMs = System.currentTimeMillis() - table[index].entryTime;
                    double hours = durationMs / (1000.0 * 60 * 60);

                    double fee = calculateFee(hours);

                    table[index].status = Status.DELETED;
                    size--;

                    System.out.printf(
                            "exitVehicle(\"%s\") → Spot #%d freed, Duration: %.2fh, Fee: $%.2f\n",
                            licensePlate, index, hours, fee
                    );
                    return;
                }

                index = (index + 1) % capacity;
                probes++;
            }

            System.out.println("Vehicle not found!");
        }

        // 🔹 Fee Calculation ($5 per hour)
        private double calculateFee(double hours) {
            return Math.ceil(hours) * 5;
        }

        // 🔹 Find Nearest Available Spot
        public void findNearestAvailable() {
            for (int i = 0; i < capacity; i++) {
                if (table[i].status != Status.OCCUPIED) {
                    System.out.println("Nearest available spot: #" + i);
                    return;
                }
            }
            System.out.println("Parking Full!");
        }

        // 🔹 Statistics
        public void getStatistics() {
            double occupancy = (size * 100.0) / capacity;
            double avgProbes = (totalOperations == 0) ? 0 :
                    (totalProbes * 1.0 / totalOperations);

            System.out.println("\n=== Parking Statistics ===");
            System.out.printf("Occupancy: %.2f%%\n", occupancy);
            System.out.printf("Avg Probes: %.2f\n", avgProbes);
            System.out.println("Peak Hour: 2-3 PM (simulated)");
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) throws InterruptedException {

        ParkingLot lot = new ParkingLot(10); // small for demo

        // 🔹 Park Vehicles
        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        // 🔹 Find nearest spot
        lot.findNearestAvailable();

        // 🔹 Wait to simulate time
        Thread.sleep(2000);

        // 🔹 Exit Vehicle
        lot.exitVehicle("ABC-1234");

        // 🔹 Stats
        lot.getStatistics();
    }
}
