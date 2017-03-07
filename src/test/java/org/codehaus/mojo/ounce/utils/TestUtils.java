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
package org.codehaus.mojo.ounce.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class TestUtils
    extends TestCase
{

    // small object for testing the dynamicToString
    class TestObj
    {
        private String a;

        private int b;

        private double c;

        public TestObj( String aa, int bb, double cc )
        {
            this.a = aa;
            this.b = bb;
            this.c = cc;
        }
    }

    public void testDynamicToString()
    {
        TestObj a = new TestObj( "foo", 1, 2.2 );

        String result = Utils.getDynamicToString( a );
        String expectedResult =
            "org.codehaus.mojo.ounce.utils.TestUtils$TestObj:  a= foo  b= 1  c= 2.2  this$0= testDynamicToString(org.codehaus.mojo.ounce.utils.TestUtils) ";
        assertEquals( expectedResult, result );
    }

    public void testFilePathConversion()
    {
        assertEquals( "./org/apache", Utils.makeRelative( "/repo/org/apache", "/repo") );
/* TODO: Switch over to new methods
        assertEquals( "%M2_REPO%/org/apache", Utils.PathConverter( "/repo/org/apache", "/repo", "M2_REPO" ) );
        assertEquals( "%M2_REPO%/org/apache;%M2_REPO%/com", Utils.PathConverter( "/repo/org/apache;/repo/com",
                                                                                         "/repo", "M2_REPO" ) );
        assertEquals( "%M2_REPO%/org/apache;%M2_REPO%/com",
                      Utils.PathConverter( "\\repo\\org\\apache;\\repo\\com", "\\repo", "M2_REPO" ) );
        assertEquals( "org/apache", Utils.PathConverter( "\\repo\\org\\apache", "\\repo", "" ) );
        assertEquals( ".", Utils.PathConverter( "/foo/something", "/foo/something", "" ) );
        assertEquals( "%key%", Utils.PathConverter( "/foo/something", "/foo/something", "key" ) );
*/
        List list = new ArrayList();
        list.add( "/repo/org/apache" );
        list.add( "/repo/org/apache/foo" );

        /*List result = Utils.PathConverter( list, "/repo", "" );

        assertEquals( list.size(), result.size() );
        assertEquals( "org/apache", result.get( 0 ) );
        assertEquals( "org/apache/foo", result.get( 1 ) );

        result = Utils.PathConverter( list, "/repo", "M2_REPO" );

        assertEquals( list.size(), result.size() );
        assertEquals( "%M2_REPO%/org/apache", result.get( 0 ) );
        assertEquals( "%M2_REPO%/org/apache/foo", result.get( 1 ) );*/

    }

    public void testFilePathConversionMaps()
    {
        Map properties = new HashMap();
        properties.put( "M2_REPO", "/repo" );
        properties.put( "SOMEPLACE", "someplace" );

        assertEquals( "%M2_REPO%", Utils.convertToVariablePath( "/repo", properties ) );
        assertEquals( "%SOMEPLACE%", Utils.convertToVariablePath( "someplace", properties ) );
        assertEquals( "%M2_REPO%/%SOMEPLACE%", Utils.convertToVariablePath( "/repo/someplace", properties ) );
        assertEquals( "/repo/someplace", Utils.convertToVariablePath( "/repo/someplace", null ) );
        assertEquals( "/repo/someplace", Utils.convertToVariablePath( "/repo/someplace", new HashMap() ) );

        List list = new ArrayList( 3 );
        list.add( "/repo" );
        list.add( "someplace" );
        list.add( "/repo/someplace/foo" );

        // check null
        list = Utils.convertToPropertyPaths( list, null );

        assertTrue( list.contains( "/repo" ) );
        assertTrue( list.contains( "someplace" ) );
        assertTrue( list.contains( "/repo/someplace/foo" ) );

        // check empty map
        list = Utils.convertToPropertyPaths( list, new HashMap() );

        assertTrue( list.contains( "/repo" ) );
        assertTrue( list.contains( "someplace" ) );
        assertTrue( list.contains( "/repo/someplace/foo" ) );

        list = Utils.convertToPropertyPaths( list, properties );

        assertTrue( list.contains( "%M2_REPO%" ) );
        assertTrue( list.contains( "%SOMEPLACE%" ) );
        assertTrue( list.contains( "%M2_REPO%/%SOMEPLACE%/foo" ) );

    }
}
