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

//import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.ounce.core.OunceCore;
import org.codehaus.mojo.ounce.core.OunceCoreException;
import org.codehaus.mojo.ounce.utils.Utils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.StringUtils;

/**
 * This mojo allows an on demand scan of an application and the optional publishing of the results.
 * 
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * 
 */
@Mojo (name="scan", aggregator=true)
@Execute( lifecycle="scan", phase=LifecyclePhase.PACKAGE )
public class ScanMojo
    extends AbstractOunceMojo
{

    /**
     * The location of the application file (.paf) to scan.</br>
     * DO NOT USE
     * 
     */
	@Parameter (property="ounce.applicationFile", defaultValue="${ounce.appDir}/${project.artifactId}.paf")
    String applicationFile;

    /**
     * A name to help identify the assessment.
     * 
     * 
     */
	@Parameter (property="ounce.assessmentName")
    String assessmentName;

    /**
     * A filename to which to save the assessment. <br/> If filename is not specified, Ounce/Maven generates a name
     * based on the application name and timestamp and saves it to the applications working directory.</br>
     * Command line variable: -Dounce.assessmentOutput</br>
     * Example: -Dounce.assessmentOutput="MyAssessment.ozasmt"
     * 
     * 
     */
	@Parameter (property="ounce.assessmentOutput")
    String assessmentOutput;

    /**
     * A short string to help identify the corresponding entries in the ounceauto log file.<br/>
     * Command line variable: -Dounce.caller
     * 
     * 
     */
	@Parameter (property="ounce.caller")
    String caller;

    /**
     * Generates an Ounce report of the specified type, including findings reports, SmartAudit Reports, and, if
     * available, custom reports. Ounce/Maven generates a report for this assessment after the scan completes. <br/> The
     * following report types are included: Findings, Findings By CWE, Findings By API, Findings By Classification,
     * Findings By File, Findings By Type, Findings By Bundle, OWASP Top Ten, PCI Data Security Standard, Ounce Software
     * Security Profile, or OWASP Top Ten 2007. If you specify reportType, then reportOutputType and
     * reportOutputPath are required.</br>
     * Command line variable: -Dounce.reportType</br>
     * Example: -Dounce.reportType="Findings"
     * 
     * 
     */
	@Parameter (property="ounce.reportType")
    String reportType;

    /**
     * The output to generate for the report specified in reportType. Required with reportType. Output type may be html,
     * zip, pdf-summary, pdf-detailed, pdf-comprehensive, or pdf-annotated.</br>
     * Command line variable: -Dounce.reportOutputType</br>
     * Example: -Dounce.reportOutputType="html"
     * 
     * 
     */
	@Parameter (property="ounce.reportOutputType")
    String reportOutputType;

    /**
     * The path to which to write the report specified in reportType. Required with reportType.</br>
     * Command line variable: -Dounce.reportOutputPath</br>
     * Example: -Dounce.reportOutputPath="C:\MyReports"
     * 
     * 
     */
	@Parameter (property="ounce.reportOutputPath")
    String reportOutputPath;

    /**
     * Number of lines of source code to include in the report before each finding.</br>
     * Command line variable: -Dounce.includeSrcBefore</br>
     * Example: -Dounce.includeSrcBefore=5
     * 
     * 
     */
	@Parameter (property="ounce.includeSrcBefore")
    int includeSrcBefore = -1;

    /**
     * Number of lines of source code to include in the report after each finding.</br>
     * Command line variable: -Dounce.includeSrcAfter</br>
     * Example: -Dounce.includeSrcAfter=5
     * 
     * 
     */
	@Parameter (property="ounce.includeSrcAfter")
    int includeSrcAfter = -1;
    
    /**
     * Include trace information in the report for definitive findings. </br>
     * Command line variable: -Dounce.includeTraceDefinitive</br>
     * Example: -Dounce.includeTraceDefinitive=true
     * 
     * 
     */
	@Parameter (property="ounce.includeTraceDefinitive")
    boolean includeTraceDefinitive = false;
    
    /**
     * Include trace information in the report for suspect findings. </br>
     * Command line variable: -Dounce.includeTraceSuspect</br>
     * Example: -Dounce.includeTraceSuspect=true
     * 
     * 
     */
	@Parameter (property="ounce.includeTraceSuspect")
    boolean includeTraceSuspect = false;
    
    /**
     * Include trace information in the report for scan coverage findings. </br>
     * Command line variable: -Dounce.includeTraceCoverage
     * Example: -Dounce.includeTraceCoverage=true
     * 
     * 
     */
	@Parameter (property="ounce.includeTraceCoverage")
    boolean includeTraceCoverage = false;

    /**
     * Automatically publish the assessment following the completion of the scan.</br>
     * Command line variable: -Dounce.publish</br>
     * Example: -Dounce.publish=true
     * 
     * 
     */
	@Parameter (property="ounce.publish", defaultValue="false")
    boolean publish;

    /**
     * The location of the Ounce client installation directory if the Ounce client is not on the path.</br>
     * Command line variable: -Dounce.installDir </br>
     * Example: Dounce.installDir="C:\Program Files (x86)\IBM\AppScanSource"
     * 
     * 
     */
	@Parameter (property="ounce.installDir")
    String installDir;

    /**
     * Forces the goal to wait until the scan finishes, thus blocking the Maven build. This is useful if the scan is
     * being performed from the report mojo as part of integration with the site target and the site is getting
     * deployed.</br>
     * Command line variable: -Dounce.wait</br>
     * Example: -Dounce.wait=true
     * 
     * 
     */
	@Parameter (property="ounce.wait", defaultValue="false")
    boolean waitForScan;
    //private final String SPACE="\u0000";
    /**
     * Allows a scan configuration to be specified for the scan </br>
     * Command line variable: -Dounce.scanconfig</br>
     * Example: -Dounce.scanconfig="Normal scan"
     * 
     * 
     * 
     */
	@Parameter (property="ounce.scanconfig")
    String scanConfig;
    
    /**
     * 
     * 
     */
	@Parameter (property="ounce.projectFile")
    String projectFile;
    
    /**
     * If the application that you are opening includes JavaServer Pages (for example, a 
     * WAR or EAR file), use this setting to specify the application server to use for JSP
     * compilation. Specify one of these, in double quotation marks: Tomcat 5, Tomcat 6,
     * Tomcat 7, Tomcat 8, WebSphere 6.1, WebSphere 7.0, WebSphere 8.0, WebSphere 8.5,
     * WebLogic 8, WebLogic 9, WebLogic 11g or WebLogic 12c
     * Command line variable: -Dounce.appserver_type
     * Example: -Dounce.appserver_type="WebSphere 8.5"
     * 
     * 
     */
	@Parameter (property="ounce.appserver_type")
    String appserver_type;

    /**
     * This is a static variable used to persist the cached results across plugin invocations.
     */
    protected static Set cache = new HashSet();

    // private List projects;
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( StringUtils.isEmpty( applicationFile ) )
        {
            throw new MojoExecutionException( "\'applicationFile\' must be defined." );
        }

        // check my cache to see if this particular set of params has already been scanned.
        if ( shouldExecute() )
        {
            try
            {
                if ( includeSrcAfter != -1 || includeSrcBefore != -1 )
                {
                    if ( options == null )
                    {
                        options = new HashMap();
                    }
                    options.put( "includeSrcAfter", new Integer( includeSrcAfter ) );
                    options.put( "includeSrcBefore", new Integer( includeSrcBefore ) );
                }
                OunceCore core = getCore();
                core.scan( Utils.convertToVariablePath( applicationFile, pathVariableMap ), assessmentName,
                           assessmentOutput, scanConfig,caller, reportType, reportOutputType, reportOutputPath, publish,
                           this.options, this.installDir, waitForScan, includeTraceDefinitive, includeTraceSuspect,
                           includeTraceCoverage, appserver_type, getLog() );
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
            this.getLog().info(
                                "Skipping Scan because these same parameters where already used in a scan for this project during this build. (build was probably forked)" );
        }
    }

    /**
     * This method checks the cache to see if the scan should be run. It is used to avoid multiple invocations of scan
     * in the instance of a forked build.
     * 
     * @return boolean value to see if the scan should be run
     */
    protected boolean shouldExecute()
    {
        // get the hash and try to add it.
        // if it was added, then we need to execute, otherwise it was already there
        // and we don't.
        return ( cache.add( getParameterHash() ) );
    }

    /**
     * This method returns a hash of the parameters that may influence scan results. The result of this is used to
     * determine if a scan should be rerun within the same build lifetime. If any parameters change, then the scan will
     * continue. This allows multiple executions to be defined with slightly different parameters.
     * 
     * @return String of hashCodes
     */
    protected String getParameterHash()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( getSafeHash( this.applicationFile ) );
        buf.append( "-" );
        buf.append( getSafeHash( this.assessmentOutput ) );
        buf.append( "-" );
        buf.append( getSafeHash( this.caller ) );
        buf.append( "-" );
        buf.append( getSafeHash( this.pathVariableMap ) );
        buf.append( "-" );
        buf.append( getSafeHash( this.reportOutputPath ) );
        buf.append( "-" );
        buf.append( getSafeHash( this.reportOutputType ) );
        buf.append( "-" );
        buf.append( getSafeHash( this.reportType ) );
        this.getLog().debug( "Parameter Hash: " + buf.toString() );
        return buf.toString();
    }

    /**
     * Simple helper to handle null parameters for hash checking.
     * 
     * @param o
     * @return
     */
    private final int getSafeHash( Object o )
    {
        if ( o != null )
        {
            return o.hashCode();
        }
        else
        {
            return 0;
        }
    }

}
