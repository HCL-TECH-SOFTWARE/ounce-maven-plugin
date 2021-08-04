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

import java.util.Map;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class OunceCoreScan
{
    String applicationFile;

    String assessmentName;

    String assessmentOutput;
    
    String scanconfig;
    
    String appserver_type;

    String caller;

    String reportType;

    String reportOutputType;

    String reportOutputLocation;
    
    boolean includeHowToFix;
    
    boolean includeTraceDefinitive;
    
    boolean includeTraceSuspect;
    
    boolean includeTraceCoverage;

    boolean publish;

    Map OunceOptions;

    public OunceCoreScan( String theApplicationFile, String theAssessmentName, String theAssessmentOutput,
                          String theScanconfig, String theAppserver_type,String theCaller, String theReportType, String theReportOutputType,
                          String theReportOutputLocation, boolean includeHowToFix, boolean theIncludeTraceDefinitive, boolean theIncludeTraceSuspect, 
                          boolean theIncludeTraceCoverage, boolean thePublish, Map theOunceOptions )
    {
        super();
        this.applicationFile = theApplicationFile;
        this.assessmentName = theAssessmentName;
        this.assessmentOutput = theAssessmentOutput;
        this.scanconfig = theScanconfig;
        this.caller = theCaller;
        this.reportType = theReportType;
        this.reportOutputLocation = theReportOutputLocation;
        this.reportOutputType = theReportOutputType;
        this.includeHowToFix = includeHowToFix;
        this.publish = thePublish;
        this.OunceOptions = theOunceOptions;
        this.appserver_type = theAppserver_type;
        this.includeTraceDefinitive = theIncludeTraceDefinitive;
        this.includeTraceSuspect = theIncludeTraceSuspect;
        this.includeTraceCoverage = theIncludeTraceCoverage;
    }

    /**
     * @return the assessmentName
     */
    public String getAssessmentName()
    {
        return this.assessmentName;
    }

    /**
     * @return the assessmentOutput
     */
    public String getAssessmentOutput()
    {
        return this.assessmentOutput;
    }

    /**
     * @return the caller
     */
    public String getCaller()
    {
        return this.caller;
    }

    /**
     * @return the reportType
     */
    public String getReportType()
    {
        return this.reportType;
    }

    /**
     * @return the publish
     */
    public boolean isPublish()
    {
        return this.publish;
    }

    /**
     * @return the applicationFile
     */
    public String getApplicationFile()
    {
        return this.applicationFile;
    }

    /**
     * @return the ounceOptions
     */
    public Map getOunceOptions()
    {
        return this.OunceOptions;
    }

    public String getReportOutputType()
    {
        return reportOutputType;
    }

    public String getReportOutputLocation()
    {
        return reportOutputLocation;
    }

	public String getScanconfig() {
		return scanconfig;
	}

	public String getAppserver_type() {
		return appserver_type;
	}
	
	public boolean isIncludeHowToFix() {
		return includeHowToFix;
	}
	
	public boolean isIncludeTraceDefinitive() {
		return includeTraceDefinitive;
	}

	public boolean isIncludeTraceSuspect() {
		return includeTraceSuspect;
	}

	public boolean isIncludeTraceCoverage() {
		return includeTraceCoverage;
	}
	
	
}
