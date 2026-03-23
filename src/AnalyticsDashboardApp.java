import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnalyticsDashboardApp {

    // 🔹 Event Class
    static class PageViewEvent {
        String url;
        String userId;
        String source;

        PageViewEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // 🔹 Analytics System
    static class AnalyticsSystem {

        // Page → total visits
        private Map<String, Integer> pageViews;

        // Page → unique users
        private Map<String, Set<String>> uniqueVisitors;

        // Source → count
        private Map<String, Integer> trafficSources;

        public AnalyticsSystem() {
            pageViews = new ConcurrentHashMap<>();
            uniqueVisitors = new ConcurrentHashMap<>();
            trafficSources = new ConcurrentHashMap<>();

            startDashboardUpdater();
        }

        // 🔹 Process incoming event (O(1))
        public void processEvent(PageViewEvent event) {

            // Update page views
            pageViews.put(event.url,
                    pageViews.getOrDefault(event.url, 0) + 1);

            // Update unique visitors
            uniqueVisitors
                    .computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                    .add(event.userId);

            // Update traffic sources
            trafficSources.put(event.source,
                    trafficSources.getOrDefault(event.source, 0) + 1);
        }

        // 🔹 Get Top 10 Pages
        private List<Map.Entry<String, Integer>> getTopPages() {
            PriorityQueue<Map.Entry<String, Integer>> pq =
                    new PriorityQueue<>(Map.Entry.comparingByValue());

            for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
                pq.offer(entry);
                if (pq.size() > 10) {
                    pq.poll();
                }
            }

            List<Map.Entry<String, Integer>> result = new ArrayList<>();
            while (!pq.isEmpty()) {
                result.add(pq.poll());
            }

            Collections.reverse(result);
            return result;
        }

        // 🔹 Display Dashboard
        public void getDashboard() {
            System.out.println("\n===== REAL-TIME DASHBOARD =====");

            // Top Pages
            System.out.println("\nTop Pages:");
            List<Map.Entry<String, Integer>> topPages = getTopPages();

            int rank = 1;
            for (Map.Entry<String, Integer> entry : topPages) {
                String url = entry.getKey();
                int views = entry.getValue();
                int unique = uniqueVisitors.get(url).size();

                System.out.println(rank + ". " + url +
                        " - " + views + " views (" + unique + " unique)");
                rank++;
            }

            // Traffic Sources
            System.out.println("\nTraffic Sources:");
            int total = trafficSources.values().stream().mapToInt(i -> i).sum();

            for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
                double percent = (entry.getValue() * 100.0) / total;

                System.out.printf("%s: %.2f%%\n",
                        entry.getKey(), percent);
            }
        }

        // 🔹 Auto-update every 5 seconds
        private void startDashboardUpdater() {
            Thread dashboardThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    getDashboard();
                }
            });

            dashboardThread.setDaemon(true);
            dashboardThread.start();
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) throws InterruptedException {

        AnalyticsSystem system = new AnalyticsSystem();

        // Simulated events
        String[] urls = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai-update",
                "/health/tips"
        };

        String[] sources = {"google", "facebook", "direct", "twitter"};

        Random random = new Random();

        // Simulate real-time traffic
        for (int i = 1; i <= 100; i++) {

            String url = urls[random.nextInt(urls.length)];
            String userId = "user_" + random.nextInt(50);
            String source = sources[random.nextInt(sources.length)];

            system.processEvent(new PageViewEvent(url, userId, source));

            Thread.sleep(100); // simulate incoming traffic
        }

        // Let dashboard print a few times
        Thread.sleep(15000);
    }
}
