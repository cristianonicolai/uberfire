package org.uberfire.io.attribute;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uberfire.io.IOService;
import org.uberfire.io.impl.IOServiceDotFileImpl;
import org.uberfire.java.nio.base.version.VersionAttributeView;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.OpenOption;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.attribute.FileAttribute;

import static org.junit.Assert.*;

/**
 *
 */
public class DotFileAttrViewTest {

    protected static final List<File> tempFiles = new ArrayList<File>();

    private static boolean created = false;

    @Test
    public void testDotFileAttrAccess() throws IOException {
        final URI newRepo = URI.create( "git://" + new Date().getTime() + "-repo-test" );
        ioService().newFileSystem( newRepo, new HashMap<String, Object>() );

        final Path dir = ioService().get( newRepo );
        final Path file = dir.resolve( "myFile.txt" );

        ioService().write( file, "mycontent", Collections.<OpenOption>emptySet() );

        {
            final DublinCoreView view = ioService().getFileAttributeView( file, DublinCoreView.class );

            assertNotNull( view );

            assertNotNull( view.readAttributes() );

            assertNotNull( view.readAttributes().languages() );

            assertEquals( 0, view.readAttributes().languages().size() );
        }

        ioService().write( file, "mycontent", Collections.<OpenOption>emptySet(), new FileAttribute<Object>() {
                               @Override
                               public String name() {
                                   return "dcore.creator";
                               }

                               @Override
                               public Object value() {
                                   return "some user name here";
                               }
                           }, new FileAttribute<Object>() {
                               @Override
                               public String name() {
                                   return "dcore.language[0]";
                               }

                               @Override
                               public Object value() {
                                   return "en";
                               }
                           }, new FileAttribute<Object>() {
                               @Override
                               public String name() {
                                   return "dcore.language[1]";
                               }

                               @Override
                               public Object value() {
                                   return "pt-BR";
                               }
                           }
                         );

        {
            final DublinCoreView view = ioService().getFileAttributeView( file, DublinCoreView.class );

            assertNotNull( view );

            assertNotNull( view.readAttributes() );

            assertNotNull( view.readAttributes().languages() );

            assertEquals( 2, view.readAttributes().languages().size() );

            assertTrue( view.readAttributes().languages().contains( "pt-BR" ) );

            assertTrue( view.readAttributes().languages().contains( "en" ) );

            assertEquals( 1, view.readAttributes().creators().size() );

            assertTrue( view.readAttributes().creators().contains( "some user name here" ) );
        }

        final Path dotFile = file.getParent().resolve( ".myFile.txt" );
        assertTrue( Files.exists( dotFile ) );

        final VersionAttributeView attrs = Files.getFileAttributeView( dotFile, VersionAttributeView.class );
        assertEquals( 1, attrs.readAttributes().history().records().size() );

        final Map<String, Object> result = Files.readAttributes( dotFile, "*" );
        assertNotNull( result );
    }

    protected static IOService ioService = null;

    public IOService ioService() {
        if ( ioService == null ) {
            ioService = new IOServiceDotFileImpl();
        }
        return ioService;
    }

    @Before
    public void setup() throws IOException {
        if ( !created ) {
            final String path = createTempDirectory().getAbsolutePath();
            System.setProperty( "org.uberfire.nio.git.dir", path );
            System.out.println( ".niogit: " + path );

            final URI newRepo = URI.create( "git://repo-test" );

            try {
                ioService().newFileSystem( newRepo, new HashMap<String, Object>() );
            } catch ( final Exception ex ) {
            } finally {
                created = true;
            }
        }
    }

    @After
    public void tearDown() {
        // dispose the IOService or it will badly influence the tests executed after
        ioService.dispose();
    }

    @AfterClass
    @BeforeClass
    public static void cleanup() {
        for ( final File tempFile : tempFiles ) {
            FileUtils.deleteQuietly( tempFile );
        }
    }

    public static File createTempDirectory()
            throws IOException {
        final File temp = File.createTempFile( "temp", Long.toString( System.nanoTime() ) );
        if ( !( temp.delete() ) ) {
            throw new IOException( "Could not delete temp file: " + temp.getAbsolutePath() );
        }

        if ( !( temp.mkdir() ) ) {
            throw new IOException( "Could not create temp directory: " + temp.getAbsolutePath() );
        }

        tempFiles.add( temp );

        return temp;
    }

}
