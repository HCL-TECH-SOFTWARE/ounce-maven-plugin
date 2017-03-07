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
package org.codehaus.mojo.ounce.its;

/**
 * This class executes the IT tests. The setup will create a pom-test.xml from the plugin pom. The version is changed to
 * "test" and the tests themselves turned off to avoid an infinite loop. The test version of the plugin is then built
 * and installed to a new temporary local repo used to execute the tests. This only occurs once for the suite of tests.
 * Each test below just uses the tools to execute Maven on the named project with the passed in goals.
 * 
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a> Copied from the Eclipse AbstractEclipsePluginTestCase v2.4
 */
public class OunceProjectMojoITTest
    extends AbstractOuncePluginITCase
{

    public void testProject1()
        throws Exception
    {
        testProject( "project1", props, "org.codehaus.mojo:ounce-maven-plugin:project" );
    }

    public void testProject2()
        throws Exception
    {
        testProject( "project2", props, "org.codehaus.mojo:ounce-maven-plugin:project" );
    }

    public void testProject3()
        throws Exception
    {
        testProject( "project3", props, "org.codehaus.mojo:ounce-maven-plugin:project" );
    }

    public void testProject4()
        throws Exception
    {
        testProject( "project4", props, "org.codehaus.mojo:ounce-maven-plugin:project" );
    }

    public void testProject5()
        throws Exception
    {
        testProject( "project5", props, "org.codehaus.mojo:ounce-maven-plugin:project" );
    }

    public void testProject6()
        throws Exception
    {
        testProject( "project6", props, "org.codehaus.mojo:ounce-maven-plugin:project-only" );
    }

    public void testProject7()
        throws Exception
    {
        // test config bound in pom
        testProject( "project7", props, "package" );
    }

}
