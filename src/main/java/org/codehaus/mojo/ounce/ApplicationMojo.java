/*
 * Copyright (c) 2007, Ounce Labs, Inc.
 * All rights reserved.
 * (c) Copyright HCL Technologies Ltd. 2017. All rights reserved.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.ounce.core.OunceCore;
import org.codehaus.mojo.ounce.core.OunceCoreApplication;
import org.codehaus.mojo.ounce.core.OunceCoreException;
import org.codehaus.mojo.ounce.core.OunceProjectBean;
import org.codehaus.mojo.ounce.utils.ExternalApplication;
import org.codehaus.mojo.ounce.utils.ProjectFileInfo;
import org.codehaus.mojo.ounce.utils.Utils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.util.StringUtils;

/**
 * This mojo generates an Ounce application file. It will automatically include all child modules as projects. This list
 * may be modified using the includes and excludes patterns. Projects that are external to this build may be included
 * directly using the externalProjects list. External Applications may also be included. All of their modules will be
 * inherted as part of this application file. Those projects may also be filtered upon import.
 * 
 * @aggregator
 * @phase package
 * @goal application
 */
public class ApplicationMojo
    extends AbstractOunceMojo
{

    /**
     * The projects in the current build.
     * 
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    private List<MavenProject> projects;

    /**
     * An array of directories containing the pom file of any projects to include. If an include pattern is specified,
     * projects not specifed by include patterns are excluded. <br/> Include only applies to inherited modules, not
     * external projects. The current project is not filtered. <br/> The include pattern may contain the following
     * wildcard characters:<br/> *: Zero or more characters<br/> **: Any folders<br/> ?: One and only one character<br/>
     * 
     * @parameter
     */
    protected String[] includes;

    /**
     * An array of directories containing the pom file of any projects to exclude. Excludes can contain standard
     * Ant-style wildcards. <br/> Excludes only apply to inherited modules, not external projects. The current project
     * is not filtered.
     * 
     * @parameter
     */
    protected String[] excludes;

    /**
     * List of external projects to include. These projects are included after any other projects have been included or
     * excluded. <br/> The format is: name,path<br/> Where:<br/>
     * <li>name is the artifact ID of the project to include.</li>
     * <li>path is the pathname to the project.</li>
     * 
     * @parameter
     */
    protected List externalProjects;

    /**
     * Allows you to include projects from multiple applications. The external application properties are not inherited,
     * and the external application must already exist. <br/> externalApplications is a list of directories containing
     * top-level pom files. <br/> The format for externalApplications is:
     * pathname,[includes|includes],[excludes|excludes] <br/> Where:<br/>
     * <li>pathname, includes, and excludes are comma delimited; if you have excludes, but no includes, use two commas.</li>
     * <li>Multiple includes or excludes are separated by pipes (\x7c).</li>
     * <li>Excludes can contain standard Ant style wildcards.</li>
     * 
     * @parameter
     */
    protected List externalApplications;
    
    /**
     * Specifies the directory where to create the ppf file
     * 
     * @parameter expression="${ounce.projectDir}" default-value="${basedir}"
     */
    private String projectDir;
    
    /**
     * Specifies the directory where to create the paf file
     * 
     * @parameter expression="${ounce.appDir}" default-value="${basedir}"
     * 
     */
    private String appDir;
    
    /**
     * Specifies the directory where to create the paf file
     * 
     * @parameter expression="${ounce.appName}" default-value="${project.artifactId}"
     * 
     */
    private String appName;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!isThisTheExecutionRoot())
           return;

        try {
            OunceCore core = getCore();

            List coreProjects = getIncludedModules();
            List externs = getExternalProjects();
            
            if (externs != null) {
                if (coreProjects != null)
                    coreProjects.addAll( externs );
                else
                    coreProjects = externs;
            }

            externs = getIncludedExternalApplicationProjects( core );
            if (externs != null)
                coreProjects.addAll( externs );

            getLog().debug("AppMojo: Application directory is " + appDir);
            core.createApplication(appDir, appName, appDir, coreProjects, options , getLog());
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "Unable to lookup the core interface for hint: " + coreHint, e );
        }
        catch ( OunceCoreException e )
        {
            throw new MojoExecutionException( "Nested Ouncecore exception: " + e.getLocalizedMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Nested IOexception: " + e.getLocalizedMessage(), e );
        }

    }

    /**
     * This method filters the projects.
     * 
     * @param theProjects
     * @param includes
     * @param excludes
     * @return
     * @throws IOException
     */
    private List getSelectedModules( List theProjects, String[] includes, String[] excludes ) throws IOException
    {
        IncludeExcludeFileSelector selector = new IncludeExcludeFileSelector();

        if ( excludes != null && excludes.length > 0 )
        {
            selector.setExcludes( excludes );
        }

        if ( includes != null && includes.length > 0 )
        {
            selector.setIncludes( includes );
        }

        List coreProjects = new ArrayList( theProjects.size() );

        Iterator iter = theProjects.iterator();
        while ( iter.hasNext() ) {
            MavenProject prj = (MavenProject) iter.next();
            
            File prjt = null;
            
            if(!projectDir.isEmpty())
            	prjt = new File(getProjectRoot());
            else {
            	prjt = prj.getBasedir();
            	getLog().debug("AppMojo: else");
            }
            
            getLog().debug("AppMojo: Project Directory: " + prjt);

            if ( selector.isSelected( new ProjectFileInfo( prjt ) ) || prj == project )
                coreProjects.add( prj );
        }
        return coreProjects;
    }

    /**
     * Build a list of modules to be included as projects. These will be projects that are children of the current
     * project.
     * 
     * @return List of OunceProjectBeans representing each module
     * @throws IOException
     */
    protected List getIncludedModules() throws IOException
    {
        /*
         * first we need to prefilter the reactor projects list. instead of including only the current project's
         * children, it includes everything we need to build a prefilter based on the current project's path and only
         * include projects with a matching path.
         */
        String[] preFilterIncludes = new String[1];
        preFilterIncludes[0] = "**/" + project.getBasedir().getParentFile().getName() + "/**";
        List preFilteredProjects = getSelectedModules( projects, preFilterIncludes, null );

        // now do the normal filtering
        List includedProjects = getSelectedModules( preFilteredProjects, includes, excludes );
		includedProjects = getSelectedModules( projects, includes, excludes );
		
        // now make them beans
        return convertToBeans( includedProjects );
    }

    /**
     * Converts a list of Maven Projects to OunceProjectBeans
     * 
     * @param theProjects
     * @return
     */
    private List<OunceProjectBean> convertToBeans(List<OunceProjectBean> theProjects)
    {
        List<OunceProjectBean> beanProjects = new ArrayList<OunceProjectBean>(theProjects.size());

        Iterator iter = theProjects.iterator();
        while (iter.hasNext()) {
            MavenProject prj = (MavenProject) iter.next();

            if (!skipPoms || !prj.getPackaging().equalsIgnoreCase("pom")) {
            	
				beanProjects.add( new OunceProjectBean(prj.getBasedir().getPath(), prj.getArtifactId()));
            }
            else
                getLog().debug( "Skipping Pom: " + prj.getArtifactId() );
        }

        return beanProjects;
    }

    /**
     * Get the list of user defined external projects
     * 
     * @return List of external Projects
     * @throws MojoExecutionException if the format is invalid
     */
    protected List getExternalProjects() throws MojoExecutionException {
        List externals = null;

        if ( externalProjects != null && externalProjects.size() > 0 ) {
            externals = new ArrayList( externalProjects.size() );

            // add each one
            Iterator iter = externalProjects.iterator();
            while ( iter.hasNext() ) {
                // break the project into name:path
                String extern = (String) iter.next();
                String[] prj = extern.split( "," );
                if ( prj.length == 2 )
                    externals.add( new OunceProjectBean( prj[1], prj[0] ) );
                else // they didn't follow the format
                    throw new MojoExecutionException( "Invalid External Project String: " + extern );
            }
        }
        return externals;
    }

    /**
     * Get the list of user defined external application files to process
     * 
     * @return List of external Applications
     * @throws MojoExecutionException if the format is invalid
     */
    protected List getExternalApplications() throws MojoExecutionException {
        List externals = null;

        if ( externalApplications != null && externalApplications.size() > 0 ) {
            externals = new ArrayList( externalApplications.size() );

            // add each one
            Iterator iter = externalApplications.iterator();
            while ( iter.hasNext() ) {
                // break the project into
                // path,includes,excludes
                String extern = (String) iter.next();
                String[] prj = extern.split( "," );

                if ( prj.length == 3 )
                    externals.add( new ExternalApplication( prj[0], prj[1], prj[2] ) );
                else if ( prj.length == 2 )
                    externals.add( new ExternalApplication( prj[0], prj[1], null ) );
                else if ( prj.length == 1 )
                    externals.add( new ExternalApplication( prj[0], null, null ) );
                else // they didn't follow the format
                    throw new MojoExecutionException( "Invalid External Application String: " + extern );
            }
        }
        else
            externals = new ArrayList();

        return externals;
    }

    /**
     * This method processes the external Applications and filters them according to the include/exclude patterns
     * 
     * @return List of OunceCoreProject objects
     * @throws MojoExecutionException
     * @throws OunceCoreException
     * @throws IOException
     */
    public List getIncludedExternalApplicationProjects( OunceCore core ) throws MojoExecutionException, OunceCoreException, IOException {
        List externalApps = getExternalApplications();

        // init results
        ArrayList results = new ArrayList( externalApps.size() );

        Iterator iter = externalApps.iterator();

        while ( iter.hasNext() ) {
            ExternalApplication extern = (ExternalApplication) iter.next();

            // read the projects from the application ->
            getLog().debug( "Reading External Application: " + extern.getPath() );
            OunceCoreApplication app = core.readApplication( extern.getPath(), getLog() );

            results.addAll( filterExternalApplicationProjects( app, extern ) );
        }

        return results;
    }

    /**
     * This method filters the projects retrieved from the external application
     * 
     * @param app
     * @param extern
     * @return
     * @throws IOException
     */
    public List filterExternalApplicationProjects( OunceCoreApplication app, ExternalApplication extern ) throws IOException {
        List results = new ArrayList();
        
        if ( app != null && app.getProjects() != null )
        {
            IncludeExcludeFileSelector fileSelector = new IncludeExcludeFileSelector();

            String excludeString = extern.getExcludes();
            String includeString = extern.getIncludes();

            // init scanner with inc/exc
            if ( StringUtils.isNotEmpty( excludeString ) )
                fileSelector.setExcludes( excludeString.split( "\\x7C" ) ); // split on |

            if ( StringUtils.isNotEmpty( includeString ) )
                fileSelector.setIncludes( includeString.split( "\\x7C" ) ); // split on |

            for ( Iterator pIter = app.getProjects().iterator(); pIter.hasNext(); ) {
                OunceProjectBean prj = (OunceProjectBean) pIter.next();
                getLog().debug( "Filtering External App Project: " + prj );
                
                if ( fileSelector.isSelected( new ProjectFileInfo( new File( prj.getPath() ) ) ) ) {
                    String path = extern.getPath() + File.separator + prj.getPath();
                    path = Utils.convertToVariablePath( path, pathVariableMap );
                    getLog().debug("AppMojo: Path after conversion: " + path);
                    prj.setPath( path );

                    getLog().debug( "Adding External App Project: " + prj );
                    results.add( prj );
                }
            }
        }
        return results;
    }

    /**
     * @return the projects
     */
    public List getProjects()
    {
        return this.projects;
    }

    /**
     * @param theProjects the projects to set
     */
    public void setProjects( List theProjects )
    {
        this.projects = theProjects;
    }
    
    /**
     * @return the includes
     */
    public String[] getIncludes()
    {
        return this.includes;
    }

    /**
     * @param theIncludes the includes to set
     */
    public void setIncludes( String[] theIncludes )
    {
        this.includes = theIncludes;
    }

    /**
     * @return the excludes
     */
    public String[] getExcludes()
    {
        return this.excludes;
    }

    /**
     * @param theExcludes the excludes to set
     */
    public void setExcludes( String[] theExcludes )
    {
        this.excludes = theExcludes;
    }

    /**
     * @param theExternalProjects the externalProjects to set
     */
    public void setExternalProjects( List theExternalProjects )
    {
        this.externalProjects = theExternalProjects;
    }

    /**
     * @param theExternalApplications the externalApplications to set
     */
    public void setExternalApplications( List theExternalApplications )
    {
        this.externalApplications = theExternalApplications;
    }

    /**
     * @param thePathProperties the pathVariableMap to set
     */
    public void setPathProperties( Map thePathProperties )
    {
        this.pathVariableMap = thePathProperties;
    }
    
}
