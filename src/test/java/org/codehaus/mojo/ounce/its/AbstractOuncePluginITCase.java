/*
 * Copyright (c) 2007, Ounce Labs, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY OUNCE LABS, INC. ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL OUNCE LABS, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.codehaus.mojo.ounce.its;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.test.plugin.BuildTool;
import org.apache.maven.shared.test.plugin.PluginTestTool;
import org.apache.maven.shared.test.plugin.ProjectTool;
import org.apache.maven.shared.test.plugin.TestToolsException;
//import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.mojo.ounce.utils.Utils;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a> 
 * @version $Id: AbstractOuncePluginITCase.java 6588 2008-03-28 12:22:57Z bentmann $
 */
public abstract class AbstractOuncePluginITCase
    extends PlexusTestCase
{

    private BuildTool buildTool;

    private ProjectTool projectTool;

    /**
     * Test repository directory.
     */
    protected static File localRepositoryDirectory = getTestFile( "target/test-classes/m2repo" );

    /**
     * Pom File
     */
    protected static File PomFile = new File( getBasedir(), "pom.xml" );

    /**
     * Group-Id for running test builds.
     */
    protected static final String GROUP_ID = "org.codehaus.mojo";

    /**
     * Artifact-Id for running test builds.
     */
    protected static final String ARTIFACT_ID = "ounce-maven-plugin";

    /**
     * Version under which the plugin was installed to the test-time local repository for running test builds.
     */
    protected static final String VERSION = "test";

    private static final String BUILD_OUTPUT_DIRECTORY = "target/surefire-reports/build-output";

    private static boolean installed = false;

    Properties props = new Properties();

    /**
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        if ( !installed )
        {
            System.out.println( "*** Running integation test builds; output will be directed to: " +
                BUILD_OUTPUT_DIRECTORY + "\n" );
        }

        props.setProperty( "ounce.core", "test-xml" );

        super.setUp();

        buildTool = (BuildTool) lookup( BuildTool.ROLE, "default" );

        projectTool = (ProjectTool) lookup( ProjectTool.ROLE, "default" );

        String mavenHome = System.getProperty( "maven.home" );

        // maven.home is set by surefire when the test is
        // run with maven, but better make the test run in
        // IDEs without
        // the need of additional properties
        if ( mavenHome == null )
        {
            String path = System.getProperty( "java.library.path" );
            String[] paths = StringUtils.split( path, System.getProperty( "path.separator" ) );
            for ( int j = 0; j < paths.length; j++ )
            {
                String pt = paths[j];
                if ( new File( pt, "mvn" ).exists() )
                {
                    System.setProperty( "maven.home", new File( pt ).getAbsoluteFile().getParent() );
                    break;
                }

            }
        }

        System.setProperty( "MAVEN_TERMINATE_CMD", "on" );

        synchronized ( AbstractOuncePluginITCase.class )
        {
            if ( !installed )
            {
                PluginTestTool pluginTestTool = (PluginTestTool) lookup( PluginTestTool.ROLE, "default" );

                localRepositoryDirectory =
                    pluginTestTool.preparePluginForUnitTestingWithMavenBuilds( PomFile, "test",
                                                                               localRepositoryDirectory );

                System.out.println( "*** Installed test-version of the Ounce plugin to: " + localRepositoryDirectory +
                    "\n" );

                installed = true;
            }
        }

    }

    /**
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        List containers = new ArrayList();

        containers.add( getContainer() );

        for ( Iterator iter = containers.iterator(); iter.hasNext(); )
        {
            PlexusContainer container = (PlexusContainer) iter.next();

            if ( container != null )
            {
                container.dispose();

                ClassRealm realm = container.getContainerRealm();

                if ( realm != null )
                {
                    realm.getWorld().disposeRealm( realm.getId() );
                }
            }
        }
    }

    /**
     * Execute the plugin with no properties
     * 
     * @param projectName project directory
     * @param goalList comma separated list of goals to execute
     * @throws Exception any exception generated during test
     */
    protected void testProject( String projectName, String goalList )
        throws Exception
    {
        Properties props = new Properties();
        testProject( projectName, props, goalList );
    }

    /**
     * Execute the plugin.
     * 
     * @param projectName project directory
     * @param properties additional properties
     * @param goalList comma separated list of goals to execute
     * @throws Exception any exception generated during test
     */
    protected void testProject( String projectName, Properties properties, String goalList )
        throws Exception
    {
        System.out.println( "Executing Test Project: " + projectName + "..." );

        File theBasedir = getTestFile( "target/test-classes/its/" + projectName );

        File pom = new File( theBasedir, "pom.xml" );

        String[] goal = goalList.split( "," );

        List goals = new ArrayList();

        for ( int i = 0; i < goal.length; i++ )
        {
            goals.add( goal[i] );
        }

        executeMaven( pom, properties, goals );

        MavenProject project = readProject( pom );
        File testOutDir = new File( project.getBuild().getDirectory() );

        compareDirectoryContent( theBasedir, testOutDir, "" );
        System.out.println( "Success!" );
    }

    protected File getOutputDirectory( String projectName )
    {
        return getTestFile( "target/test-classes/projects/" + projectName );
    }

    protected void executeMaven( File pom, Properties properties, List goals )
        throws TestToolsException, ExecutionFailedException
    {
        executeMaven( pom, properties, goals, true );
    }

    protected void executeMaven( File pom, Properties properties, List goals, boolean switchLocalRepo )
        throws TestToolsException, ExecutionFailedException
    {
        // insert the test property to activate the test
        // profile
        properties.setProperty( "test", "true" );
        new File( BUILD_OUTPUT_DIRECTORY ).mkdirs();

        NullPointerException npe = new NullPointerException();
        StackTraceElement[] trace = npe.getStackTrace();

        File buildLog = null;

        for ( int i = 0; i < trace.length; i++ )
        {
            StackTraceElement element = trace[i];

            String methodName = element.getMethodName();

            if ( methodName.startsWith( "test" ) && !methodName.equals( "testProject" ) )
            {
                String classname = element.getClassName();

                buildLog = new File( BUILD_OUTPUT_DIRECTORY, classname + "_" + element.getMethodName() + ".build.log" );

                break;
            }
        }

        if ( buildLog == null )
        {
            buildLog = new File( BUILD_OUTPUT_DIRECTORY, "unknown.build.log" );
        }

        InvocationRequest request = buildTool.createBasicInvocationRequest( pom, properties, goals, buildLog );
        request.setUpdateSnapshots( false );
        request.setShowErrors( true );

        request.setDebug( true );

        if ( switchLocalRepo )
        {
            request.setLocalRepositoryDirectory( localRepositoryDirectory );
        }

        InvocationResult result = buildTool.executeMaven( request );

        if ( result.getExitCode() != 0 )
        {
            String buildLogUrl = buildLog.getAbsolutePath();

            try
            {
                buildLogUrl = buildLog.toURL().toExternalForm();
            }
            catch ( MalformedURLException e )
            {
            }

            throw new ExecutionFailedException( "Failed to execute build.\nPOM: " + pom + "\nGoals: " +
                StringUtils.join( goals.iterator(), ", " ) + "\nExit Code: " + result.getExitCode() + "\nError: " +
                result.getExecutionException() + "\nBuild Log: " + buildLogUrl + "\n", result );
        }
    }

    protected MavenProject readProject( File pom )
        throws TestToolsException
    {
        return projectTool.readProject( pom, localRepositoryDirectory );
    }

    protected String getPluginCLISpecification()
    {
        String pluginSpec = GROUP_ID + ":" + ARTIFACT_ID + ":" + VERSION + ":";

        return pluginSpec;
    }

    protected void assertFileEquals( String mavenRepo, File expectedFile, File actualFile, File baseDir )
        throws IOException
    {
        List expectedLines = getLines( mavenRepo, expectedFile );

        if ( !actualFile.exists() )
        {
            throw new AssertionFailedError( "Expected file not found: " + actualFile.getAbsolutePath() );
        }

        List actualLines = getLines( mavenRepo, actualFile );
        String filename = actualFile.getName();

        for ( int i = 0; i < expectedLines.size(); i++ )
        {
            String expected = expectedLines.get( i ).toString();

            // replace some vars in the expected line, to
            // account
            // for absolute paths that are different on each
            // installation.
            expected =
                StringUtils.replace( expected, "${basedir}", baseDir.getCanonicalPath());
            expected =
                StringUtils.replace( expected, "${M2_TEST_REPO}", localRepositoryDirectory.getCanonicalPath() );

            if ( actualLines.size() <= i )
            {
                fail( "Too few lines in the actual file. Was " + actualLines.size() + ", expected: " +
                    expectedLines.size() );
            }

            String actual = actualLines.get( i ).toString();

            if ( expected.startsWith( "#" ) && actual.startsWith( "#" ) )
            {
                // ignore comments, for settings file
                continue;
            }

            assertEquals( "Checking " + filename + ", line #" + ( i + 1 ), expected, actual );
        }

        assertTrue( "Unequal number of lines.", expectedLines.size() == actualLines.size() );
    }

    private List getLines( String mavenRepo, File file )
        throws IOException
    {
        List lines = new ArrayList();

        BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), "UTF-8" ) );

        String line;

        while ( ( line = reader.readLine() ) != null )
        {
            lines.add( line );
        }

        IOUtil.close( reader );

        return lines;
    }

    /**
     * @param basedir
     * @param projectOutputDir
     * @throws IOException
     */
    private void compareDirectoryContent( File basedir, File projectOutputDir, String additionalDir )
        throws IOException
    {

        File expectedConfigDir = new File( basedir, "expected" + File.separator + additionalDir );
        if ( expectedConfigDir.isDirectory() )
        {
            File[] files = expectedConfigDir.listFiles( new FileFilter()
            {
                public boolean accept( File file )
                {
                    return !file.isDirectory();
                }
            } );

            for ( int j = 0; j < files.length; j++ )
            {
                File expectedFile = files[j];
                File actualFile =
                    new File( projectOutputDir, additionalDir + expectedFile.getName() ).getCanonicalFile();

                if ( !actualFile.exists() )
                {
                    throw new AssertionFailedError( "Expected file not found: " + actualFile.getAbsolutePath() );
                }

                assertFileEquals( localRepositoryDirectory.getCanonicalPath(), expectedFile, actualFile, basedir );

            }
        }
    }
}
