package org.testng.internal.thread;

import java.util.concurrent.CountDownLatch;

/**
 * Represents a Thread that would like to use a synchronization mechanism so that the Thread executor
 * can orchestrate a wait for all the threads to run to completion.
 */
public interface IThreadSynchronizer {
    /**
     * @param latch - The {@link CountDownLatch} to be used.
     */
    void setLatch(CountDownLatch latch);
}
