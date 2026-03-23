import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FlashSaleApp {

    // 🔹 Inner Class: Inventory Manager
    static class InventoryManager {

        // Product -> Stock
        private Map<String, Integer> stockMap;

        // Product -> Waiting List (FIFO)
        private Map<String, LinkedHashMap<Integer, Integer>> waitingListMap;

        public InventoryManager() {
            stockMap = new ConcurrentHashMap<>();
            waitingListMap = new ConcurrentHashMap<>();
        }

        // 🔹 Add Product
        public void addProduct(String productId, int stock) {
            stockMap.put(productId, stock);
            waitingListMap.put(productId, new LinkedHashMap<>());
        }

        // 🔹 Check Stock (O(1))
        public int checkStock(String productId) {
            return stockMap.getOrDefault(productId, 0);
        }

        // 🔹 Purchase Item (Thread-safe)
        public synchronized String purchaseItem(String productId, int userId) {

            int stock = stockMap.getOrDefault(productId, 0);

            // If stock available
            if (stock > 0) {
                stockMap.put(productId, stock - 1);
                return "Success, " + (stock - 1) + " units remaining";
            }

            // If out of stock → add to waiting list
            LinkedHashMap<Integer, Integer> queue = waitingListMap.get(productId);

            int position = queue.size() + 1;
            queue.put(userId, position);

            return "Added to waiting list, position #" + position;
        }

        // 🔹 Display Waiting List
        public void showWaitingList(String productId) {
            LinkedHashMap<Integer, Integer> queue = waitingListMap.get(productId);

            System.out.println("Waiting List for " + productId + ":");
            for (Map.Entry<Integer, Integer> entry : queue.entrySet()) {
                System.out.println("User " + entry.getKey() +
                        " → Position " + entry.getValue());
            }
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) {

        InventoryManager manager = new InventoryManager();

        // Add product with 100 units
        manager.addProduct("IPHONE15_256GB", 100);

        // 🔹 Check stock
        System.out.println("checkStock → " +
                manager.checkStock("IPHONE15_256GB") + " units available");

        // 🔹 Simulate purchases
        for (int i = 1; i <= 102; i++) {
            String result = manager.purchaseItem("IPHONE15_256GB", i);
            System.out.println("User " + i + ": " + result);
        }

        // 🔹 Show waiting list
        manager.showWaitingList("IPHONE15_256GB");
    }
}

