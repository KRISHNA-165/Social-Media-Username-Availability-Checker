
import java.util.*;

public class PlagiarismDetectorApp {

    // 🔹 Plagiarism Detector Class
    static class PlagiarismDetector {

        // n-gram → set of document IDs
        private Map<String, Set<String>> ngramIndex;

        // document → its n-grams
        private Map<String, List<String>> documentNgrams;

        private int N = 5; // 5-gram

        public PlagiarismDetector() {
            ngramIndex = new HashMap<>();
            documentNgrams = new HashMap<>();
        }

        // 🔹 Add document to database
        public void addDocument(String docId, String text) {
            List<String> ngrams = generateNgrams(text);
            documentNgrams.put(docId, ngrams);

            for (String gram : ngrams) {
                ngramIndex
                        .computeIfAbsent(gram, k -> new HashSet<>())
                        .add(docId);
            }
        }

        // 🔹 Analyze a new document
        public void analyzeDocument(String docId, String text) {

            List<String> ngrams = generateNgrams(text);
            System.out.println("Analyzing: " + docId);
            System.out.println("Extracted " + ngrams.size() + " n-grams");

            // Count matches per document
            Map<String, Integer> matchCount = new HashMap<>();

            for (String gram : ngrams) {
                if (ngramIndex.containsKey(gram)) {
                    for (String existingDoc : ngramIndex.get(gram)) {
                        matchCount.put(existingDoc,
                                matchCount.getOrDefault(existingDoc, 0) + 1);
                    }
                }
            }

            // Calculate similarity
            for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
                String existingDoc = entry.getKey();
                int matches = entry.getValue();

                int total = ngrams.size();

                double similarity = (matches * 100.0) / total;

                System.out.println("→ Found " + matches +
                        " matching n-grams with \"" + existingDoc + "\"");

                System.out.printf("→ Similarity: %.2f%% ", similarity);

                if (similarity > 60) {
                    System.out.println("(PLAGIARISM DETECTED)");
                } else if (similarity > 15) {
                    System.out.println("(Suspicious)");
                } else {
                    System.out.println("(Safe)");
                }
            }
        }

        // 🔹 Generate n-grams
        private List<String> generateNgrams(String text) {
            List<String> result = new ArrayList<>();

            String[] words = text.toLowerCase().split("\\s+");

            for (int i = 0; i <= words.length - N; i++) {
                StringBuilder gram = new StringBuilder();

                for (int j = 0; j < N; j++) {
                    gram.append(words[i + j]).append(" ");
                }

                result.add(gram.toString().trim());
            }

            return result;
        }
    }

    // 🔹 Main Method
    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        // 🔹 Add existing documents
        String doc1 = "data structures and algorithms are important for programming and problem solving";
        String doc2 = "machine learning and data science are popular fields in computer science";

        detector.addDocument("essay_089.txt", doc1);
        detector.addDocument("essay_092.txt", doc2);

        // 🔹 New document to analyze
        String newDoc = "data structures and algorithms are important for programming and problem solving in computer science";

        detector.analyzeDocument("essay_123.txt", newDoc);
    }
}
