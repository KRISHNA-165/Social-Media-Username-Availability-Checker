import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterApp {

    // 🔹 Token Bucket Class
    static class TokenBucket {
        private int tokens;
        private final int maxTokens;
        private final double refillRatePerSec; // tokens per second
        private long lastRefillTime;

        public TokenBucket(int maxTokens, int refillPerHour) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.refillRatePerSec = refillPerHour / 3600.0;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // 🔹 Refill tokens based on elapsed time
        private void refill() {
            long now = System.currentTimeMillis();
            double seconds = (now - lastRefillTime) / 1000.0;

            int tokensToAdd = (int) (seconds * refillRatePerSec);

            if (tokensToAdd > 0) {
                tokens = Math.min(maxTokens, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }

        // 🔹 Try consuming a token
        public synchronized boolean allowRequest() {
            refill();

            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        // 🔹 Remaining tokens
        public int getRemainingTokens() {
            refill();
            return tokens;
        }

        // 🔹 Time until next token (in seconds)
        public long getRetryAfterSeconds() {
            if (tokens > 0) return 0;

            return (long) Math.ceil(1 / refillRatePerSec);
        }
    }

    // 🔹 Rate Limiter Class
    static class RateLimiter {

        // clientId → TokenBucket
        private Map<String, TokenBucket> clientBuckets;

        private final int MAX_TOKENS = 1000;

        public RateLimiter() {
            clientBuckets = new ConcurrentHashMap<>();
        }

        // 🔹 Check Rate Limit (O(1))
        public String checkRateLimit(String clientId) {

            TokenBucket bucket = clientBuckets.computeIfAbsent(
                    clientId,
                    id -> new TokenBucket(MAX_TOKENS, MAX_TOKENS)
            );

            if (bucket.allowRequest()) {
                return "Allowed (" + bucket.getRemainingTokens() +
                        " requests remaining)";
            } else {
                return "Denied (0 requests remaining, retry after " +
                        bucket.getRetryAfterSeconds() + "s)";
            }
        }

        // 🔹 Get Status
        public String getRateLimitStatus(String clientId) {
            TokenBucket bucket = clientBuckets.get(clientId);

            if (bucket == null) {
                return "{used: 0, limit: 1000, reset: N/A}";
            }

            int remaining = bucket.getRemainingTokens();
            int used = MAX_TOKENS - remaining;

            return "{used: " + used +
                    ", limit: " + MAX_TOKENS +
                    ", remaining: " + remaining + "}";
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) {

        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        // 🔹 Simulate requests
        for (int i = 1; i <= 1005; i++) {
            String result = limiter.checkRateLimit(client);

            if (i <= 5 || i > 995) { // print few samples
                System.out.println("Request " + i + ": " + result);
            }
        }

        // 🔹 Status
        System.out.println("\nRate Limit Status:");
        System.out.println(limiter.getRateLimitStatus(client));
    }
}

