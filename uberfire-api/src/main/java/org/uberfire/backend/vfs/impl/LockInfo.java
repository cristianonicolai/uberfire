package org.uberfire.backend.vfs.impl;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.uberfire.backend.vfs.Path;

/**
 * Holds lock information about a {@link Path}.
 */
@Portable
public class LockInfo {

    private final boolean locked;
    private final String lockedBy;
    private final Path file;
    private final transient Path lock;

    public LockInfo( @MapsTo("locked") boolean locked,
                     @MapsTo("lockedBy") String lockedBy,
                     @MapsTo("file") Path file ) {

        this( locked,
              lockedBy,
              file,
              null );
    }

    public LockInfo( boolean locked,
                     String lockedBy,
                     Path file,
                     Path lock ) {

        this.locked = locked;
        this.lockedBy = lockedBy;
        this.file = file;
        this.lock = lock;
    }
    
    public static LockInfo unlocked() {
        return new LockInfo(false, null, null);
    }

    public boolean isLocked() {
        return locked;
    }

    public String lockedBy() {
        return lockedBy;
    }

    public Path getFile() {
        return file;
    }

    public Path getLock() {
        return lock;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + (locked ? 1231 : 1237);
        result = prime * result + ((lockedBy == null) ? 0 : lockedBy.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        LockInfo other = (LockInfo) obj;
        if ( file == null ) {
            if ( other.file != null )
                return false;
        } else if ( !file.equals( other.file ) )
            return false;
        if ( locked != other.locked )
            return false;
        if ( lockedBy == null ) {
            if ( other.lockedBy != null )
                return false;
        } else if ( !lockedBy.equals( other.lockedBy ) )
            return false;
        return true;
    }

}
