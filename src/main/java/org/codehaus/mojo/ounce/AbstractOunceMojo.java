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

import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.ounce.core.OunceCore;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public abstract class AbstractOunceMojo
    extends AbstractMojo
    implements Contextualizable
{
    /**
     * The current Project.
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * The Maven Session Object
     * 
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession mavenSession;

    /**
     * This hint provides a way to switch the core implementation. Consult Ounce support for details, most users should
     * leave this set to the default. Use -Dounce.core=console to have have the output displayed instead of written to
     * the file for debugging purposes.
     * 
     * @parameter default-value="ouncexml" expression="${ounce.core}
     */
    protected String coreHint;

    /**
     * Map of Ounce variable names and paths.<br/> pathVariableMap variables are automatically registered with Ounce by
     * the Ounce/Maven plugin if the Ounce Automation Server is installed.
     * 
     * @parameter
     */
    Map pathVariableMap;

    /**
     * The name of the project set in the pom.
     * 
     * @parameter expression="${project.artifactId}
     * @readonly
     */
    protected String name;

    /**
     * The root of the project.
     * 
     * @parameter expression="${basedir}"
     * @readonly
     */
    private String projectRoot;

    /**
     * If pom packaging projects should be skipped. Typically these will not have source code and should be excluded.
     * This is true by default because typically the application or projects will be created at a pom level and the poms
     * have no source to be analyzed Only set this if you have source in your "pom" packaging projects that needs to be
     * scanned.
     * 
     * @parameter expression="${ounce.skipPoms}" default-value="true"
     */
    protected boolean skipPoms;

    /**
     * Extra Options supported by Ounce.
     */
    protected Map options;

    // set by the contextualize method. Only way to get the
    // plugin's container in 2.0.x
    protected PlexusContainer container;

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    protected OunceCore getCore()
        throws ComponentLookupException
    {
        return (OunceCore) container.lookup( OunceCore.ROLE, coreHint );
    }

    /**
     * @return the project
     */
    protected MavenProject getProject()
    {
        return this.project;
    }

    /**
     * @param theProject the project to set
     */
    protected void setProject( MavenProject theProject )
    {
        this.project = theProject;
    }

    /**
     * @return the coreHint
     */
    protected String getCoreHint()
    {
        return this.coreHint;
    }

    /**
     * @param theCoreHint the coreHint to set
     */
    protected void setCoreHint( String theCoreHint )
    {
        this.coreHint = theCoreHint;
    }

    /**
     * @return the name
     */
    protected String getName()
    {
        return this.name;
    }

    /**
     * @param theName the name to set
     */
    protected void setName( String theName )
    {
        this.name = theName;
    }

    /**
     * @return the projectRoot
     */
    protected String getProjectRoot()
    {
        return this.projectRoot;
    }

    /**
     * @param theProjectRoot the projectRoot to set
     */
    protected void setProjectRoot( String theProjectRoot )
    {
        this.projectRoot = theProjectRoot;
    }

    /**
     * @return the skipPoms
     */
    public boolean isSkipPoms()
    {
        return this.skipPoms;
    }

    /**
     * @param theSkipPoms the skipPoms to set
     */
    public void setSkipPoms( boolean theSkipPoms )
    {
        this.skipPoms = theSkipPoms;
    }

    /**
     * Returns true if the current project is located at the Execution Root Directory (where mvn was launched)
     * 
     * @return
     */
    protected boolean isThisTheExecutionRoot()
    {
        final Log log = getLog();
        if ( null == mavenSession )
        {
            log.debug( "Assuming this is the execution root." );
            return true;
        }
        final String executionRootDirectory = mavenSession.getExecutionRootDirectory();
        log.debug( "Root Folder:" + executionRootDirectory );
        log.debug( "Current Folder:" + projectRoot );
        final boolean result = executionRootDirectory.equalsIgnoreCase( projectRoot );
        if ( result )
        {
            log.debug( "This is the execution root." );
        }
        else
        {
            log.debug( "This is NOT the execution root." );
        }

        return result;
    }
}
