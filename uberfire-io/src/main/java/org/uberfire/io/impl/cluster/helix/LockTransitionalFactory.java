package org.uberfire.io.impl.cluster.helix;

import java.util.concurrent.Semaphore;

import org.apache.helix.participant.statemachine.StateModelFactory;

public class LockTransitionalFactory extends StateModelFactory<LockTransitionModel> {

    private final Semaphore locked;
    private final Semaphore unlocked;

    LockTransitionalFactory( final Semaphore locked,
                             final Semaphore unlocked ) {
        this.locked = locked;
        this.unlocked = unlocked;
    }

    @Override
    public LockTransitionModel createNewStateModel( final String lockName ) {
        return new LockTransitionModel( lockName, locked, unlocked );
    }
}
