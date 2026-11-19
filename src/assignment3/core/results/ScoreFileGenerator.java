package assignment3.core.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates synthetic score records simulating 10,000 student result files
 * with controlled failure and corruption vectors for stress-testing.
 */
public class ScoreFileGenerator {

    private static final String[] FIRST_NAMES = {"Aarav", "Pooja", "Vikram", "Sneha", "Karan", "Ananya", "Rohan", "Meera", "Kabir", "Neha"};
    private static final String[] LAST_NAMES = {"Sharma", "Verma", "Iyer", "Patel", "Singh", "Reddy", "Gupta", "Das", "Menon", "Joshi"};
    private static final String[] SUBJECTS = {"CS101", "CS102", "MATH201", "PHYS101", "ENG105"};

    public static class SyntheticRecord {
        private final String fileId;
        private final String payload;

        public SyntheticRecord(String fileId, String payload) {
            this.fileId = fileId;
            this.payload = payload;
        }

        public String getFileId() {
            return fileId;
        }

        public String getPayload() {
            return payload;
        }
    }

    public static List<SyntheticRecord> generateDataset(int totalCount) {
        List<SyntheticRecord> records = new ArrayList<>(totalCount);
        Random random = new Random(42); // Deterministic seed for reproducible testing

        for (int i = 1; i <= totalCount; i++) {
            String fileId = String.format("SCORE-FILE-%05d.dat", i);
            int rollInt = 10000 + i;
            String rollNo = "24-CS-" + rollInt;
            String name = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[random.nextInt(LAST_NAMES.length)];

            // Introduce 5% controlled anomalies/corruptions:
            int anomalyType = (i % 20 == 0) ? (i % 4) + 1 : 0;

            String payload;
            switch (anomalyType) {
                case 1 -> {
                    // MALFORMED SYNTAX / Missing pipe delimiters
                    payload = rollNo + " " + name + " CS101=85,MATH201=90";
                }
                case 2 -> {
                    // OUT OF BOUNDS MARK (e.g. -25 or 140)
                    double badMark = (random.nextBoolean()) ? -15.0 : 135.0;
                    payload = String.format("%s|%s|CS101:%.1f,MATH201:88.0,PHYS101:76.5", rollNo, name, badMark);
                }
                case 3 -> {
                    // MISSING MANDATORY FIELD (empty roll or name)
                    payload = String.format("|%s|CS101:75.0,MATH201:82.0", name);
                }
                case 4 -> {
                    // NON-NUMERIC SCORE TOKEN
                    payload = String.format("%s|%s|CS101:EXCELLENT,MATH201:80.0", rollNo, name);
                }
                default -> {
                    // VALID RECORD: generate realistic scores between 45 and 99
                    StringBuilder marks = new StringBuilder();
                    for (int s = 0; s < SUBJECTS.length; s++) {
                        double mark = 45.0 + random.nextDouble() * 54.0;
                        marks.append(SUBJECTS[s]).append(":").append(String.format("%.1f", mark));
                        if (s < SUBJECTS.length - 1) marks.append(",");
                    }
                    payload = String.format("%s|%s|%s", rollNo, name, marks);
                }
            }

            records.add(new SyntheticRecord(fileId, payload));
        }

        return records;
    }
}
