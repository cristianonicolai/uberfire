package org.uberfire.io.impl.cluster.helix;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.helix.Criteria;
import org.apache.helix.HelixManager;
import org.apache.helix.InstanceType;
import org.apache.helix.NotificationContext;
import org.apache.helix.messaging.handling.HelixTaskResult;
import org.apache.helix.messaging.handling.MessageHandler;
import org.apache.helix.messaging.handling.MessageHandlerFactory;
import org.apache.helix.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.cluster.ClusterService;
import org.uberfire.commons.data.Pair;
import org.uberfire.commons.message.AsyncCallback;
import org.uberfire.commons.message.MessageHandlerResolver;
import org.uberfire.commons.message.MessageType;
import org.uberfire.io.impl.cluster.ClusterMessageType;

import static java.util.Arrays.*;
import static java.util.UUID.*;
import static org.apache.helix.HelixManagerFactory.*;

public class ClusterServiceHelix implements ClusterService {

    private static final Logger logger = LoggerFactory.getLogger( ClusterServiceHelix.class );

    private final String clusterName;
    private final String instanceName;
    private final HelixManager participantManager;
    private final String resourceName;
    private final Map<String, MessageHandlerResolver> messageHandlerResolver = new ConcurrentHashMap<String, MessageHandlerResolver>();
    private final Collection<Runnable> onStart = new CopyOnWriteArrayList<Runnable>();

    private final AtomicBoolean isStarted = new AtomicBoolean( false );

    private final Semaphore helixLocked = new Semaphore( 1, true );
    private final Semaphore helixUnlocked = new Semaphore( 1, true );

    private final ReentrantLock lock = new ReentrantLock( true );

    public ClusterServiceHelix( final String clusterName,
                                final String zkAddress,
                                final String instanceName,
                                final String resourceName,
                                final MessageHandlerResolver messageHandlerResolver ) {
        this.clusterName = clusterName;
        this.instanceName = instanceName;
        this.resourceName = resourceName;
        this.messageHandlerResolver.put( messageHandlerResolver.getServiceId(), messageHandlerResolver );

        this.participantManager = getZKHelixManager( clusterName, instanceName, InstanceType.PARTICIPANT, zkAddress );
    }

    //TODO {porcelli} quick hack for now, the real solution would have a cluster per repo
    @Override
    public void addMessageHandlerResolver( final MessageHandlerResolver resolver ) {
        this.messageHandlerResolver.put( resolver.getServiceId(), resolver );
    }

    @Override
    public void start() {
        if ( isStarted.get() ) {
            return;
        }
        try {
            this.participantManager.getMessagingService().registerMessageHandlerFactory( Message.MessageType.USER_DEFINE_MSG.toString(), new MessageHandlerResolverWrapper().convert() );
            this.participantManager.connect();
            internalDisablePartition();
            this.participantManager.getStateMachineEngine().registerStateModelFactory( "LeaderStandby", new LockTransitionalFactory( helixLocked, helixUnlocked ) );
            this.isStarted.set( true );
            for ( final Runnable runnable : onStart ) {
                runnable.run();
            }
        } catch ( final Exception ex ) {
            throw new RuntimeException( ex );
        }
    }

    private void waitForStart() {
        while ( !isStarted.get() ) {
            try {
                Thread.sleep( 10 );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispose() {
        if ( this.participantManager != null && this.participantManager.isConnected() ) {
            this.participantManager.disconnect();
        }
    }

    @Override
    public void onStart( final Runnable runnable ) {
        if ( isStarted.get() ) {
            runnable.run();
        } else {
            onStart.add( runnable );
        }
    }

    @Override
    public boolean isInnerLocked() {
        return lock.getHoldCount() > 1;
    }

    private void enablePartition() {
        try {
            helixLocked.acquire();
            participantManager.getClusterManagmentTool().enablePartition( true, clusterName, instanceName, resourceName, asList( resourceName + "_0" ) );
            helixLocked.acquire();
        } catch ( InterruptedException e ) {
            if ( helixLocked.availablePermits() > 0 ) {
                throw new IllegalStateException( "Couldn't enable Helix partition." );
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        } finally {
            if ( helixUnlocked.availablePermits() == 0 ) {
                helixUnlocked.release();
            }
        }
    }

    private void disablePartition() {
        try {
            helixUnlocked.acquire();
            internalDisablePartition();
            helixUnlocked.acquire();
        } catch ( final InterruptedException e ) {
            if ( helixUnlocked.availablePermits() > 0 ) {
                throw new IllegalStateException( "Couldn't disable Helix partition." );
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        } finally {
            if ( helixLocked.availablePermits() == 0 ) {
                helixLocked.release();
            }
        }
    }

    private void internalDisablePartition() {
        participantManager.getClusterManagmentTool().enablePartition( false, clusterName, instanceName, resourceName, asList( resourceName + "_0" ) );
    }

    @Override
    public void lock() {
        waitForStart();

        lock.lock();

        if ( lock.getHoldCount() > 1 ) {
            return;
        }

        enablePartition();
    }

    @Override
    public void unlock() {
        waitForStart();

        if ( lock.getHoldCount() > 1 ) {
            lock.unlock();
            return;
        }

        disablePartition();
        lock.unlock();
    }

    @Override
    public boolean isLocked() {
        waitForStart();

        return lock.isLocked();
    }

    @Override
    public void broadcastAndWait( final String serviceId,
                                  final MessageType type,
                                  final Map<String, String> content,
                                  int timeOut ) {
        waitForStart();

        participantManager.getMessagingService().sendAndWait( buildCriteria(), buildMessage( serviceId, type, content ), new org.apache.helix.messaging.AsyncCallback( timeOut ) {
            @Override
            public void onTimeOut() {
            }

            @Override
            public void onReplyMessage( final Message message ) {
            }
        }, timeOut );
    }

    @Override
    public void broadcastAndWait( final String serviceId,
                                  final MessageType type,
                                  final Map<String, String> content,
                                  final int timeOut,
                                  final AsyncCallback callback ) {
        waitForStart();

        int msg = participantManager.getMessagingService().sendAndWait( buildCriteria(), buildMessage( serviceId, type, content ), new org.apache.helix.messaging.AsyncCallback() {
            @Override
            public void onTimeOut() {
                callback.onTimeOut();
            }

            @Override
            public void onReplyMessage( final Message message ) {
                final MessageType type = buildMessageTypeFromReply( message );
                final Map<String, String> map = getMessageContentFromReply( message );

                callback.onReply( type, map );
            }
        }, timeOut );
        if ( msg == 0 ) {
            callback.onTimeOut();
        }
    }

    @Override
    public void broadcast( final String serviceId,
                           final MessageType type,
                           final Map<String, String> content ) {
        waitForStart();

        participantManager.getMessagingService().send( buildCriteria(), buildMessage( serviceId, type, content ) );
    }

    @Override
    public void broadcast( final String serviceId,
                           final MessageType type,
                           final Map<String, String> content,
                           final int timeOut,
                           final AsyncCallback callback ) {
        waitForStart();

        participantManager.getMessagingService().send( buildCriteria(), buildMessage( serviceId, type, content ), new org.apache.helix.messaging.AsyncCallback() {
            @Override
            public void onTimeOut() {
                callback.onTimeOut();
            }

            @Override
            public void onReplyMessage( final Message message ) {
                final MessageType type = buildMessageTypeFromReply( message );
                final Map<String, String> map = getMessageContent( message );

                callback.onReply( type, map );
            }
        }, timeOut );
    }

    @Override
    public void sendTo( final String serviceId,
                        final String resourceId,
                        final MessageType type,
                        final Map<String, String> content ) {
        waitForStart();

        participantManager.getMessagingService().send( buildCriteria( resourceId ), buildMessage( serviceId, type, content ) );
    }

    private Criteria buildCriteria( final String resourceId ) {
        return new Criteria() {{
            setInstanceName( resourceId );
            setRecipientInstanceType( InstanceType.PARTICIPANT );
            setResource( resourceName );
            setSelfExcluded( true );
            setSessionSpecific( true );
        }};
    }

    private Criteria buildCriteria() {
        return buildCriteria( "%" );
    }

    private Message buildMessage( final String serviceId,
                                  final MessageType type,
                                  final Map<String, String> content ) {
        return new Message( Message.MessageType.USER_DEFINE_MSG, randomUUID().toString() ) {{
            setMsgState( Message.MessageState.NEW );
            getRecord().setMapField( "content", content );
            getRecord().setSimpleField( "serviceId", serviceId );
            getRecord().setSimpleField( "type", type.toString() );
            getRecord().setSimpleField( "origin", instanceName );
        }};
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE + 200;
    }

    class MessageHandlerResolverWrapper {

        MessageHandlerFactory convert() {
            return new MessageHandlerFactory() {

                @Override
                public MessageHandler createHandler( final Message message,
                                                     final NotificationContext context ) {

                    return new MessageHandler( message, context ) {
                        @Override
                        public HelixTaskResult handleMessage() throws InterruptedException {
                            try {
                                final String serviceId = _message.getRecord().getSimpleField( "serviceId" );
                                final MessageType type = buildMessageType( _message.getRecord().getSimpleField( "type" ) );
                                final Map<String, String> map = getMessageContent( _message );

                                final MessageHandlerResolver resolver = messageHandlerResolver.get( serviceId );

                                if ( resolver == null ) {
                                    System.err.println( "serviceId not found '" + serviceId + "'" );
                                    return new HelixTaskResult() {{
                                        setSuccess( false );
                                        setMessage( "Can't find resolver" );
                                    }};
                                }

                                final org.uberfire.commons.message.MessageHandler handler = resolver.resolveHandler( serviceId, type );

                                if ( handler == null ) {
                                    System.err.println( "handler not found for '" + serviceId + "' and type '" + type.toString() + "'" );
                                    return new HelixTaskResult() {{
                                        setSuccess( false );
                                        setMessage( "Can't find handler." );
                                    }};
                                }

                                final Pair<MessageType, Map<String, String>> result = handler.handleMessage( type, map );

                                if ( result == null ) {
                                    return new HelixTaskResult() {{
                                        setSuccess( true );
                                    }};
                                }

                                return new HelixTaskResult() {{
                                    setSuccess( true );
                                    getTaskResultMap().put( "serviceId", serviceId );
                                    getTaskResultMap().put( "type", result.getK1().toString() );
                                    getTaskResultMap().put( "origin", instanceName );
                                    for ( Map.Entry<String, String> entry : result.getK2().entrySet() ) {
                                        getTaskResultMap().put( entry.getKey(), entry.getValue() );
                                    }
                                }};
                            } catch ( final Throwable e ) {
                                logger.error( "Error while processing cluster message", e );
                                return new HelixTaskResult() {{
                                    setSuccess( false );
                                    setMessage( e.getMessage() );
                                    setException( new RuntimeException( e ) );
                                }};
                            }
                        }

                        @Override
                        public void onError( final Exception e,
                                             final ErrorCode code,
                                             final ErrorType type ) {
                        }
                    };
                }

                @Override
                public String getMessageType() {
                    return Message.MessageType.USER_DEFINE_MSG.toString();
                }

                @Override
                public void reset() {
                }
            };
        }
    }

    private MessageType buildMessageType( final String _type ) {
        if ( _type == null ) {
            return null;
        }

        MessageType type;
        try {
            type = ClusterMessageType.valueOf( _type );
        } catch ( Exception ex ) {
            type = new MessageType() {
                @Override
                public String toString() {
                    return _type;
                }

                @Override
                public int hashCode() {
                    return _type.hashCode();
                }
            };
        }

        return type;
    }

    private MessageType buildMessageTypeFromReply( Message message ) {
        final Map<String, String> result = message.getRecord().getMapField( Message.Attributes.MESSAGE_RESULT.toString() );
        return buildMessageType( result.get( "type" ) );
    }

    private Map<String, String> getMessageContent( final Message message ) {
        return message.getRecord().getMapField( "content" );
    }

    private Map<String, String> getMessageContentFromReply( final Message message ) {
        return new HashMap<String, String>() {{
            for ( final Map.Entry<String, String> field : message.getRecord().getMapField( Message.Attributes.MESSAGE_RESULT.toString() ).entrySet() ) {
                if ( !field.getKey().equals( "serviceId" ) && !field.getKey().equals( "origin" ) && !field.getKey().equals( "type" ) ) {
                    put( field.getKey(), field.getValue() );
                }
            }
        }};
    }
}