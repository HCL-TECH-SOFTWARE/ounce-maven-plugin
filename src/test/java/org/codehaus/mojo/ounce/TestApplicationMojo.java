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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.ounce.TestUtils.ApplicationMojoMock;
import org.codehaus.mojo.ounce.TestUtils.MockProject;
import org.codehaus.mojo.ounce.TestUtils.OunceCoreMock;
import org.codehaus.mojo.ounce.core.OunceCoreApplication;
import org.codehaus.mojo.ounce.core.OunceCoreException;
import org.codehaus.mojo.ounce.core.OunceProjectBean;
import org.codehaus.mojo.ounce.utils.ExternalApplication;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class TestApplicationMojo
    extends TestCase
{

    public void testApplicationRelative()
        throws ComponentLookupException, MojoExecutionException, MojoFailureException
    {

        ApplicationMojoMock mojo = new ApplicationMojoMock();

        mojo.setSkipPoms( false );
        mojo.setName( "MyName" );
        mojo.setProjectRoot( new File( "/etc/bin/myname" ).getAbsolutePath() );

        List projects = getProjects();

        // first one in the list is always the current
        // project.
        mojo.setProject( (MavenProject) projects.get( 0 ) );

        mojo.setProjects( projects );
        mojo.execute();

        OunceCoreMock core = (OunceCoreMock) mojo.getCore();
        OunceCoreApplication app = core.getApp();

        assertEquals( "MyName", app.getName() );
        assertEquals( ".", app.getApplicationRoot() );

        List ounceProjects = app.getProjects();

        assertEquals( 2, ounceProjects.size() );

        OunceProjectBean prj = (OunceProjectBean) ounceProjects.get( 0 );
        assertEquals( "MyName", prj.getName() );
        assertEquals( ".", prj.getPath() );

        prj = (OunceProjectBean) ounceProjects.get( 1 );
        assertEquals( "foo", prj.getName() );
        assertEquals( "foo", prj.getPath() );

    }

    public void testApplicationSkipCurrent()
        throws ComponentLookupException, MojoExecutionException, MojoFailureException
    {

        ApplicationMojoMock mojo = new ApplicationMojoMock();

        mojo.setSkipPoms( true );
        mojo.setName( "MyName" );
        mojo.setProjectRoot( new File( "/etc/bin/myname" ).getAbsolutePath() );
        List projects = getProjects();

        // first one in the list is always the current
        // project.
        mojo.setProject( (MavenProject) projects.get( 0 ) );

        mojo.setProjects( projects );
        mojo.execute();

        OunceCoreMock core = (OunceCoreMock) mojo.getCore();
        OunceCoreApplication app = core.getApp();

        assertEquals( "MyName", app.getName() );
        assertEquals( ".", app.getApplicationRoot() );

        List ounceProjects = app.getProjects();

        assertEquals( 1, ounceProjects.size() );

        OunceProjectBean prj = (OunceProjectBean) ounceProjects.get( 0 );
        assertEquals( "foo", prj.getName() );
        assertEquals( "foo", prj.getPath() );

    }

    public void testApplicationExcludes()
        throws MojoExecutionException, MojoFailureException, ComponentLookupException
    {
        ApplicationMojoMock mojo = new ApplicationMojoMock();

        mojo.setSkipPoms( false );
        mojo.setName( "MyName" );

        mojo.setProjectRoot( getCanonicalName( "/etc/bin/myname" ) );

        List projects = getProjects();

        // first one in the list is always the current
        // project.
        mojo.setProject( (MavenProject) projects.get( 0 ) );

        mojo.setProjects( projects );
        String[] excludes = { "**/foo" };
        mojo.setExcludes( excludes );
        mojo.execute();
        OunceCoreMock core = (OunceCoreMock) mojo.getCore();
        OunceCoreApplication app = core.getApp();

        assertEquals( "MyName", app.getName() );
        assertEquals( ".", app.getApplicationRoot() );

        List ounceProjects = app.getProjects();

        assertEquals( 1, ounceProjects.size() );
        assertContains( ounceProjects, ".", mojo.getProjectRoot(), "MyName" );
    }

    public void testApplicationIncludes()
        throws MojoExecutionException, MojoFailureException, ComponentLookupException
    {
        ApplicationMojoMock mojo = new ApplicationMojoMock();

        mojo.setSkipPoms( true );
        mojo.setName( "MyName" );

        mojo.setProjectRoot( getCanonicalName( "/etc/bin/myname" ) );

        List projects = getProjects();

        // first one in the list is always the current
        // project.
        mojo.setProject( (MavenProject) projects.get( 0 ) );

        mojo.setProjects( projects );
        String[] includes = { "**/foo" };
        mojo.setIncludes( includes );
        mojo.execute();
        OunceCoreMock core = (OunceCoreMock) mojo.getCore();
        OunceCoreApplication app = core.getApp();

        assertEquals( "MyName", app.getName() );
        assertEquals( ".", app.getApplicationRoot() );

        List ounceProjects = app.getProjects();

        assertEquals( 1, ounceProjects.size() );
        assertContains( ounceProjects, "foo", mojo.getProjectRoot(), "foo" );
    }

    public void testApplicationExcludePath()
        throws MojoExecutionException, MojoFailureException, ComponentLookupException
    {
        ApplicationMojoMock mojo = new ApplicationMojoMock();

        mojo.setSkipPoms( false );
        mojo.setName( "MyName" );

        mojo.setProjectRoot( getCanonicalName( "/etc/bin/myname" ) );

        List projects = getProjects();
        MockProject project = new MockProject();
        project.setBaseDir( new File( "/etc/src/myname/foo" ) );
        project.setArtifactId( "foo2" );

        projects.add( project );

        // first one in the list is always the current
        // project.
        mojo.setProject( (MavenProject) projects.get( 0 ) );

        mojo.setProjects( projects );
        String[] excludes = { "**/src/" };

        mojo.setExcludes( excludes );
        mojo.execute();
        OunceCoreMock core = (OunceCoreMock) mojo.getCore();
        OunceCoreApplication app = core.getApp();

        assertEquals( "MyName", app.getName() );
        assertEquals( ".", app.getApplicationRoot() );

        List ounceProjects = app.getProjects();

        assertEquals( 2, ounceProjects.size() );

        assertContains( ounceProjects, ".", mojo.getProjectRoot(), "MyName" );
        assertContains( ounceProjects, "foo", mojo.getProjectRoot(), "foo" );

    }

    public void testExternals()
        throws MojoExecutionException, MojoFailureException, ComponentLookupException
    {
        ApplicationMojoMock mojo = new ApplicationMojoMock();

        mojo.setSkipPoms( true );
        mojo.setName( "MyName" );

        mojo.setProjectRoot( getCanonicalName( "/etc/bin/myname" ) );

        List projects = getProjects();
        MockProject project = new MockProject();
        project.setBaseDir( new File( "/etc/bin/myname" ) );
        project.setArtifactId( "foo2" );
        project.setPackaging( "jar" );

        projects.add( project );

        // first one in the list is always the current
        // project.
        mojo.setProject( (MavenProject) projects.get( 0 ) );

        mojo.setProjects( projects );

        // exclude all the modules
        String[] includes = { " " };
        mojo.setIncludes( includes );

        List externs = new ArrayList();
        externs.add( "name,/mypath" );
        externs.add( "name2,/mypath2" );
        mojo.setExternalProjects( externs );
        mojo.execute();
        OunceCoreMock core = (OunceCoreMock) mojo.getCore();
        OunceCoreApplication app = core.getApp();

        assertEquals( "MyName", app.getName() );
        assertEquals( ".", app.getApplicationRoot() );

        List ounceProjects = app.getProjects();

        assertEquals( 2, ounceProjects.size() );
        assertContains( ounceProjects, "/mypath", mojo.getProjectRoot(), "name" );
        assertContains( ounceProjects, "/mypath2", mojo.getProjectRoot(), "name2" );
    }

    public void testExternalApplications()
        throws MojoExecutionException
    {
        ApplicationMojo mojo = new ApplicationMojo();

        List apps = new ArrayList();
        apps.add( "/mypath" );
        apps.add( "/mypath2" );
        apps.add( "/foo,**/foo|**/6" );
        apps.add( "/foo2,**/*,**/foo" );

        mojo.setExternalApplications( apps );

        List result = mojo.getExternalApplications();

        assertEquals( 4, result.size() );
        ExternalApplication app = (ExternalApplication) result.get( 0 );

        assertEquals( "/mypath", app.getPath() );
        assertEquals( null, app.getIncludes() );
        assertEquals( null, app.getExcludes() );

        app = (ExternalApplication) result.get( 1 );
        assertEquals( "/mypath2", app.getPath() );
        assertEquals( null, app.getIncludes() );
        assertEquals( null, app.getExcludes() );

        app = (ExternalApplication) result.get( 2 );
        assertEquals( "/foo", app.getPath() );
        assertEquals( "**/foo|**/6", app.getIncludes() );
        assertEquals( null, app.getExcludes() );

        app = (ExternalApplication) result.get( 3 );
        assertEquals( "/foo2", app.getPath() );
        assertEquals( "**/*", app.getIncludes() );
        assertEquals( "**/foo", app.getExcludes() );

        apps = new ArrayList();
        apps.add( "something,something,something,,something" );

        mojo.setExternalApplications( apps );

        try
        {
            mojo.getExternalApplications();
            fail( "Expected an exception" );
        }
        catch ( MojoExecutionException e )
        {

        }
    }

    public void testIncludeExternalProjectsFromApplication()
        throws MojoExecutionException, OunceCoreException, IOException
    {
        ApplicationMojo mojo = new ApplicationMojo();
        OunceCoreMock core = new OunceCoreMock();

        List externalApplications = new ArrayList();
        externalApplications.add( "myExternalApp" );

        mojo.setExternalApplications( externalApplications );

        List projects = mojo.getIncludedExternalApplicationProjects( core );

        assertEquals( 3, projects.size() );

        assertContains( projects, "myExternalApp/e", "myExternalApp", "e" );
        assertContains( projects, "myExternalApp/uis/f", "myExternalApp", "f" );
        assertContains( projects, "myExternalApp/uis/g", "myExternalApp", "g" );

        externalApplications.clear();
        externalApplications.add( "myExternalApp,**/e" );

        projects = mojo.getIncludedExternalApplicationProjects( core );
        assertEquals( 1, projects.size() );
        assertContains( projects, "myExternalApp/e", "myExternalApp", "e" );

        externalApplications.clear();
        externalApplications.add( "myExternalApp,**/uis/**" );

        projects = mojo.getIncludedExternalApplicationProjects( core );
        assertEquals( 2, projects.size() );
        assertContains( projects, "myExternalApp/uis/f", "myExternalApp", "f" );
        assertContains( projects, "myExternalApp/uis/g", "myExternalApp", "g" );

        externalApplications.clear();
        externalApplications.add( "myExternalApp,**/*,**/uis/*" );

        projects = mojo.getIncludedExternalApplicationProjects( core );
        assertEquals( 1, projects.size() );
        assertContains( projects, "myExternalApp/e", "myExternalApp", "e" );
    }

    public void testPropertyReplacements()
        throws MojoExecutionException, OunceCoreException, IOException
    {
        ApplicationMojo mojo = new ApplicationMojo();
        OunceCoreMock core = new OunceCoreMock();

        List externalApplications = new ArrayList();
        externalApplications.add( "myExternalApp" );

        Map properties = new HashMap();

        // intentionally using backwards path to makesure it
        // is handled correctly
        properties.put( "pathToExtern", "myExternalApp\\e" );
        properties.put( "pathToExtern2", "myExternalApp\\uis" );

        mojo.setPathProperties( properties );

        mojo.setExternalApplications( externalApplications );

        List projects = mojo.getIncludedExternalApplicationProjects( core );

        assertEquals( 3, projects.size() );

        assertContains( projects, "%pathToExtern%", "myExternalApp", "e" );
        assertContains( projects, "%pathToExtern2%/f", "myExternalApp", "f" );
        assertContains( projects, "%pathToExtern2%/g", "myExternalApp", "g" );
    }

    public List getProjects()
    {
        List projects = new ArrayList();
        MockProject project = new MockProject();
        project.setBaseDir( new File( "/etc/bin/myname" ) );
        project.setPackaging( "pom" );
        project.setArtifactId( "MyName" );
        projects.add( project );

        project = new MockProject();
        project.setBaseDir( new File( "/etc/bin/myname/foo" ) );
        project.setArtifactId( "foo" );
        project.setPackaging( "jar" );
        projects.add( project );
        return projects;
    }

    private String getCanonicalName( String pName )
    {
        return pName.replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );
    }

    public void assertContains( List list, String path, String projectRoot, String name )
    {
        // this is done to handle weird ways that windows
        // creates the paths.
        // the IT tests ensure that the correct pathing is
        // always done.
        String absPath = new File( path ).getAbsolutePath();
        String drive = absPath.substring( 0, 2 );
        absPath = drive + path;

        // System.out.println("Assert that "+list+ "contains
        // "+ absPath+" or "+ path);
        // try the absolute and the original.
        OunceProjectBean one = new OunceProjectBean( absPath, name );
        OunceProjectBean two = new OunceProjectBean( path, name );
        OunceProjectBean three = new OunceProjectBean( getCanonicalName( path ), name );
        assertTrue( list.contains( one ) || list.contains( two ) || list.contains( three ) );
    }
}
