package assignment3.core.sales;

import java.io.Closeable;
import java.io.IOException;

/**
 * A custom AutoCloseable resource used to simulate I/O operations and
 * demonstrate suppressed exceptions during automated closing.
 */
public class FaultyResource implements Closeable {

    private final String resourceName;
    private final String trackerHandle;
    private final boolean throwOnRead;
    private final boolean throwOnClose;
    private boolean closed = false;

    public FaultyResource(String resourceName, boolean throwOnRead, boolean throwOnClose) {
        this.resourceName = resourceName;
        this.throwOnRead = throwOnRead;
        this.throwOnClose = throwOnClose;
        this.trackerHandle = ResourceTracker.registerResource(resourceName);
    }

    public void processData() throws IOException {
        if (closed) {
            throw new IllegalStateException("Cannot read from closed resource: " + resourceName);
        }
        if (throwOnRead) {
            throw new IOException("I/O failure during data streaming from: " + resourceName);
        }
        System.out.println("   [FaultyResource] Data streamed successfully from: " + resourceName);
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        ResourceTracker.releaseResource(trackerHandle);

        if (throwOnClose) {
            throw new IOException("Resource closing failure: Write-flush failed on underlying device for " + resourceName);
        }
    }

    public boolean isClosed() {
        return closed;
    }
}
