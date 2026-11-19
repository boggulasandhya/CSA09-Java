package assignment3.core.wallet.mess;

import java.time.LocalDateTime;

/**
 * Domain entity representing a digitized mess dining token.
 */
public class MessToken {
    private final String tokenId;
    private final String mealType;
    private final String issuedByStaff;
    private final LocalDateTime issuedAt;

    public MessToken(String tokenId, String mealType, String issuedByStaff) {
        this.tokenId = tokenId;
        this.mealType = mealType;
        this.issuedByStaff = issuedByStaff;
        this.issuedAt = LocalDateTime.now();
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getMealType() {
        return mealType;
    }

    public String getIssuedByStaff() {
        return issuedByStaff;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (Staff: %s)]", tokenId, mealType, issuedByStaff);
    }
}
