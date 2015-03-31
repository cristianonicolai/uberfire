package org.uberfire.backend.server.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.uberfire.commons.async.SimpleAsyncExecutorService;
import org.uberfire.commons.cluster.ClusterServiceFactory;
import org.uberfire.commons.lifecycle.PriorityDisposable;

public class DisposableShutdownService implements ServletContextListener {

    @Inject
    private Instance<PriorityDisposable> disposables;

    @Inject
    @Named("clusterServiceFactory")
    private ClusterServiceFactory clusterServiceFactory;

    @Override
    public void contextInitialized( ServletContextEvent sce ) {

    }

    @Override
    public void contextDestroyed( final ServletContextEvent sce ) {
        final ArrayList<PriorityDisposable> collection = new ArrayList<PriorityDisposable>();
        for ( final PriorityDisposable disposable : disposables ) {
            collection.add( disposable );
        }

        if ( clusterServiceFactory != null ) {
            //TODO: hack that should be changed soon;
            collection.add( clusterServiceFactory.build( null ) );
        }

        Collections.sort( collection, new Comparator<PriorityDisposable>() {
            @Override
            public int compare( final PriorityDisposable o1,
                                final PriorityDisposable o2 ) {
                return ( o2.priority() < o1.priority() ) ? -1 : ( ( o2.priority() == o1.priority() ) ? 0 : 1 );
            }
        } );

        for ( final PriorityDisposable disposable : collection ) {
            disposable.dispose();
        }

        SimpleAsyncExecutorService.shutdownInstances();
    }
}
