package org.uberfire.io.lock;

import java.util.concurrent.locks.ReentrantLock;

import org.uberfire.java.nio.file.FileSystem;

public class BatchLockControl {

    private final ReentrantLock lock = new ReentrantLock( true );

    public void lock( final FileSystem... fileSystems ) {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public boolean hasQueuedThreads() {
        return lock.hasQueuedThreads();
    }

    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }
}
