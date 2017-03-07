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
package org.codehaus.mojo.ounce.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.ounce.ProjectOnlyMojo;
import org.codehaus.plexus.util.StringUtils;

import com.thoughtworks.xstream.XStream;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * @author <a href="mailto:sam.headrick@ouncelabs.com">Sam Headrick</a>
 * @plexus.component role="org.codehaus.mojo.ounce.core.OunceCore" role-hint="test-xml"
 */
public class OunceCoreXml
    implements OunceCore
{

    /*
     * (non-Javadoc)
     * 
     * @see com.ouncelabs.plugins.OunceCore#createApplication(java.lang.String, java.io.File, java.util.List, boolean,
     *      boolean)
     */
    public void createApplication( String baseDir, String theName, String applicationDir, List theProjects, Map ounceOptions, Log log )
        throws OunceCoreException
    {
        // sort them to avoid implementation details messing
        // up the order for testing.
        Collections.sort( theProjects );
        log.info( "Writing parameters to xml." );
        OunceCoreApplication bean = new OunceCoreApplication( theName,applicationDir, theProjects, ounceOptions );
        String appDir = applicationDir;
        XStream xs = new XStream();

        xs.alias( "application", OunceCoreApplication.class );
        xs.alias( "project", OunceProjectBean.class );
        try
        {
            File outFile = new File( appDir + File.separator + "target", "application.xml" );
            outFile.getParentFile().mkdirs();
            outFile.createNewFile();
            ObjectOutputStream out = xs.createObjectOutputStream( new FileWriter( outFile ) );
            out.writeObject( bean );
            out.close();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ouncelabs.plugins.OunceCore#createProject(java.lang.String, java.io.File, java.util.List, java.io.File,
     *      java.lang.String, java.lang.String, boolean, com.ouncelabs.plugins.CompilerOptions)
     */
    public void createProject( String baseDir, String theName, String jspCompilerInfoName, String jspCompilerType, String theProjectRoot, List theSourceRoots,
                               String theWebRoot, String theClassPath, String theJdkName, String theCompilerOptions,
                               String packaging, Map ounceOptions, boolean forceWeb,
                               boolean analyzeStrutsFramework, boolean importStrutsValidation, String projectDir,
                               String[] srcRoot, Log log )
        throws OunceCoreException
    {
        if ( StringUtils.isNotEmpty( theClassPath ) )
        {
            // the classpath order can change subtly from
            // maven. Lets sort it alphabetically since we
            // really
            // only care about the contents for testing the
            // plugin
            String[] classp = theClassPath.split( ";" );
            Arrays.sort( classp );

            StringBuffer sb = new StringBuffer();
            if ( classp.length > 0 )
            {
                // first one, no separator needed
                sb.append( classp[0] );

                // separate the rest of them with
                // pathSeparator
                for ( int i = 1; i < classp.length; i++ )
                {
                    sb.append( File.pathSeparator );
                    sb.append( classp[i] );
                }
                theClassPath = sb.toString();
            }
        }

        log.info( "OunceCoreXML: Writing parameters to xml." );
        OunceCoreProject bean =
            new OunceCoreProject( theName, theProjectRoot, theSourceRoots, theWebRoot, theClassPath, theJdkName,
                                  packaging, theCompilerOptions, ounceOptions );
        XStream xs = new XStream();
        xs.alias( "project", OunceCoreProject.class );
        log.info("OuncereXML: theProjectRoot" + theProjectRoot);
        try
        {
        	File outFile = new File(baseDir);
        	log.info("Outfile: " + baseDir);
        	
        	if(!projectDir.isEmpty())
        	{
        		log.info("OuncereXML: ProjectDir has been set to " + theProjectRoot);
        		outFile = new File( theProjectRoot + File.separator + "target", "project.xml" );
        	}
        	else
        	{
        		outFile = new File( baseDir + File.separator + "target", "project.xml" );
        	}
            
            outFile.getParentFile().mkdirs();
            outFile.createNewFile();
            ObjectOutputStream out = xs.createObjectOutputStream( new FileWriter( outFile ) );
            out.writeObject( bean );
            out.close();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.mojo.ounce.core.OunceCore#readApplication(java.lang.String)
     */
    public OunceCoreApplication readApplication( String thePath, Log log )
        throws OunceCoreException
    {
        XStream xs = new XStream();
        xs.alias( "application", OunceCoreApplication.class );
        xs.alias( "project", OunceProjectBean.class );

        File inFile = new File( thePath + File.separator + "target", "application.xml" );
        try
        {
            FileInputStream is = new FileInputStream( inFile );
            OunceCoreApplication app = (OunceCoreApplication) xs.fromXML( is );
            is.close();
            return app;
        }
        catch ( IOException e )
        {
            throw new OunceCoreException( "Unable to read file:" + inFile.getPath(), e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.mojo.ounce.core.OunceCore#readProject(java.lang.String)
     */
    public OunceCoreProject readProject( String thePath, Log log )
        throws OunceCoreException
    {
        XStream xs = new XStream();
        xs.alias( "project", OunceCoreProject.class );

        File inFile = new File( thePath + File.separator + "target", "project.xml" );
        log.info("OuncereXML: thePath: " + thePath);
        try
        {
            FileInputStream is = new FileInputStream( inFile );
            OunceCoreProject project = (OunceCoreProject) xs.fromXML( is );
            is.close();
            return project;
        }
        catch ( IOException e )
        {
            throw new OunceCoreException( "Unable to read file:" + inFile.getPath(), e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.mojo.ounce.core.OunceCore#scan(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, boolean, java.util.Map, java.lang.String, boolean,
     *      org.apache.maven.plugin.logging.Log)
     */
    public void scan( String theApplicationFile, String theAssessmentName, String theAssessmentOutput,
                      String theScanconfig, String theCaller, String theReportType, String theReportOutputType,
                      String theReportOutputLocation, boolean thePublish, Map theOunceOptions, String installDir,
                      boolean wait, boolean theIncludeTraceDefinitive, boolean theIncludeTraceSuspect,
                      boolean theIncludeTraceCoverage, String theAppserver_type, Log theLog )
        throws OunceCoreException
    {

        OunceCoreScan bean =
            new OunceCoreScan( theApplicationFile, theAssessmentName, theAssessmentOutput, theScanconfig, theAppserver_type,theCaller, theReportType,
                               theReportOutputType, theReportOutputLocation, theIncludeTraceDefinitive, theIncludeTraceSuspect,
                               theIncludeTraceCoverage,thePublish, theOunceOptions );

        {
            XStream xs = new XStream();
            xs.alias( "scan", OunceCoreScan.class );
            try
            {
                File outFile = new File( "./target", "scan.xml" );
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();
                ObjectOutputStream out = xs.createObjectOutputStream( new FileWriter( outFile ) );
                out.writeObject( bean );
                out.close();
            }
            catch ( IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void createPathVariables( Map pathVariableMap, String installDir, Log log )
        throws OunceCoreException
    {
        // TODO Auto-generated method stub

    }

	public void publishASE(String assessmentFile, String caller,
			String folderID, String installDir, boolean wait, Log log)
			throws OunceCoreException {
		OunceCorePublishASE pub = new OunceCorePublishASE(assessmentFile, caller, folderID, installDir, wait);
		{
			XStream xs = new XStream();
			xs.alias( "pub", OunceCorePublishASE.class );
			 try
	            {
	                File outFile = new File( "./target", "pub.xml" );
	                outFile.getParentFile().mkdirs();
	                outFile.createNewFile();
	                ObjectOutputStream out = xs.createObjectOutputStream( new FileWriter( outFile ) );
	                out.writeObject( pub );
	                out.close();
	            }
	            catch ( IOException e )
	            {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
		}
	}
}
