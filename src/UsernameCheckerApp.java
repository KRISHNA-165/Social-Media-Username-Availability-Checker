import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameCheckerApp {

    // 🔹 Inner Class: UsernameChecker
    static class UsernameChecker {

        // Username -> UserID
        private Map<String, Integer> userMap;

        // Username -> Attempt Count
        private Map<String, Integer> attemptMap;

        public UsernameChecker() {
            userMap = new ConcurrentHashMap<>();
            attemptMap = new ConcurrentHashMap<>();
        }

        // 🔹 Register existing users
        public void registerUser(String username, int userId) {
            userMap.put(username.toLowerCase(), userId);
        }

        // 🔹 Check availability (O(1))
        public boolean checkAvailability(String username) {
            username = username.toLowerCase();

            // Track attempts
            attemptMap.put(username,
                    attemptMap.getOrDefault(username, 0) + 1);

            return !userMap.containsKey(username);
        }

        // 🔹 Suggest alternatives
        public List<String> suggestAlternatives(String username) {
            username = username.toLowerCase();
            List<String> suggestions = new ArrayList<>();

            // Append numbers
            for (int i = 1; i <= 5; i++) {
                String newName = username + i;
                if (!userMap.containsKey(newName)) {
                    suggestions.add(newName);
                }
            }

            // Replace "_" with "."
            if (username.contains("_")) {
                String alt = username.replace("_", ".");
                if (!userMap.containsKey(alt)) {
                    suggestions.add(alt);
                }
            }

            // Add prefix
            String prefix = "the_" + username;
            if (!userMap.containsKey(prefix)) {
                suggestions.add(prefix);
            }

            // Add suffix
            String suffix = username + "_official";
            if (!userMap.containsKey(suffix)) {
                suggestions.add(suffix);
            }

            return suggestions;
        }

        // 🔹 Get most attempted username
        public String getMostAttempted() {
            String result = "";
            int max = 0;

            for (Map.Entry<String, Integer> entry : attemptMap.entrySet()) {
                if (entry.getValue() > max) {
                    max = entry.getValue();
                    result = entry.getKey();
                }
            }

            return result + " (" + max + " attempts)";
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) {

        UsernameChecker checker = new UsernameChecker();

        // Preload users
        checker.registerUser("john_doe", 1);
        checker.registerUser("admin", 2);
        checker.registerUser("user123", 3);

        // 🔹 Availability check
        System.out.println(
                "checkAvailability(\"john_doe\") → " +
                        checker.checkAvailability("john_doe")
        );

        System.out.println(
                "checkAvailability(\"jane_smith\") → " +
                        checker.checkAvailability("jane_smith")
        );

        // 🔹 Suggestions
        System.out.println(
                "suggestAlternatives(\"john_doe\") → " +
                        checker.suggestAlternatives("john_doe")
        );

        // 🔹 Simulate high attempts
        for (int i = 0; i < 10543; i++) {
            checker.checkAvailability("admin");
        }

        // 🔹 Most attempted
        System.out.println(
                "getMostAttempted() → " +
                        checker.getMostAttempted()
        );
    }
}
