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
package org.codehaus.mojo.ounce.TestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.ounce.core.OunceCore;
import org.codehaus.mojo.ounce.core.OunceCoreApplication;
import org.codehaus.mojo.ounce.core.OunceCoreException;
import org.codehaus.mojo.ounce.core.OunceCoreProject;
import org.codehaus.mojo.ounce.core.OunceCorePublishASE;
import org.codehaus.mojo.ounce.core.OunceCoreScan;
import org.codehaus.mojo.ounce.core.OunceProjectBean;

/**
 * This is a test implementation of the core that can be injected to capture and analyze the output.
 * 
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class OunceCoreMock
    implements OunceCore
{

    String baseDir;

    OunceCoreApplication app;

    OunceCoreProject project;

    OunceCoreScan scan;
    
    OunceCorePublishASE publish;

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.mojo.ounce.core.OunceCore#createApplication(java.lang.String, java.lang.String, java.util.List,
     *      boolean, boolean, org.apache.maven.plugin.logging.Log)
     */
    public void createApplication( String baseDir, String theName, String applicationFile, List projects, Map ounceOptions, Log theLog )
        throws OunceCoreException
    {

        app = new OunceCoreApplication( applicationFile, applicationFile, projects, ounceOptions );

        baseDir = applicationFile.substring(0, applicationFile.lastIndexOf(File.separator)-1);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.mojo.ounce.core.OunceCore#createProject(java.lang.String, java.lang.String, java.util.List,
     *      java.lang.String, java.lang.String, java.lang.String, boolean,
     *      org.codehaus.mojo.ounce.core.OunceCoreCompilerOptions, java.lang.String, java.util.Set, java.util.Set,
     *      org.apache.maven.plugin.logging.Log)
     */
    public void createProject( String theBaseDir, String name, String jspCompilerInfoName, String jspCompilerType, String projectRoot, List sourceRoots, String webRoot,
                               String classPath, String jdkName, String compilerOptions, String packaging,
                               Map ounceOptions, boolean forceWeb, boolean analyzeStrutsFramework, boolean importStrutsValidation,
                               String projectDir, Log theLog )
        throws OunceCoreException
    {
        project =
            new OunceCoreProject( name, projectRoot, sourceRoots, webRoot, classPath, jdkName, packaging,
                                  compilerOptions, ounceOptions );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.mojo.ounce.core.OunceCore#readApplication(java.lang.String,
     *      org.apache.maven.plugin.logging.Log)
     */
    public OunceCoreApplication readApplication( String thePath, Log theLog )
        throws OunceCoreException
    {
        List projects = new ArrayList();
        projects.add( new OunceProjectBean( "e", "e" ) );
        projects.add( new OunceProjectBean( "uis/f", "f" ) );
        projects.add( new OunceProjectBean( "uis/g", "g" ) );

        OunceCoreApplication appl = new OunceCoreApplication( "testApp", thePath, projects, null );

        return appl;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.mojo.ounce.core.OunceCore#readProject(java.lang.String, org.apache.maven.plugin.logging.Log)
     */
    public OunceCoreProject readProject( String thePath, Log theLog )
        throws OunceCoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.mojo.ounce.core.OunceCore#scan(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, boolean, org.apache.maven.plugin.logging.Log)
     */
    public void scan( String applicationFile, String assessmentName, String assessmentOutput, String scanconfig, String caller,
                      String reportType, String reportOutputType, String reportOutputLocation, boolean publish,
                      Map ounceOptions, String installDir, boolean wait, boolean includeTraceDefinitive, boolean includeTraceSuspect, 
                      boolean includeTraceCoverage, String appserver_type, Log log )
        throws OunceCoreException
    {
        scan =
            new OunceCoreScan( applicationFile, assessmentName, assessmentOutput, scanconfig, caller, reportType, reportOutputType,
                               reportOutputLocation, installDir, publish, wait, wait, wait, ounceOptions );
    }

    /**
     * @return the baseDir
     */
    public String getBaseDir()
    {
        return this.baseDir;
    }

    /**
     * @return the app
     */
    public OunceCoreApplication getApp()
    {
        return this.app;
    }

    /**
     * @return the project
     */
    public OunceCoreProject getProject()
    {
        return this.project;
    }

    /**
     * @return the scan
     */
    public OunceCoreScan getScan()
    {
        return this.scan;
    }

    public void createPathVariables( Map pathVariableMap, String installDir, Log log )
        throws OunceCoreException
    {
        // TODO Auto-generated method stub

    }

	public void publishASE(String assessmentFile, String caller,
			String folderID, String installDir, boolean wait, Log log)
			throws OunceCoreException {
		
		publish = new OunceCorePublishASE(assessmentFile, caller, folderID, installDir, wait);
		
	}
}
