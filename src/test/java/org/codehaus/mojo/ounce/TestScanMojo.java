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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.ounce.TestUtils.OunceCoreMock;
import org.codehaus.mojo.ounce.TestUtils.ScanMojoMock;
import org.codehaus.mojo.ounce.core.OunceCoreScan;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class TestScanMojo
    extends TestCase
{
    public void testScanMojo()
        throws MojoFailureException, MojoExecutionException, ComponentLookupException
    {
        ScanMojo mojo = new ScanMojoMock();

        try
        {
            mojo.execute();
            fail( "Expected an exception because the appFile or appName are not populated" );
        }
        catch ( MojoExecutionException e )
        {

        }

        mojo.applicationFile = "my\\path/is/messed\\up";
        mojo.assessmentName = "assessName";
        mojo.assessmentOutput = "assessOut";
        mojo.caller = "caller";
        mojo.reportType = "my|report|type";
        mojo.project = new MavenProject();

        mojo.execute();

        OunceCoreMock core = (OunceCoreMock) mojo.getCore();
        OunceCoreScan scan = core.getScan();

        assertEquals( "my/path/is/messed/up", scan.getApplicationFile() );
        assertEquals( "assessName", scan.getAssessmentName() );
        assertEquals( "assessOut", scan.getAssessmentOutput() );
        assertEquals( "caller", scan.getCaller() );
        assertEquals( "my|report|type", scan.getReportType() );

        Map map = new HashMap();
        map.put( "mypath", "my/path" );
        map.put( "up", "up" );

        mojo.pathVariableMap = map;
        mojo.execute();
        core = (OunceCoreMock) mojo.getCore();

        // we should get a new scan object if the mojo actually executed.
        assertNotSame( scan, core.getScan() );
        scan = core.getScan();

        assertEquals( "%mypath%/is/messed/%up%", scan.getApplicationFile() );

        // now make sure we don't execute twice with the same params
        mojo.execute();
        assertSame( scan, core.getScan() );

    }
}
