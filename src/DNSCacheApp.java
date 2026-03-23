import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DNSCacheApp {

    // 🔹 Entry Class
    static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime; // in milliseconds

        DNSEntry(String domain, String ipAddress, int ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    // 🔹 DNS Cache Class
    static class DNSCache {

        private final int MAX_SIZE;

        // LRU Cache using LinkedHashMap
        private LinkedHashMap<String, DNSEntry> cache;

        // Stats
        private int hits = 0;
        private int misses = 0;
        private long totalLookupTime = 0;
        private int totalRequests = 0;

        public DNSCache(int size) {
            this.MAX_SIZE = size;

            cache = new LinkedHashMap<String, DNSEntry>(size, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                    return size() > MAX_SIZE;
                }
            };

            startCleanupThread();
        }

        // 🔹 Resolve domain
        public synchronized String resolve(String domain) {
            long startTime = System.nanoTime();
            totalRequests++;

            DNSEntry entry = cache.get(domain);

            // Cache HIT
            if (entry != null && !entry.isExpired()) {
                hits++;
                long time = System.nanoTime() - startTime;
                totalLookupTime += time;

                System.out.println("Cache HIT → " + entry.ipAddress);
                return entry.ipAddress;
            }

            // Cache MISS or EXPIRED
            if (entry != null && entry.isExpired()) {
                cache.remove(domain);
                System.out.println("Cache EXPIRED → Querying upstream...");
            } else {
                System.out.println("Cache MISS → Querying upstream...");
            }

            misses++;

            // Simulate upstream DNS call
            String newIP = queryUpstreamDNS(domain);

            // Store in cache (TTL = 5 seconds for demo)
            cache.put(domain, new DNSEntry(domain, newIP, 5));

            long time = System.nanoTime() - startTime;
            totalLookupTime += time;

            return newIP;
        }

        // 🔹 Simulate upstream DNS
        private String queryUpstreamDNS(String domain) {
            try {
                Thread.sleep(100); // simulate delay
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Generate fake IP
            return "172.217.14." + new Random().nextInt(255);
        }

        // 🔹 Cleanup expired entries (background thread)
        private void startCleanupThread() {
            Thread cleaner = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(2000); // run every 2 sec
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    synchronized (this) {
                        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                        while (it.hasNext()) {
                            if (it.next().getValue().isExpired()) {
                                it.remove();
                            }
                        }
                    }
                }
            });

            cleaner.setDaemon(true);
            cleaner.start();
        }

        // 🔹 Cache Statistics
        public void getCacheStats() {
            double hitRate = (totalRequests == 0) ? 0 :
                    (hits * 100.0 / totalRequests);

            double avgTimeMs = (totalRequests == 0) ? 0 :
                    (totalLookupTime / totalRequests) / 1_000_000.0;

            System.out.println("\nCache Stats:");
            System.out.println("Hit Rate: " + hitRate + "%");
            System.out.println("Average Lookup Time: " + avgTimeMs + " ms");
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) throws InterruptedException {

        DNSCache dnsCache = new DNSCache(3);

        // First request → MISS
        System.out.println("resolve(google.com) → " +
                dnsCache.resolve("google.com"));

        // Second request → HIT
        System.out.println("resolve(google.com) → " +
                dnsCache.resolve("google.com"));

        // Wait for expiry (TTL = 5s)
        Thread.sleep(6000);

        // After expiry → MISS again
        System.out.println("resolve(google.com) → " +
                dnsCache.resolve("google.com"));

        // More requests
        dnsCache.resolve("yahoo.com");
        dnsCache.resolve("bing.com");
        dnsCache.resolve("openai.com"); // triggers LRU eviction

        // Stats
        dnsCache.getCacheStats();
    }
}

