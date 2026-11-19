package assignment3.core.wallet.mess;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumer runnable representing a student queuing and consuming mess meal tokens.
 */
public class TokenConsumer implements Runnable {

    private final MessTokenCounterBuffer buffer;
    private final String studentName;
    private final int tokensToConsume;
    private final AtomicInteger consumedCounter;

    public TokenConsumer(MessTokenCounterBuffer buffer, String studentName, 
                         int tokensToConsume, AtomicInteger consumedCounter) {
        this.buffer = buffer;
        this.studentName = studentName;
        this.tokensToConsume = tokensToConsume;
        this.consumedCounter = consumedCounter;
    }

    @Override
    public void run() {
        for (int i = 1; i <= tokensToConsume; i++) {
            try {
                // Sleep slightly to simulate student arrival interval
                Thread.sleep(35);

                MessToken token = buffer.takeToken();
                if (token != null) {
                    consumedCounter.incrementAndGet();
                } else {
                    break;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
