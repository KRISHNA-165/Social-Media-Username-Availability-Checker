import java.util.*;

public class MultiLevelCacheApp {

    // 🔹 Video Data
    static class Video {
        String id;
        String content;

        Video(String id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    // 🔹 LRU Cache using LinkedHashMap
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public LRUCache(int capacity) {
            super(capacity, 0.75f, true); // access-order
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    // 🔹 Multi-Level Cache System
    static class MultiLevelCache {

        // L1, L2 caches
        private LRUCache<String, Video> L1;
        private LRUCache<String, Video> L2;

        // L3 (Database simulation)
        private Map<String, Video> database;

        // Access count (for promotion)
        private Map<String, Integer> accessCount;

        // Stats
        private int L1Hits = 0, L2Hits = 0, L3Hits = 0;
        private int totalRequests = 0;

        public MultiLevelCache() {
            L1 = new LRUCache<>(10);     // small for demo (real: 10,000)
            L2 = new LRUCache<>(20);     // small for demo (real: 100,000)
            database = new HashMap<>();
            accessCount = new HashMap<>();

            preloadDatabase();
        }

        // 🔹 Simulated DB
        private void preloadDatabase() {
            for (int i = 1; i <= 50; i++) {
                database.put("video_" + i,
                        new Video("video_" + i, "Content of video " + i));
            }
        }

        // 🔹 Get Video
        public Video getVideo(String videoId) {

            totalRequests++;
            long start = System.nanoTime();

            // 🔹 L1 Check
            if (L1.containsKey(videoId)) {
                L1Hits++;
                System.out.println("→ L1 Cache HIT (0.5ms)");
                return L1.get(videoId);
            }

            System.out.println("→ L1 Cache MISS");

            // 🔹 L2 Check
            if (L2.containsKey(videoId)) {
                L2Hits++;
                System.out.println("→ L2 Cache HIT (5ms)");

                Video v = L2.get(videoId);

                // Promote to L1
                L1.put(videoId, v);
                System.out.println("→ Promoted to L1");

                return v;
            }

            System.out.println("→ L2 Cache MISS");

            // 🔹 L3 (Database)
            if (database.containsKey(videoId)) {
                L3Hits++;
                System.out.println("→ L3 Database HIT (150ms)");

                Video v = database.get(videoId);

                // Add to L2
                L2.put(videoId, v);

                // Update access count
                int count = accessCount.getOrDefault(videoId, 0) + 1;
                accessCount.put(videoId, count);

                return v;
            }

            System.out.println("Video not found!");
            return null;
        }

        // 🔹 Statistics
        public void getStatistics() {
            double l1Rate = (L1Hits * 100.0) / totalRequests;
            double l2Rate = (L2Hits * 100.0) / totalRequests;
            double l3Rate = (L3Hits * 100.0) / totalRequests;

            double overall = ((L1Hits + L2Hits) * 100.0) / totalRequests;

            System.out.println("\n=== Cache Statistics ===");
            System.out.printf("L1: Hit Rate %.2f%%, Avg Time: 0.5ms\n", l1Rate);
            System.out.printf("L2: Hit Rate %.2f%%, Avg Time: 5ms\n", l2Rate);
            System.out.printf("L3: Hit Rate %.2f%%, Avg Time: 150ms\n", l3Rate);
            System.out.printf("Overall Hit Rate: %.2f%%\n", overall);
        }

        // 🔹 Cache Invalidation
        public void invalidate(String videoId) {
            L1.remove(videoId);
            L2.remove(videoId);
            database.remove(videoId);

            System.out.println("Cache invalidated for " + videoId);
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        // 🔹 First request (L3 → L2)
        System.out.println("\ngetVideo(\"video_10\")");
        cache.getVideo("video_10");

        // 🔹 Second request (L2 → L1)
        System.out.println("\ngetVideo(\"video_10\")");
        cache.getVideo("video_10");

        // 🔹 Third request (L1 hit)
        System.out.println("\ngetVideo(\"video_10\")");
        cache.getVideo("video_10");

        // 🔹 Another video
        System.out.println("\ngetVideo(\"video_20\")");
        cache.getVideo("video_20");

        // 🔹 Stats
        cache.getStatistics();
    }
}
