import java.util.*;

public class FinancialTransactionsApp {

    // 🔹 Transaction Class
    static class Transaction {
        int id;
        int amount;
        String merchant;
        String account;
        long timestamp; // in milliseconds

        Transaction(int id, int amount, String merchant, String account, long timestamp) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.timestamp = timestamp;
        }
    }

    // 🔹 Analyzer Class
    static class TransactionAnalyzer {

        List<Transaction> transactions;

        public TransactionAnalyzer(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        // 🔹 1. Classic Two-Sum
        public void findTwoSum(int target) {
            Map<Integer, Transaction> map = new HashMap<>();

            System.out.println("\nTwo-Sum Results:");

            for (Transaction t : transactions) {
                int complement = target - t.amount;

                if (map.containsKey(complement)) {
                    Transaction t2 = map.get(complement);
                    System.out.println("(" + t2.id + ", " + t.id + ")");
                }

                map.put(t.amount, t);
            }
        }

        // 🔹 2. Two-Sum with Time Window (1 hour)
        public void findTwoSumWithTimeWindow(int target) {
            Map<Integer, List<Transaction>> map = new HashMap<>();

            System.out.println("\nTwo-Sum (1-hour window):");

            for (Transaction t : transactions) {

                int complement = target - t.amount;

                if (map.containsKey(complement)) {
                    for (Transaction prev : map.get(complement)) {
                        long diff = Math.abs(t.timestamp - prev.timestamp);

                        if (diff <= 3600 * 1000) { // 1 hour
                            System.out.println("(" + prev.id + ", " + t.id + ")");
                        }
                    }
                }

                map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
            }
        }

        // 🔹 3. K-Sum (Backtracking)
        public void findKSum(int k, int target) {
            System.out.println("\nK-Sum Results:");
            backtrack(new ArrayList<>(), 0, k, target);
        }

        private void backtrack(List<Transaction> temp, int start, int k, int target) {
            if (k == 0 && target == 0) {
                System.out.print("(");
                for (Transaction t : temp) {
                    System.out.print(t.id + " ");
                }
                System.out.println(")");
                return;
            }

            if (k == 0 || target < 0) return;

            for (int i = start; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);

                temp.add(t);
                backtrack(temp, i + 1, k - 1, target - t.amount);
                temp.remove(temp.size() - 1);
            }
        }

        // 🔹 4. Duplicate Detection
        public void detectDuplicates() {
            Map<String, List<Transaction>> map = new HashMap<>();

            System.out.println("\nDuplicate Transactions:");

            for (Transaction t : transactions) {
                String key = t.amount + "_" + t.merchant;

                map.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
            }

            for (Map.Entry<String, List<Transaction>> entry : map.entrySet()) {
                List<Transaction> list = entry.getValue();

                if (list.size() > 1) {
                    System.out.print("{amount=" + list.get(0).amount +
                            ", merchant=" + list.get(0).merchant +
                            ", accounts=[ ");

                    for (Transaction t : list) {
                        System.out.print(t.account + " ");
                    }

                    System.out.println("]}");
                }
            }
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) {

        List<Transaction> transactions = new ArrayList<>();

        long baseTime = System.currentTimeMillis();

        transactions.add(new Transaction(1, 500, "Store A", "acc1", baseTime));
        transactions.add(new Transaction(2, 300, "Store B", "acc2", baseTime + 1000));
        transactions.add(new Transaction(3, 200, "Store C", "acc3", baseTime + 2000));
        transactions.add(new Transaction(4, 500, "Store A", "acc4", baseTime + 3000));

        TransactionAnalyzer analyzer = new TransactionAnalyzer(transactions);

        // 🔹 Classic Two-Sum
        analyzer.findTwoSum(500);

        // 🔹 Time Window Two-Sum
        analyzer.findTwoSumWithTimeWindow(500);

        // 🔹 K-Sum
        analyzer.findKSum(3, 1000);

        // 🔹 Duplicate Detection
        analyzer.detectDuplicates();
    }
}
