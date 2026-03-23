import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AutocompleteSystemApp {

    // 🔹 Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }

    // 🔹 Autocomplete System
    static class AutocompleteSystem {

        // Query → Frequency
        private Map<String, Integer> frequencyMap;

        // Trie Root
        private TrieNode root;

        public AutocompleteSystem() {
            frequencyMap = new ConcurrentHashMap<>();
            root = new TrieNode();
        }

        // 🔹 Insert query into Trie
        private void insert(String query) {
            TrieNode node = root;

            for (char ch : query.toCharArray()) {
                node.children.putIfAbsent(ch, new TrieNode());
                node = node.children.get(ch);
            }

            node.isEnd = true;
        }

        // 🔹 Add or update query frequency
        public void updateFrequency(String query) {
            frequencyMap.put(query,
                    frequencyMap.getOrDefault(query, 0) + 1);

            insert(query);
        }

        // 🔹 Search suggestions for prefix
        public List<String> search(String prefix) {

            TrieNode node = root;

            // Traverse prefix
            for (char ch : prefix.toCharArray()) {
                if (!node.children.containsKey(ch)) {
                    return new ArrayList<>();
                }
                node = node.children.get(ch);
            }

            // Collect all queries with this prefix
            List<String> results = new ArrayList<>();
            dfs(node, prefix, results);

            // Get Top 10 using Min Heap
            PriorityQueue<String> pq = new PriorityQueue<>(
                    (a, b) -> frequencyMap.get(a) - frequencyMap.get(b)
            );

            for (String q : results) {
                pq.offer(q);
                if (pq.size() > 10) {
                    pq.poll();
                }
            }

            List<String> topResults = new ArrayList<>();
            while (!pq.isEmpty()) {
                topResults.add(pq.poll());
            }

            Collections.reverse(topResults);
            return topResults;
        }

        // 🔹 DFS to collect queries
        private void dfs(TrieNode node, String prefix, List<String> results) {
            if (node.isEnd) {
                results.add(prefix);
            }

            for (char ch : node.children.keySet()) {
                dfs(node.children.get(ch), prefix + ch, results);
            }
        }

        // 🔹 Display suggestions with frequency
        public void printSuggestions(String prefix) {
            List<String> suggestions = search(prefix);

            System.out.println("\nSearch(\"" + prefix + "\") →");

            int rank = 1;
            for (String s : suggestions) {
                System.out.println(rank + ". " + s +
                        " (" + frequencyMap.get(s) + " searches)");
                rank++;
            }
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        // 🔹 Preload queries
        system.updateFrequency("java tutorial");
        system.updateFrequency("javascript");
        system.updateFrequency("java download");
        system.updateFrequency("java tutorial");
        system.updateFrequency("java tutorial");
        system.updateFrequency("javascript");
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        // 🔹 Search prefix
        system.printSuggestions("jav");

        // 🔹 Update frequency (trending)
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println("\nAfter Trending Update:");
        system.printSuggestions("jav");
    }
}
