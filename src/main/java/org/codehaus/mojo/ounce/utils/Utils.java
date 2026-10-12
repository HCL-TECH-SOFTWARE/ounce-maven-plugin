/*
 * Copyright (c) 2007, Ounce Labs, Inc.
 * All rights reserved.
 * (c) Copyright HCL Technologies Ltd. 2017. All rights reserved.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.ounce.core.OunceCoreException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class Utils
{
    static final String propertyFormat = "%**%";

    /**
     * Known jspCompilerName -&gt; jspCompilerType pairs, taken from this environment's
     * AppScan Source JspCompilerInfos configuration. Used to derive the missing one of the
     * two when only jspCompilerName or only jspCompilerType is explicitly provided, so the
     * ppf never ends up with a mismatched/inconsistent compiler name+type pair.
     */
    private static final Map<String, String> JSP_COMPILER_NAME_TO_TYPE = new HashMap<String, String>();

    /**
     * Reverse lookup of {@link #JSP_COMPILER_NAME_TO_TYPE}.
     */
    private static final Map<String, String> JSP_COMPILER_TYPE_TO_NAME = new HashMap<String, String>();

    private static void addJspCompilerMapping( String name, String type )
    {
        JSP_COMPILER_NAME_TO_TYPE.put( name, type );
        JSP_COMPILER_TYPE_TO_NAME.put( type, name );
    }

    static
    {
        addJspCompilerMapping( "Tomcat External", "1" );
        addJspCompilerMapping( "Tomcat 9", "15" );
        addJspCompilerMapping( "WebSphere 7.0", "5" );
        addJspCompilerMapping( "WebSphere 8.0", "8" );
        addJspCompilerMapping( "WebSphere 8.5", "9" );
        addJspCompilerMapping( "WebSphere 9.0", "10" );
        addJspCompilerMapping( "WebLogic 11g", "11" );
        addJspCompilerMapping( "WebLogic 12c", "12" );
    }

    /**
     * Resolves a partially specified jspCompilerName/jspCompilerType pair using the known
     * mapping table above, so that specifying only one of the two never produces a ppf with
     * a mismatched name/type combination.
     * <ul>
     * <li>Only name given -&gt; derive the matching type.</li>
     * <li>Only type given -&gt; derive the matching name.</li>
     * <li>Both given and they don't match the known table -&gt; throws {@link OunceCoreException},
     * failing the build with a clear message rather than silently writing an inconsistent ppf.</li>
     * <li>Neither given -&gt; returned unchanged (caller applies its own default, if any).</li>
     * </ul>
     *
     * @param jspCompilerInfoName the jspCompilerName as supplied by the mojo (may be null/empty)
     * @param jspCompilerType the jspCompilerType as supplied by the mojo (may be null/empty)
     * @param log mojo log used to report derived values
     * @return a 2-element array: [0] = resolved jspCompilerInfoName, [1] = resolved jspCompilerType
     * @throws OunceCoreException if both values are explicitly provided but don't match the known mapping
     */
    public static String[] resolveJspCompilerNameAndType( String jspCompilerInfoName, String jspCompilerType, Log log )
        throws OunceCoreException
    {
        boolean hasName = StringUtils.isNotEmpty( jspCompilerInfoName );
        boolean hasType = StringUtils.isNotEmpty( jspCompilerType );
        if (hasName)
        {
            String jspCompilerTypeFromName = JSP_COMPILER_NAME_TO_TYPE.get( jspCompilerInfoName );
            if(jspCompilerTypeFromName!=null){
                return new String[] { jspCompilerInfoName, JSP_COMPILER_NAME_TO_TYPE.get( jspCompilerInfoName ) };
            }
        }
        else if (hasType)
        {
            String jspCompilerNameFromType = JSP_COMPILER_TYPE_TO_NAME.get( jspCompilerType );
            if(jspCompilerNameFromType!=null){
                return new String[] { JSP_COMPILER_TYPE_TO_NAME.get( jspCompilerType ), jspCompilerType };
            }

        }
        return new String[] { jspCompilerInfoName, jspCompilerType };
    }

    public static String convertClasspathElement(String classpathElement, String ounceProjectDir, Map ounceVariableMap )
    {
    	String ret = convertToVariablePath(classpathElement, ounceVariableMap);
    	if(ret.equals(classpathElement) && ounceProjectDir != null)
			ret = makeRelative(classpathElement, ounceProjectDir);

        return ret;
    }

    /**
     * @param path path string
     * @param pathProperties path properties map
     * @return path string
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
     * @param obj object
     * @return object as string
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
	 * @return A List &lt;String&gt; of files of the given type or null if the given directory doesn't exist.
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
	/**
	 * Get value of a plugin's property
	 * @param project maven project
	 * @param groupId groupId of the plugin
	 * @param artifactId artifactId of the plugin
	 * @param property name of the property
	 * @return value of the property
	 */
	public static String getValueOfProperty(MavenProject project, String groupId, String artifactId, String property) {
		String propVal = null;
		Xpp3Dom configDom = (Xpp3Dom) project.getPlugin(groupId + ":" + artifactId).getConfiguration();
		if (configDom != null) {
			Xpp3Dom propDom = configDom.getChild(property);
			if (propDom != null)
				propVal = propDom.getValue();
		}
		return propVal;
	}
}
