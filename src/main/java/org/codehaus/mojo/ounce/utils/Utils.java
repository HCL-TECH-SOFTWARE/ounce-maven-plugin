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

import java.io.File;
import java.nio.file.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class Utils
{
    static final String propertyFormat = "%**%";

    public static String convertClasspathElement(String classpathElement, String ounceProjectDir, Map ounceVariableMap )
    {
    	String classpathelementWithVariableReplacement = convertToVariablePath(classpathElement, ounceVariableMap);
    	if(classpathelementWithVariableReplacement.equals(classpathElement)) {
			if(new File(classpathElement).isAbsolute()) {
				classpathElement = makeRelative(classpathElement, ounceProjectDir);
			}
    		return classpathElement;
    	}
        return classpathelementWithVariableReplacement;
    }

    /**
     * @param path
     * @param pathVariableMap
     * @return
     */
    static public String convertToVariablePath( String path, Map pathProperties ) {
        if ( path != null && pathProperties != null && pathProperties.size() > 0 ) {
            for ( Iterator iter = pathProperties.entrySet().iterator(); iter.hasNext(); ) {
                Entry entry = (Entry) iter.next();
                if ( StringUtils.isNotEmpty( (String) entry.getKey() ) ) {
                    String formattedKey = StringUtils.replace( propertyFormat, "**", (String) entry.getKey() );
                    path = StringUtils.replace( path, (String) entry.getValue(), formattedKey );
                }
            }
        }
        return path;
    }

    /**
     * Performs the path/key substitution for all paths.
     * 
     * @param paths
     * @param pathVariableMap map of key/path pairs to replace
     * @return processed list
     */
    static public List convertToPropertyPaths( List paths, Map pathProperties )
    {
        List result = paths;
        if ( pathProperties != null && pathProperties.size() > 0 )
        {
            for ( Iterator iter = pathProperties.entrySet().iterator(); iter.hasNext(); )
            {
                Entry entry = (Entry) iter.next();
                result = convertToRelativePaths( result, (String) entry.getValue(), (String) entry.getKey() );
            }
        }
        return result;
    }

    /**
     * Removes the pathToRemove from the paths and optionally replaces it with a key
     * 
     * @param paths original paths in a list
     * @param pathToRemove string to replace
     * @param key string to replace with
     * @return normalized string beginning with the key
     */
    static public List convertToRelativePaths( List paths, String pathToRemove, String key )
    {
        // go through the list, replace remove the
        // strings
        List newPaths = new ArrayList( paths.size() );
        Iterator iter = paths.iterator();

        while ( iter.hasNext() )
        {
        	String next = (String)iter.next();
            //newPaths.add( convertToRelativePath( (String) iter.next(), pathToRemove, key ) );
        	
        	if(isConvertible(next, pathToRemove))
        	{
        		newPaths.add(makeRelative(next, pathToRemove));
        	}
        	else
        	{
        		newPaths.add(next);
        	}
        }
        return newPaths;
    }

    /**
     * Use reflection to generate a toString with all parameters.
     * 
     * @param obj
     * @return
     */
    public static synchronized String getDynamicToString( Object obj )
    {
        StringBuffer buf = new StringBuffer();

        try
        {
            Class clazz = obj.getClass();
            buf.append( clazz.getName() + ": " );

            Field[] fields = clazz.getDeclaredFields();
            for ( int i = 0; i < fields.length; i++ )
            {
                buf.append( " " );
                buf.append( fields[i].getName() );
                buf.append( "= " );
                try
                {
                    fields[i].setAccessible( true );

                    buf.append( fields[i].get( obj ) );
                    buf.append( " " );
                }
                catch ( Exception e )
                {
                    buf.append( "Error Retrieving Value " );
                }
            }
        }
        catch ( Exception e )
        {
            buf.append( "Exception: " + e.getMessage() );
        }
        return buf.toString();
    }
	
	public static boolean isConvertible(String childPath,String parentPath)
	{
		/*File child = new File(childPath.trim());
		File parent = new File(parentPath.trim());*/
		
		
		/*String[] childPathElements = child.toString().split(File.separator);
		String[] parentPathElements = parent.toString().split(File.separator);*/
		
		String[] childPathElements = childPath.replaceAll("\\\\", "/").split("/");
		String[] parentPathElements = parentPath.replaceAll("\\\\", "/").split("/");
		
		int counter = 0;
		int limit = parentPathElements.length;
		
		if(childPathElements.length < parentPathElements.length)
		{
			limit = childPathElements.length;
		}
		
		//System.out.println("loop limit " + limit);
		//System.out.println("number of child elements: " + childPathElements.length);
		
		for(int index = 0; index < limit; index++)
		{
			
			if(parentPathElements[index].equals(childPathElements[index]))
			{
				counter++;
				continue;
			}
			else
			{
				if(counter <=1)
				{
					return false;
				}
				else
				{
					return true;
				}
				
			}
		}
		return true;
	}// end of method isConvertible
	
	public static String makeRelative(String sPathToBeMadeRelative, String sReferenceDir)
	{
		// TODO: Check parameters
		
		Path pathToBeMadeRelative = Paths.get(sPathToBeMadeRelative);
		Path referenceDir = Paths.get(sReferenceDir);
		Path relativePath = referenceDir.relativize(pathToBeMadeRelative);

		String sRelativePath = relativePath.toString();
		// Paths are the same.
		if(sRelativePath.length() == 0) {
			return ".";
		}
		// sPathToBeMadeRelative contains complete sReferenceDir (added for clarity)
		if(!sRelativePath.startsWith(".") && !sRelativePath.startsWith(File.separator)) {
			sRelativePath = "." + File.separator + sRelativePath;
		}
		
		return sRelativePath;
	}
	
	public static void main(String[] args) {
		Path p1 = Paths.get("/home/smatthiesen/Code/WebGoat-develop");
		Path p2 = Paths.get("/home/smatthiesen/Scan/AppScanSource/Projects");
		Path p3 = Paths.get("/home/smatthiesen/Code/WebGoat-develop/webgoat-container");
		Path p4 = Paths.get("/home/smatthiesen/Code/WebGoat-develop");
		
		Path p1_to_p2 = p1.relativize(p2);
		Path p1_to_p3 = p1.relativize(p3);
		Path p1_to_p4 = p1.relativize(p4);
		Path p3_to_p1 = p3.relativize(p1);
		
		System.out.println("p1 to p2: " + p1_to_p2.toString());
		System.out.println("p1 to p3: " + p1_to_p3.toString());
		System.out.println("p1 to p4: " + p1_to_p4.toString().length());
		System.out.println("p3 to p1: " + p3_to_p1.toString());
	}
}
