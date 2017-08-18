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
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.StringUtils;

public class Utils
{
    static final String propertyFormat = "%**%";

    public static String convertClasspathElement(String classpathElement, String ounceProjectDir, Map ounceVariableMap )
    {
    	String ret = convertToVariablePath(classpathElement, ounceVariableMap);
    	if(ret.equals(classpathElement) && ounceProjectDir != null)
			ret = makeRelative(classpathElement, ounceProjectDir);

        return ret;
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
     * Converts the given List of paths to relative paths using baseDir as the base directory.
     * 
     * @param paths original paths in a list
     * @param baseDir The directory to use as the base of the relative paths.
	 *
     * @return A List of relative paths
     */
    static public List<String> convertToRelativePaths(List<String> paths, String baseDir)
    {
        ArrayList<String> relativePaths = new ArrayList<String>();
        for(String path : paths)
        	relativePaths.add(makeRelative(path, baseDir));
        
        return relativePaths;
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
		String[] childPathElements = childPath.replaceAll("\\\\", "/").split("/");
		String[] parentPathElements = parentPath.replaceAll("\\\\", "/").split("/");
		
		int counter = 0;
		int limit = parentPathElements.length;
		
		if(childPathElements.length < parentPathElements.length)
		{
			limit = childPathElements.length;
		}

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
	}
	
	public static String makeRelative(String sPathToBeMadeRelative, String sReferenceDir)
	{
		//Ensure absolute paths.
		try {
			File dir = new File(sReferenceDir);
			sReferenceDir = dir.getCanonicalPath();
			dir = new File(sPathToBeMadeRelative);
			sPathToBeMadeRelative = dir.getCanonicalPath();
		} catch (IOException e) {
			return sPathToBeMadeRelative;
		}
		
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
	
	/**
	 * Gets a List of files in the given directory of a specified type. 
	 * @param directory The directory to search.
	 * @param extension The file extension to match.
	 * @return A List<String> of files of the given type or null if the given directory doesn't exist.
	 */
	public static List<String> getFilesOfType(File directory, String extension) {
		List<String> ret = new ArrayList<String>();

		if(directory.isDirectory()) {
			for(File file : directory.listFiles()) {
				if(file.isFile() && file.getName().endsWith(extension))
					ret.add(file.getAbsolutePath());
				else
					ret.addAll(getFilesOfType(file, extension));
			}
		}
		return ret;
	}
}
