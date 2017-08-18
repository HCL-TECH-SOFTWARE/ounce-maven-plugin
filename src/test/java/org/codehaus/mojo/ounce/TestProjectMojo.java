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
package org.codehaus.mojo.ounce;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.ounce.TestUtils.MockProject;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class TestProjectMojo
    extends AbstractOunceMojoTest
{

    public void testGetClasspathElements()
        throws MojoExecutionException
    {

        ProjectOnlyMojo mojo = new ProjectOnlyMojo();
        mojo.setProject( getProject() );

        mojo.setClasspathScope( Artifact.SCOPE_COMPILE );
        assertEquals( 1, mojo.getClasspathElements().size() );

        mojo.setClasspathScope( Artifact.SCOPE_SYSTEM );
        assertEquals( 2, mojo.getClasspathElements().size() );

        mojo.setClasspathScope( Artifact.SCOPE_RUNTIME );
        assertEquals( 3, mojo.getClasspathElements().size() );

        mojo.setClasspathScope( Artifact.SCOPE_TEST );
        assertEquals( 4, mojo.getClasspathElements().size() );

        try
        {
            mojo.setClasspathScope( "someJunkValue" );
            mojo.getClasspathElements();
            fail( "Expected an exception here" );
        }
        catch ( MojoExecutionException e )
        {
            // expecting this
        }
    }

    public void testBuildClasspath()
        throws MojoExecutionException
    {
        ProjectOnlyMojo mojo = new ProjectOnlyMojo();
        mojo.setProject( getProject() );
        mojo.setClasspathScope( Artifact.SCOPE_SYSTEM );

        String classpath = mojo.getClasspath();

        String expectedResult = "/repo/test.jar" + File.pathSeparator + "/repo/test2.jar";
        assertEquals( expectedResult, classpath );
    }

    public MavenProject getProject()
    {
        MockProject project = new MockProject();

        ArrayList list = new ArrayList();
        list.add( File.separator + "repo" + File.separator + "test.jar" );
        project.setCompileClasspathElements( (List) list.clone() );
        list.add( File.separator + "repo" + File.separator + "test2.jar" );
        project.setSystemClasspathElements( (List) list.clone() );
        list.add( File.separator + "repo" + File.separator + "test3.jar" );
        project.setRuntimeClasspathElements( (List) list.clone() );
        list.add( File.separator + "repo" + File.separator + "test4.jar" );
        project.setTestClasspathElements( (List) list.clone() );

        return project;
    }

    /*
     * The two project mojos have different implementations of the getSourceRoots(). I am checking that they are both
     * getting their sources from the correct location with no cross over.
     */
    public void testGetSourceRoots()
    {
        MavenProject project = getProject();
        project.addCompileSourceRoot( "/src/main/generated" );
        project.addCompileSourceRoot( "/src/main/generated2" );
        project.addTestCompileSourceRoot( "/src/test/generated" );

        MavenProject executedProject = getProject();
        executedProject.addCompileSourceRoot( "/src/exec/generated" );
        executedProject.addCompileSourceRoot( "/src/exec/generated2" );
        executedProject.addTestCompileSourceRoot( "/src/testexec/generated" );

        ProjectOnlyMojo mojo = new ProjectOnlyMojo();
        mojo.setProject( project );

        // i should only get back the values from project
        // (no tests)
        List roots = mojo.getSourceRoots();
        assertTrue( roots.contains( "/src/main/generated" ) );
        assertTrue( roots.contains( "/src/main/generated2" ) );
        assertFalse( roots.contains( "/src/test/generated" ) );
        assertFalse( roots.contains( "/src/exec/generated" ) );
        assertFalse( roots.contains( "/src/exec/generated2" ) );
        assertFalse( roots.contains( "/src/testexec/generated" ) );

        mojo.setIncludeTestSources( true );
        roots = mojo.getSourceRoots();
        // now I should get the project and tests
        assertTrue( roots.contains( "/src/main/generated" ) );
        assertTrue( roots.contains( "/src/main/generated2" ) );
        assertTrue( roots.contains( "/src/test/generated" ) ); // <----now
        // true
        assertFalse( roots.contains( "/src/exec/generated" ) );
        assertFalse( roots.contains( "/src/exec/generated2" ) );
        assertFalse( roots.contains( "/src/testexec/generated" ) );

        ProjectMojo mojo2 = new ProjectMojo();
        mojo2.setProject( project );
        mojo2.setExecutedProject( executedProject );

        roots = mojo2.getSourceRoots();
        // i should only get the executed sources
        assertFalse( roots.contains( "/src/main/generated" ) );
        assertFalse( roots.contains( "/src/main/generated2" ) );
        assertFalse( roots.contains( "/src/test/generated" ) );
        assertTrue( roots.contains( "/src/exec/generated" ) );
        assertTrue( roots.contains( "/src/exec/generated2" ) );
        assertFalse( roots.contains( "/src/testexec/generated" ) );

        mojo2.setIncludeTestSources( true );
        roots = mojo2.getSourceRoots();
        // now executed and executed tests.
        assertFalse( roots.contains( "/src/main/generated" ) );
        assertFalse( roots.contains( "/src/main/generated2" ) );
        assertFalse( roots.contains( "/src/test/generated" ) );
        assertTrue( roots.contains( "/src/exec/generated" ) );
        assertTrue( roots.contains( "/src/exec/generated2" ) );
        assertTrue( roots.contains( "/src/testexec/generated" ) ); // <----
        // now
        // true

    }

    public void testSkipPom()
    {
        MockProject project = new MockProject();
        project.setPackaging( "pom" );

        ProjectMojo mojo = new ProjectMojo();
        mojo.setProject( project );
        mojo.setSkipPoms( true );

        // since this is a pom, it should just return
        // immediately.
        try
        {
            mojo.execute();
        }
        catch ( MojoExecutionException e )
        {
            fail( e.getLocalizedMessage() );
        }
        catch ( MojoFailureException e )
        {
            fail( e.getLocalizedMessage() );
        }

        // I'm expecting that since the project isn't fully
        // configured, trying to do this is
        // going to cause all kinds of exceptions. That's
        // the easiest way to confirm that the mojo
        // isn't skipping anymore.
        mojo.setSkipPoms( false );
        try
        {
            mojo.execute();
            fail( "Expected an Exception here" );
        }
        catch ( MojoExecutionException e )
        {
        }
        catch ( MojoFailureException e )
        {
        }

        project.setPackaging( "jar" );
        // I'm expecting that since the project isn't fully
        // configured, trying to do this is
        // going to cause all kinds of exceptions. That's
        // the easiest way to confirm that the mojo
        // isn't skipping non poms.
        try
        {
            mojo.execute();
            fail( "Expected an Exception here" );
        }
        catch ( MojoExecutionException e )
        {
        }
        catch ( MojoFailureException e )
        {
        }

        // I'm expecting that since the project isn't fully
        // configured, trying to do this is
        // going to cause all kinds of exceptions. That's
        // the easiest way to confirm that the mojo
        // isn't skipping non poms.
        mojo.setSkipPoms( true );
        try
        {
            mojo.execute();
            fail( "Expected an Exception here" );
        }
        catch ( MojoExecutionException e )
        {
        }
        catch ( MojoFailureException e )
        {
        }
    }
}
