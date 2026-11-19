package assignment3.core.wallet.mess;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Producer runnable representing a mess staff member issuing dining tokens.
 */
public class TokenProducer implements Runnable {

    private static final AtomicInteger TOKEN_SEQ = new AtomicInteger(100);
    private final MessTokenCounterBuffer buffer;
    private final String staffName;
    private final int tokensToProduce;

    public TokenProducer(MessTokenCounterBuffer buffer, String staffName, int tokensToProduce) {
        this.buffer = buffer;
        this.staffName = staffName;
        this.tokensToProduce = tokensToProduce;
    }

    @Override
    public void run() {
        for (int i = 1; i <= tokensToProduce; i++) {
            try {
                String tokenId = "TKN-" + TOKEN_SEQ.incrementAndGet();
                MessToken token = new MessToken(tokenId, "LUNCH_SPECIAL", staffName);
                
                // Sleep slightly to simulate kitchen prep latency
                Thread.sleep(20);

                buffer.putToken(token);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
