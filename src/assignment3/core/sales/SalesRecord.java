package assignment3.core.sales;

/**
 * Domain entity representing a sales record parsed from daily report data.
 */
public class SalesRecord {
    private final String transactionId;
    private final String branchCode;
    private final String category;
    private final double amount;
    private final int unitsSold;

    public SalesRecord(String transactionId, String branchCode, String category, double amount, int unitsSold) {
        this.transactionId = transactionId;
        this.branchCode = branchCode;
        this.category = category;
        this.amount = amount;
        this.unitsSold = unitsSold;
    }

    public static SalesRecord parseFromCsv(String csvLine) {
        String[] tokens = csvLine.split(",");
        if (tokens.length < 5) {
            throw new IllegalArgumentException("Invalid CSV record format: " + csvLine);
        }
        return new SalesRecord(
                tokens[0].trim(),
                tokens[1].trim(),
                tokens[2].trim(),
                Double.parseDouble(tokens[3].trim()),
                Integer.parseInt(tokens[4].trim())
        );
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public int getUnitsSold() {
        return unitsSold;
    }

    @Override
    public String toString() {
        return String.format("TX[%s | Branch: %s | Cat: %s | Amount: $%.2f | Units: %d]",
                transactionId, branchCode, category, amount, unitsSold);
    }
}
