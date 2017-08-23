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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.ounce.core.OunceCoreException;
import org.codehaus.mojo.ounce.utils.Utils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * This mojo generates an Ounce project file. It does not fork the build like the "project" mojo and is instead intended
 * to be bound in a pom for automatic execution. If you would rather have the project generated on demand via the
 * command line, use the project goal instead.
 * 
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * @requiresDependencyResolution test
 * @goal project-only
 * @phase package
 */
public class ProjectOnlyMojo extends AbstractOunceMojo
{
    public static final String M2_REPO = "M2_REPO";

    /**
     * The scope of the classpath used to analyze this project. <br/> Valid choices are: compile, test, runtime, or
     * system. If includeTestSources is true, then the classpathScope reverts to test. Otherwise, the default is
     * compile.
     * 
     * @parameter default-value="compile" expression="${ounce.classpathScope}";
     */
    private String classpathScope;

    /**
     * JSP Compilers as of 9.0.3.4
		name="Jasper 3 (Tomcat 5)" type="1"
		name="WebLogic 8" type="2"
		name="WebLogic 9" type="3"
		name="WebSphere 7.0" type="5"
		name="Tomcat 6" type="6"
		name="Tomcat 7" type="7"
		name="WebSphere 8.0" type="8"
		name="WebSphere 8.5" type="9"
		name="WebLogic 11g" type="10"
		name="WebLogic 12c" type="11"
		name="Precompiled" type="12"
		name="Tomcat 8" type="13"
     **/

    /**
     * JSP compiler type name / application server.  
     * 
     * @parameter default-value="Tomcat 8" expression="${ounce.jspCompilerName}"
     */
    private String jspCompilerName;

    /**
     * JSP compiler type name / application server.  
     * 
     * @parameter default-value="13" expression="${ounce.jspCompilerType}"
     */
    private String jspCompilerType;

    /**
     * JDK configuration known to Ounce Core.
     * 
     * @parameter expression="${ounce.jdkName}"
     */
    private String jdkName;
    
    /**
     * Options to pass to the javac compiler.
     * 
     * @parameter expression="${ounce.javaCompilerOptions}"
     */
    private String javaCompilerOptions;

    /**
     * If TestSources should be included in the compilable sources. If set, adds project.getTestSourceRoot() to the path
     * and defaults the classpathScope to test.
     * 
     * @parameter expression="${ounce.includeTestSources}" default-value="false"
     */
    protected boolean includeTestSources;

    /**
     * The directory where the webapp is built for war
     * projects.
     * 
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    private String webappDirectory;
    
    /**
     * Whether the plugin should use the Ounce Automation Server to create any necessary variables (such as M2_REPO).
     * Requires that the Ounce Automation Server be installed.
     * 
     * @parameter expression="${ounce.createVariables}" default-value="true"
     */
    protected boolean createVariables;

    /**
     * Whether the plugin should force project files to be generated web projects
     * regardless of what the packaging is set to in the pom.xml.
     * 
     * @parameter expression="${ounce.forceWeb}" default-value="false"
     */
    protected boolean forceWeb;

    /**
     * The location of the Ounce client installation directory. Required if ounceauto is not on the path.
     * 
     * @parameter expression="${ounce.installDir}"
     */
    private String installDir;
    
    /**
     * Whether to analyze the framework for a Struts application
     * 
     * @parameter expression="${ounce.analyzeStrutsFramework}" default-value="false"
     */
    private boolean analyzeStrutsFramework;
    
    /**
     * Whether to import Struts validation routines
     * 
     * @parameter expression="${ounce.importStrutsValidation}" default-value="false"
     */
    private boolean importStrutsValidation;

    /**
     * Location of the local repository.
     * 
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository local;

    /**
     * 
     * @parameter expression="${ounce.precompileScan}" default-value="false"
     * 
     */
    private boolean precompileScan;
    
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
     * Specifies external jars to be added to the project classpath.  These
     * are typically jar files that are part of the application server
     * installation.  Comma separated list of files with absolute path.
     * 
     * @parameter expression="${ounce.externalJars}" default-value="${basedir}"
     * 
     */
    private String externalJars;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( project.getPackaging() != "pom" || !skipPoms )
        {
            try
            {
            	if(createVariables)
            		configureVariables();

               getCore().createProject( getProjectRoot(), name, jspCompilerName, jspCompilerType, getProjectRoot(), getSourceRoots(), getWebRoot(), getClasspath(),
                                    jdkName, javaCompilerOptions, project.getPackaging(), getOptions(), 
                                    forceWeb, analyzeStrutsFramework, importStrutsValidation, projectDir, this.getLog() );
            }
            catch ( ComponentLookupException e )
            {
                throw new MojoExecutionException( "Unable to lookup the core interface for hint: " + coreHint, e );
            }
            catch ( OunceCoreException e )
            {
                throw new MojoExecutionException( "Nested Ouncecore exception: " + e.getLocalizedMessage(), e );
            }
        }
        else
        {
            getLog().info( "Skipping Pom project." );
        }
    }

    /**
     * This method gets the source roots from the project.
     * 
     * @return List of source roots.
     */
    protected List<String> getSourceRoots()
    {
		List<String> sourceRoots = project.getCompileSourceRoots();
        if (includeTestSources)
            sourceRoots.addAll(project.getTestCompileSourceRoots());
        
        return Utils.convertToRelativePaths(sourceRoots, projectDir);
    }
    
    protected Map<String, String> getOptions() {
    	if(options == null)
    		options = new HashMap<String, String>();
    	
    	if(precompileScan && new File(project.getBuild().getOutputDirectory()).isDirectory()) {
    		File classesDir = new File(project.getBuild().getOutputDirectory());
    		
    		//Make sure the directory contains .class files
    		if(!Utils.getFilesOfType(classesDir, ".class").isEmpty())
    			options.put("precompiled", Utils.makeRelative(classesDir.getAbsolutePath(), projectDir));
    		else
    			options.put("precompiled", "");
    	}
    		
    	return options;
    }

    /**
     * Gets the classpath elements and returns a properly formatted classpath.
     * 
     * @return
     * @throws MojoExecutionException
     */
    protected String getClasspath() throws MojoExecutionException
    {
        List classpathElements = getClasspathElements();

        StringBuffer sb = new StringBuffer();
        Iterator i = classpathElements.iterator();
        if ( i.hasNext() ) {
        	// first one, no separator needed
        	String cpe = (String) i.next();
        	String converted_cpe = Utils.convertClasspathElement( cpe, projectDir, pathVariableMap);
            sb.append( converted_cpe );
            this.getLog().debug("ProjectOnlyMojo|buildClasspath: " + cpe + " converted: " + converted_cpe);
            
            // separate the rest of them with pathSeparator
            while ( i.hasNext() ) {
            	String cpe2 = (String) i.next();
            	String converted_cpe2 = Utils.convertClasspathElement( cpe2, projectDir, pathVariableMap);
                sb.append( File.pathSeparator );
                sb.append( converted_cpe2 );
                this.getLog().debug("ProjectOnlyMojo|buildClasspath: " + cpe2 + " converted: " + converted_cpe2);
            }
        }
        // Add extrenalJars to the end of the Classpath.
        // This is a comma separated list that needs to be converted to relative paths.
        for(String jar : getExternalJars().split(","))
        	sb.append(File.pathSeparator + Utils.makeRelative(jar,  projectDir));

        return sb.toString();
    }

    /**
     * Gets the properly scoped classpathElements from the Maven Project
     * 
     * @return List of classpath strings.
     * @throws MojoExecutionException
     */
    protected List getClasspathElements()
        throws MojoExecutionException
    {
        List classpathElements = null;

        try
        {
            // checking if test sources are included. If so,
            // then we want the classpath to be test
            // (includes everything)
            if ( Artifact.SCOPE_TEST.equalsIgnoreCase( this.classpathScope ) || this.includeTestSources )
            {
                classpathElements = project.getTestClasspathElements();
            }
            else if ( Artifact.SCOPE_COMPILE.equalsIgnoreCase( this.classpathScope ) )
            {
                classpathElements = project.getCompileClasspathElements();
            }
            else if ( Artifact.SCOPE_RUNTIME.equalsIgnoreCase( this.classpathScope ) )
            {
                classpathElements = project.getRuntimeClasspathElements();
            }
            else if ( Artifact.SCOPE_SYSTEM.equalsIgnoreCase( this.classpathScope ) )
            {
                classpathElements = project.getSystemClasspathElements();
            }
            else
            {
                throw new MojoExecutionException( "Invalid classpathScope: " + this.classpathScope +
                    " valid values are: compile, test, runtime, system." );
            }
        }
        catch ( DependencyResolutionRequiredException e )
        {
            throw new MojoExecutionException( e.getLocalizedMessage(), e );
        }

        return classpathElements;
    }

    /**
     * Gets the path to the generated webapp directory for war projects.
     * 
     * @return The path to the generated webapp directory or the empty string if this project is not a war project.
     */
    protected String getWebRoot() {
    	if(!project.getPackaging().equalsIgnoreCase("war"))
    		return "";
    	
    	return Utils.makeRelative(webappDirectory, projectDir);
    }
	
	private void configureVariables() throws OunceCoreException, ComponentLookupException {
        if ( pathVariableMap == null )
            pathVariableMap = new HashMap();
        
        pathVariableMap.put(M2_REPO, local.getBasedir());
        getCore().createPathVariables(pathVariableMap, installDir, this.getLog());
	}
    
    /**
     * @return the classpathScope
     */
    protected String getClasspathScope()
    {
        return this.classpathScope;
    }

    /**
     * @param theClasspathScope the classpathScope to set
     */
    protected void setClasspathScope( String theClasspathScope )
    {
        this.classpathScope = theClasspathScope;
    }

    /**
     * @return the jdkName
     */
    protected String getJdkName()
    {
        return this.jdkName;
    }

    /**
     * @param theJdkName the jdkName to set
     */
    protected void setJdkName( String theJdkName )
    {
        this.jdkName = theJdkName;
    }
    
    /**
     * @return the includeTestSources
     */
    protected boolean isIncludeTestSources()
    {
        return this.includeTestSources;
    }

    /**
     * @param theIncludeTestSources the includeTestSources to set
     */
    protected void setIncludeTestSources( boolean theIncludeTestSources )
    {
        this.includeTestSources = theIncludeTestSources;
    }

    /**
     * @return the java compiler options
     */
    protected String getJavaCompilerOptions()
    {
        return javaCompilerOptions;
    }

    /**
     * @param theJavaCompilerOptions the java compiler options
     */
    protected void setJavaCompilerOptions( String theJavaCompilerOptions )
    {
        this.javaCompilerOptions = theJavaCompilerOptions;
    }
    
    /**
     * @return whether to analyze Struts framework
     */
    protected boolean getAnalyzeStrutsFramework() {
    	return this.analyzeStrutsFramework;
    }
    
    /**
     * @param toAnalyzeStrutsFramework whether to analyze Struts Framework
     */
    protected void setAnalyzeStrutsFramework(boolean analyzeStrutsFramework) {
    	this.analyzeStrutsFramework = analyzeStrutsFramework;
    }
    
    /**
     * @return whether to import Struts validation
     */
    protected boolean getImportStrutsValidation() {
    	return this.importStrutsValidation;
    }
    
    /**
     * @param importStrutsValidation whether to import Struts validation 
     */ 
    protected void setImportStrutsValidation(boolean importStrutsValidation) {
    	this.importStrutsValidation = importStrutsValidation;
    }
    
    /**
     * @return the local
     */
    public ArtifactRepository getLocal()
    {
        return this.local;
    }

    /**
     * @param theLocal the local to set
     */
    public void setLocal( ArtifactRepository theLocal )
    {
        this.local = theLocal;
    }

	public boolean isPrecompileScan() {
		return precompileScan;
	}

	public void setPrecompileScan(boolean precompileScan) {
		this.precompileScan = precompileScan;
	}

	public String getExternalJars() {
		return externalJars;
	}

	public void setExternalJars(String externalJars) {
		this.externalJars = externalJars;
	}
    
    public boolean getPrecompileScan() {
    	return precompileScan;
    }
    
    public String getProjectDir() {
    	return projectDir;
    }

}
