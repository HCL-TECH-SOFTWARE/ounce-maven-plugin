/*
 * Copyright (c) 2007, Ounce Labs, Inc.
 * All rights reserved.
 * (c) Copyright HCL Technologies Ltd. 2019. All rights reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.plugin.logging.Log;
import org.apache.xerces.dom.DocumentImpl;
import org.codehaus.mojo.ounce.utils.Utils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author <a href="mailto:sam.headrick@ouncelabs.com">Sam Headrick</a>
 * 
 */
@Component( role=org.codehaus.mojo.ounce.core.OunceCore.class, hint="ouncexml" )
public class OunceCoreXmlSerializer implements OunceCore
{
    private HashMap <String,Object> m_existingProjectAttributes;

    public void createApplication(String baseDir, String theName,String applicationRoot, List theProjects, Map options, Log log) throws OunceCoreException
    {
        // sort them to avoid implementation details messing
        // up the order for testing.
        Collections.sort(theProjects);

        log.info("Writing Application parameters to xml...");

        try
        {
            m_existingProjectAttributes = new HashMap<String,Object>();

            // need to read the Application in first if it exists
            // create the XML Document
            Document xmlDoc;
            Element root = null;
            File pafFile = new File(baseDir, theName + ".paf");

            if ( pafFile.exists() )
            {
                // load up the PAF
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                log.info("Reading existing paf: '" + pafFile.getAbsolutePath() + "'..." );
                xmlDoc = builder.parse( pafFile );

                NodeList nodes = xmlDoc.getChildNodes();
                for ( int i = 0; i < nodes.getLength(); i++ )
                {
                    Node node = nodes.item( i );
                    String name = node.getNodeName();

                    if ( name.equals( "Application" ) )
                    {
                        root = (Element) node;

                        NodeList applicationChildren = node.getChildNodes();
                        for ( int j = 0; j < applicationChildren.getLength(); j++ )
                        {
                            Node child = applicationChildren.item( j );
                            String childName = child.getNodeName();
                            //NamedNodeMap attributes = child.getAttributes();
                            //String projectPath = attributes.getNamedItem( "path" ).getNodeValue();
                            //String pafPath = applicationFile.substring(0, applicationFile.lastIndexOf(File.separator));
                            // don't preserve Projects, everything else should be left alone
                            if ( childName.equals( "Project" ) )
                            {
                                NamedNodeMap attributes = child.getAttributes();
                                String projectPath = attributes.getNamedItem( "path" ).getNodeValue();
                                log.debug("OunceCoreXmlSerializer: projectpath: " + projectPath);
                           
                                //m_existingProjectAttributes.put( convPath, attributes);
                                m_existingProjectAttributes.put( projectPath, attributes);
                                node.removeChild( child );
                            }
                        }

                        // now add the new projects so they come first
                        log.debug("OunceCoreXmlSerializer: file path: " + pafFile.getAbsolutePath() + " Root: " + root);
                        insertChildProjects( xmlDoc, root, theProjects, pafFile.getAbsolutePath(), log );
                    }
                }
                if ( root == null )
                {
                    // every paf should have an Application element
                    throw new OunceCoreException( "The existing application file '" + pafFile.getAbsolutePath()
                        + "' is not in a valid format and cannot be updated." );
                }
            }
            else
            {
                log.info( "Creating new application file: '" + pafFile.getAbsolutePath() + "'..." );
                xmlDoc = new DocumentImpl();
                root = xmlDoc.createElement( "Application" );
                String name = theName;
                log.info("Application Name: " + theName);
                root.setAttribute( "name", theName);
                xmlDoc.appendChild( root );
                insertChildProjects( xmlDoc, root, theProjects,pafFile.getAbsolutePath(),log );
            }

            // write out the XML
            log.info("Writing XML ....");
            pafFile.getParentFile().mkdirs();
            XmlWriter writer = new XmlWriter( true );
            writer.setWriteEmptyValues( false );
            writer.setDefaultToAttributesOnSameLine( true );
            writer.saveXmlFile( pafFile.getAbsolutePath(), xmlDoc );
            log.info("Done.");
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }

    private void insertChildProjects( Document xmlDoc, Element root, List theProjects,String applicationFile, Log log ) throws IOException
    {
        // sort the projects by file path (in reverse order because they are written in reverse)
        Collections.sort( theProjects, new Comparator()
        {
            public int compare( Object arg0, Object arg1 )
            {
                OunceProjectBean project1 = (OunceProjectBean) arg0;
                OunceProjectBean project2 = (OunceProjectBean) arg1;
                String projectPath1 = project1.getPath() + File.separator + project1.name + ".ppf";
                String projectPath2 = project2.getPath() + File.separator + project2.name + ".ppf";
                return projectPath2.compareTo( projectPath1 );
            }
        } );

        for ( int i = 0; i < theProjects.size(); i++ )
        {
            OunceProjectBean projectBean = (OunceProjectBean) theProjects.get( i );
            String projectPath = new File(projectBean.getPath(), projectBean.getName() + ".ppf").getCanonicalPath();

            log.debug("OunceCoreXmlSerializer: Project Path: " + projectPath);
            log.debug("OunceCoreXmlSerializer: Name: " + projectBean.getName());

            Element project = xmlDoc.createElementNS( null, "Project" );

            NamedNodeMap existingAttribs = (NamedNodeMap) m_existingProjectAttributes.get( projectPath );

            if ( existingAttribs != null )
            {
                existingAttribs.removeNamedItem( "path" );
                existingAttribs.removeNamedItem( "language_type" );
            }

            projectPath = Utils.makeRelative(projectPath, new File(applicationFile).getParent());
            project.setAttributeNS( null, "path", projectPath );
            project.setAttributeNS( null, "language_type", "2" );

            if ( existingAttribs != null )
            {
                for ( int j = 0; j < existingAttribs.getLength(); j++ )
                {
                    Node node = existingAttribs.item( j );
                    String name = node.getNodeName();
                    String nodeValue = node.getNodeValue();
                    project.setAttributeNS( null, name, nodeValue );
                }
            }

            // if this a fresh XML doc or do we need to worry about making sure the projects come first?
            NodeList childNodes = root.getChildNodes();
            boolean hasChildren = childNodes.getLength() > 0;
            if ( hasChildren )
            {
                // there are other nodes
                Node child = childNodes.item( 0 );
                root.insertBefore( project, child );
            }
            else
            {
                root.appendChild( project );
            }
        }
    }

    public void createProject( String baseDir, String theName, String jspCompilerInfoName, String jspCompilerType, String projectRoot, List theSourceRoots,
                               String theWebRoot, String theClassPath, String theJdkName, String compilerOptions, String packaging, Map options, boolean forceWeb,
                               boolean analyzeStrutsFramework, boolean importStrutsValidation, String projectDir, Log log )
        throws OunceCoreException
    {
        // place all of the Project attributes into a property bundle
        Properties projectAttrs = new Properties();

        // set the dynamic values
        projectAttrs.setProperty( "name", theName );

        // set the constant values
        projectAttrs.setProperty("file_extension_set_name", "java");
        projectAttrs.setProperty( "language_type", "2" );
        projectAttrs.setProperty( "default_configuration_name", "Configuration 1" );
        
        // set jsp_compiler_info_name if provided
        if ( StringUtils.isNotEmpty(jspCompilerInfoName) )
        	projectAttrs.setProperty( "jsp_compiler_info_name", jspCompilerInfoName );
        
        // set jsp_compiler_type if provided
        if ( StringUtils.isNotEmpty(jspCompilerType) )
        	projectAttrs.setProperty( "jsp_compiler_type", jspCompilerType );

        if ( !StringUtils.isEmpty( theWebRoot ) && (forceWeb || ( !StringUtils.isEmpty( packaging ) && packaging.equals( "war" ) ) ) )
            projectAttrs.setProperty( "web_context_root_path", theWebRoot.trim() );
        else
            theWebRoot = null;

        if ( !StringUtils.isEmpty( compilerOptions ) )
            projectAttrs.setProperty( "compiler_options", compilerOptions );
        
        if ( analyzeStrutsFramework != false )
        	projectAttrs.setProperty( "analyze_struts_framework", "true" );
        
        if ( importStrutsValidation != false )
        	projectAttrs.setProperty( "import_struts_validation", "true" );

        try
        {
            HashMap existingConfigurationAttribs = new HashMap();
            HashMap existingSourceAttribs = new HashMap();
            HashMap<String, String> existingProperties = new HashMap<String, String>();
            ArrayList excludedSources = new ArrayList();

            Document xmlDoc;
            Element root = null;
     
            File ppfFile = new File(projectDir, theName + ".ppf");
            if ( ppfFile.exists() )
            {
                // need to preserve information that could be in the ppf (Project validation routines, etc.)
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                log.info( "Reading ppf: '" + ppfFile.getAbsolutePath() + "'..." );
                xmlDoc = builder.parse( ppfFile );
                NodeList nodes = xmlDoc.getChildNodes();
                for ( int i = 0; i < nodes.getLength(); i++ )
                {
                    Node node = nodes.item( i );
                    String name = node.getNodeName();
                    // Project should be the only top-level node -- others can be ignored
                    if ( name.equals( "Project" ) )
                    {
                        root = (Element) node;
                        NodeList projectChildren = node.getChildNodes();
                        for ( int j = 0; j < projectChildren.getLength(); j++ )
                        {
                            Node child = projectChildren.item( j );
                            String childName = child.getNodeName();

                            NamedNodeMap attributes = child.getAttributes();

                            // Don't preserve Configuration and Source (but remember their attributes for
                            // later). Everything else should be left alone.
                            if (childName.equals("Configuration")) {
                                String configurationName = attributes.getNamedItem( "name" ).getNodeValue();
                                existingConfigurationAttribs.put( configurationName, attributes );
                                node.removeChild( child );
                            }
                            else if(childName.equals("Source")) {
                                String sourcePath = attributes.getNamedItem( "path" ).getNodeValue();
                                log.info("Source Path: " + sourcePath);
                                String excludedStr = attributes.getNamedItem( "exclude" ).getNodeValue();
                                if ( excludedStr.equals( "true" ) )
                                {
                                    excludedSources.add( child );
                                }
                                existingSourceAttribs.put( sourcePath, attributes );
                                node.removeChild( child );
                            }
                            else if(childName.equals("Properties")) {
                            	//Store existing properties to be merged with any updated properties.
                            	NodeList properties = child.getChildNodes();
                            	for(int k = 0; k < properties.getLength(); k++) {
                            		Node property = properties.item(k);
                            		if(property.getNodeName().equals("Property")) {
                            			Node propertyName = property.getAttributes().getNamedItem("name");
                            			Node propertyValue = property.getAttributes().getNamedItem("value");
                            			if(propertyName != null && propertyValue != null)
	                            		existingProperties.put(propertyName.getNodeValue(), propertyValue.getNodeValue());
                            		}
                            	}
                            	node.removeChild(child);
                            }
                            // shouldn't need to handle SourceFile here because they come after Source
                        }
                    }
                }
                if ( root == null )
                {
                    // every ppf should have a Project element
                    throw new OunceCoreException( "The existing project file '" + ppfFile.getAbsolutePath()
                        + "' is not in a valid format and cannot be updated." );
                }
            }
            else
            {
                // create a new XML Document
                xmlDoc = new DocumentImpl();
                root = xmlDoc.createElement( "Project" );
                xmlDoc.appendChild( root );
            }

            // enumerate over the Project attributes and set them as attributes on the Project node
            Enumeration propertyNames = projectAttrs.propertyNames();
            while ( propertyNames.hasMoreElements() )
            {
                Object propertyNameObject = propertyNames.nextElement();
                String name = (String) propertyNameObject;
                String value = projectAttrs.getProperty( name );
                root.setAttribute( name, value );
            }
            
            insertSources(xmlDoc, root, baseDir, theSourceRoots, theWebRoot, existingSourceAttribs, excludedSources);
            insertConfigurations(xmlDoc, root, theClassPath, theJdkName, existingConfigurationAttribs);
            
            //Merge any existing properties with new properties and insert
            existingProperties.putAll(options);
            insertProperties(xmlDoc, root, existingProperties);

            // write out the XML
            log.info( "Writing ppf file..." );
            ppfFile.getParentFile().mkdirs();
            XmlWriter writer = new XmlWriter( true );
            writer.setWriteEmptyValues( false );
            writer.setDefaultToAttributesOnSameLine( true );
            writer.saveXmlFile(ppfFile.getAbsolutePath(), xmlDoc);
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }

    private void insertConfigurations( Document xmlDoc, Element root, String theClassPath, String theJdkName,
                                       HashMap existingConfigurationAttribs )
    {
        // place all of the Configuration attributes into a property bundle
        Properties configAttributes = new Properties();
        String configurationName = "Configuration 1";

        configAttributes.setProperty( "name", configurationName );
        configAttributes.setProperty( "class_path", theClassPath );
        if ( !StringUtils.isEmpty( theJdkName ) )
        {
            configAttributes.setProperty( "jdk_name", theJdkName.trim() );
        }

        // add the Configuration element to Project. Java Projects always have exactly one Configuration
        Element configuration = xmlDoc.createElementNS( null, "Configuration" );
        NamedNodeMap existingConfigAttribs = (NamedNodeMap) existingConfigurationAttribs.get( configurationName );

        // give the Configuration all its attributes
        Enumeration propertyNames = configAttributes.propertyNames();
        while ( propertyNames.hasMoreElements() )
        {
            Object propertyNameObject = propertyNames.nextElement();
            String name = (String) propertyNameObject;
            String value = configAttributes.getProperty( name );
            configuration.setAttributeNS( null, name, value );
            if (existingConfigAttribs != null && existingConfigAttribs.getNamedItem(name) != null)
            {
                existingConfigAttribs.removeNamedItem( name );
            }
        }

        if ( existingConfigAttribs != null )
        {
            for ( int j = 0; j < existingConfigAttribs.getLength(); j++ )
            {
                Node node = existingConfigAttribs.item( j );
                String name = node.getNodeName();
                String value = node.getNodeValue();
                configuration.setAttributeNS( null, name, value );
            }
        }

        NodeList childNodes = root.getChildNodes();
        boolean hasChildren = childNodes.getLength() > 0;
        if ( hasChildren )
        {
            Node child = childNodes.item( 0 );
            root.insertBefore( configuration, child );
        }
        else
        {
            root.appendChild( configuration );
        }
    }

    private void insertSources( Document xmlDoc, Element root, String baseDir, List theSourceRoots, String webRoot,
                                HashMap existingSourceAttribs, ArrayList excludedSources )
    {
        Collections.sort( theSourceRoots, new Comparator()
        {

            public int compare( Object arg0, Object arg1 )
            {
                String root1 = (String) arg0;
                String root2 = (String) arg1;
                return root2.compareTo( root1 );
            }

        } );

        for ( int i = 0; i < theSourceRoots.size(); i++ )
        {
            String sourceRoot = (String) theSourceRoots.get( i );

            if ( !pathAlreadyInNodeList( excludedSources, sourceRoot ) )
            {
                addSourceElement( xmlDoc, root, sourceRoot, "false", false, existingSourceAttribs );
            }
        }

        // now re-add any excluded sources created in the UI
        for ( int i = 0; i < excludedSources.size(); i++ )
        {
            Node node = (Node) excludedSources.get( i );
           
            NamedNodeMap attributes = node.getAttributes();
            String path = attributes.getNamedItem( "path" ).getNodeValue();
            if ( new File( baseDir + File.separator + path ).exists() )
            {
                NodeList childNodes = root.getChildNodes();
                boolean hasChildren = childNodes.getLength() > 0;
                if ( hasChildren )
                {
                    Node child = childNodes.item( 0 );
                    root.insertBefore( node, child );
                }
                else
                {
                    root.appendChild( node );
                }
            }
        }

        // add web context root if it exists
        
        ArrayList<Node>srcRoots = new ArrayList<Node>();
        srcRoots.addAll(theSourceRoots);
        if(webRoot == null || webRoot.isEmpty()){
        	return;
        }
        else if ( webRoot != null && !pathAlreadyInNodeList(theSourceRoots, webRoot, root))
        {
            addSourceElement( xmlDoc, root, webRoot, "true", true, existingSourceAttribs );
        }
        else
        {
        	NodeList nl = root.getChildNodes();
        	for(int i=0; i < nl.getLength(); i++)
        	{
        		Node node = nl.item(i);
        		NamedNodeMap nnm = node.getAttributes();
        		for(int x=0; x < nnm.getLength(); x++)
        		{
        			String pathAttr = nnm.getNamedItem("path").getNodeValue();
        			String webAttr = nnm.getNamedItem("web").getNodeValue();
        			
        			if(webRoot.equals(pathAttr)&& webAttr.equals("false"))
        			{
        				nnm.getNamedItem("web").setNodeValue("true");
        				     				
        			}
        		}
        	}
        }
    }

    private void insertProperties(Document xmlDoc, Element root, Map<String, String> properties) {
    	Element propertiesElement = xmlDoc.createElementNS( null, "Properties" );
    	for(String key : properties.keySet()) {
    		Element propertyElement = xmlDoc.createElementNS( null, "Property" );
    		propertyElement.setAttribute("name", key);
    		propertyElement.setAttribute("value", properties.get(key));
    		propertiesElement.appendChild(propertyElement);
    	}
    	root.appendChild(propertiesElement);
    }
    
    private void addSourceElement( Document xmlDoc, Element root, String sourceRoot, String defaultWeb,
                                   boolean forceWeb, HashMap existingSourceAttribs )
    {
        Element source = xmlDoc.createElementNS( null, "Source" );
        source.setAttributeNS( null, "path", sourceRoot );
        NamedNodeMap existingAttribs = (NamedNodeMap) existingSourceAttribs.get( sourceRoot );

        if ( existingAttribs != null )
            existingAttribs.removeNamedItem( "path" );

        if ( existingAttribs == null || existingAttribs.getNamedItem( "exclude" ) == null )
            source.setAttributeNS( null, "exclude", "false" );

        if ( forceWeb )
        {
            if ( existingAttribs != null )
                existingAttribs.removeNamedItem( "web" );

            source.setAttributeNS( null, "web", defaultWeb );

        }
        else if ( existingAttribs == null || existingAttribs.getNamedItem( "web" ) == null )
            source.setAttributeNS( null, "web", defaultWeb );

        if ( existingAttribs != null )
        {
            for ( int j = 0; j < existingAttribs.getLength(); j++ )
            {
                Node node = existingAttribs.item( j );
                String name = node.getNodeName();
                String value = node.getNodeValue();
                source.setAttributeNS( null, name, value );
            }
        }

        NodeList childNodes = root.getChildNodes();
        boolean hasChildren = childNodes.getLength() > 0;
        if ( hasChildren )
        {
            Node child = childNodes.item( 0 );
            root.insertBefore( source, child );
        }
        else
            root.appendChild( source );
    }

    private boolean pathAlreadyInNodeList( ArrayList list, String relPath )
    {
        for ( int i = 0; i < list.size(); i++ )
        {
            Node node = (Node) list.get( i );
            NamedNodeMap attributes = node.getAttributes();
            String path = attributes.getNamedItem( "path" ).getNodeValue();
            if ( !relPath.startsWith( "./" ) )
            {
                relPath = "./" + relPath;
            }
            if ( relPath.equals( path ) )
            {
                return true;
            }
        }
        return false;
    }
    
    private boolean pathAlreadyInNodeList(List list, String path, Element root)
    {
    	int counter=0;
    	
    	NodeList nl = root.getChildNodes();
    	    	
    	outer: for(int i=0; i < nl.getLength(); i++)
    	{
    		counter++;
    		
    		Node node = nl.item(i);
    		
    		NamedNodeMap nnm = node.getAttributes();
    		
    		if(nnm != null)
    		{
    			for(int x=0; x < nnm.getLength(); x++)
    			{
    				String pathAttr = nnm.getNamedItem("path").getNodeValue();
    				String webAttr = nnm.getNamedItem("web").getNodeValue();
    			
    				if(path.equals(pathAttr)&& webAttr.equals("false"))
    				{
    					return true;
    				}
    			
    			}
    		}
    		else
    		{
    			break outer;
    		}
    		
    	}
    	
    	return false;
    }

    public OunceCoreApplication readApplication( String path, Log log )
        throws OunceCoreException
    {
        try
        {
            Document xmlDoc;
            File pafFile = new File( path );
            if ( pafFile.exists() )
            {
                String parentDir = pafFile.getParent();
                String applicationFile = "File";
                String applicationName = "name";
                List projects = new ArrayList();
                Map options = new HashMap();

                // load up the PAF
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                if ( log != null )
                {
                    log.info( "Reading paf: '" + path + "'..." );
                }
                xmlDoc = builder.parse( pafFile );

                NodeList nodes = xmlDoc.getChildNodes();
                for ( int i = 0; i < nodes.getLength(); i++ )
                {
                    Node node = nodes.item( i );
                    String name = node.getNodeName();

                    if ( name.equals( "Application" ) )
                    {
                        NamedNodeMap applicationAttribs = node.getAttributes();
                        applicationName = applicationAttribs.getNamedItem( "name" ).getNodeValue();

                        NodeList applicationChildren = node.getChildNodes();
                        for ( int j = 0; j < applicationChildren.getLength(); j++ )
                        {
                            Node child = applicationChildren.item( j );
                            String childName = child.getNodeName();
                            if ( childName.equals( "Project" ) )
                            {
                            	
                                String projectPath = new File(parentDir, child.getAttributes().getNamedItem( "path" ).getNodeValue()).getAbsolutePath();                                
                                OunceCoreProject project = readProject( new File(projectPath).getAbsolutePath(), log );
                                projects.add( project );
                            }
                        }
                    }
                }

                OunceCoreApplication application = new OunceCoreApplication( applicationName,applicationFile, projects, options );
                return application;
            }
        }
        catch ( Exception ex )
        {
            if ( log != null )
                log.error( ex );
        }

        return null;
    }

    public OunceCoreProject readProject( String path, Log log )
        throws OunceCoreException
    {

        try
        {
            Document xmlDoc;
            File ppfFile = new File( path );
            if ( ppfFile.exists() )
            {
                String projectName = null;
                String jdkName = null;
                String classPath = null;
                String webRoot = null;
                String optionsStr = null;
                List sourceRoots = new ArrayList();
                Map options = new HashMap();

                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                if ( log != null )
                {
                    log.info( "Reading ppf: '" + path + "'..." );
                }
                xmlDoc = builder.parse( ppfFile );
                NodeList nodes = xmlDoc.getChildNodes();
                for ( int i = 0; i < nodes.getLength(); i++ )
                {
                    Node node = nodes.item( i );
                    String name = node.getNodeName();

                    // Project should be the only top-level node -- others can be ignored
                    if ( name.equals( "Project" ) )
                    {
                        NamedNodeMap projectAttribs = node.getAttributes();
                        for ( int j = 0; j < projectAttribs.getLength(); j++ )
                        {
                            Node attribNode = projectAttribs.item( j );
                            String nodeName = attribNode.getNodeName();
                            String nodeValue = attribNode.getNodeValue();
                            if ( !nodeName.equals( "name" ) && !nodeName.equals( "web_context_root_path" ) )
                            {
                                options.put( nodeName, nodeValue );
                            }
                        }

                        if ( projectAttribs.getNamedItem( "web_context_root_path" ) != null )
                        {
                            webRoot = projectAttribs.getNamedItem( "web_context_root_path" ).getNodeValue();
                        }

                        projectName = projectAttribs.getNamedItem( "name" ).getNodeValue();
                        NodeList projectChildren = node.getChildNodes();
                        for ( int j = 0; j < projectChildren.getLength(); j++ )
                        {
                            Node child = projectChildren.item( j );
                            String childName = child.getNodeName();

                            NamedNodeMap attribs = child.getAttributes();

                            if ( childName.equals( "Configuration" ) )
                            {
                                // attribs have name, jdk_name
                                if ( attribs.getNamedItem( "jdk_name" ) != null )
                                {
                                    jdkName = attribs.getNamedItem( "jdk_name" ).getNodeValue();
                                }
                                if ( attribs.getNamedItem( "class_path" ) != null )
                                {
                                    classPath = attribs.getNamedItem( "class_path" ).getNodeValue();
                                }
                                if ( attribs.getNamedItem( "compiler_options" ) != null )
                                {
                                    optionsStr = attribs.getNamedItem( "compiler_options" ).getNodeValue();
                                }
                            }
                            else if ( childName.equals( "Source" ) )
                            {
                                String sourcePath = attribs.getNamedItem( "path" ).getNodeValue();
                                String webStr = attribs.getNamedItem( "web" ).getNodeValue();
                                // add any non-web roots
                                if ( webStr == null || ( webStr != null && !webStr.equals( "true" ) ) )
                                {
                                    sourceRoots.add( sourcePath );
                                }
                            }
                        }
                    }
                }
                String packaging = null;
                if ( webRoot != null )
                {
                    packaging = "war";
                }

                OunceCoreProject project =
                    new OunceCoreProject( projectName, ppfFile.getParent(), sourceRoots, webRoot, classPath, jdkName,
                                          packaging, optionsStr, options );
                log.debug("OunceCoreXmlSerializer: projectRoot " + ppfFile.getParent()); 
                return project;
            }
            else
            {
                throw new OunceCoreException( "The file '" + ppfFile.getPath() + "' does not exist." );
            }
        }
        catch ( Exception ex )
        {
            if ( log != null )
            {
                log.error( ex );
            }
        }

        return null;
    }

    public void scan( String applicationFile, String assessmentName, String assessmentOutput, String scanconfig,String caller,
                      String reportType, String reportOutputType, String reportOutputLocation, boolean includeHowToFix, boolean publish,
                      Map ounceOptions, String installDir, boolean wait, boolean includeTraceDefinitive, 
                      boolean includeTraceSuspect, boolean includeTraceCoverage, String appserver_type,Log log )
        throws OunceCoreException
    {

        String command;
        if ( installDir == null )
            command = "ounceauto"; // just assume it's on the path
        else
            command = installDir + File.separator + "bin" + File.separator + "ounceauto";

        log.debug("ounceauto command: " + command);
        
        String existingAssessment = null;
        int includeSrcBefore = -1;
        int includeSrcAfter = -1;

        if ( ounceOptions != null )
        {
            if ( ounceOptions.get( "existingAssessmentFile" ) != null )
            {
                existingAssessment = (String) ounceOptions.get( "existingAssessmentFile" );
            }
            if ( ounceOptions.get( "includeSrcBefore" ) != null )
            {
                includeSrcBefore = ( (Integer) ounceOptions.get( "includeSrcBefore" ) ).intValue();
            }
            if ( ounceOptions.get( "includeSrcAfter" ) != null )
            {
                includeSrcAfter = ( (Integer) ounceOptions.get( "includeSrcAfter" ) ).intValue();
            }
        }

        try
        {
            if ( existingAssessment == null )
            {
                command += " scanapplication";
                if ( !StringUtils.isEmpty( applicationFile ) )
                {
                    command += " -application_file \"" + applicationFile + "\"";
                }
                if ( !StringUtils.isEmpty( assessmentName ) )
                {
                    command += " -name \"" + assessmentName + "\"";
                }
                if ( !StringUtils.isEmpty( assessmentOutput ) )
                {
                    command += " -save \"" + assessmentOutput + "\"";
                }
                if(!StringUtils.isEmpty(scanconfig))
                {
                	byte[] b = scanconfig.getBytes("UTF-8");
                	String convertedscanconfig = new String(b,"US-ASCII");
                	command += " -scanconfig \"" + convertedscanconfig +"\"";
                }
                if(!StringUtils.isEmpty(appserver_type))
                {
                	command += " -appserver_type \"" + appserver_type +"\"";
                }
                if ( !StringUtils.isEmpty( reportType ) )
                {
                    command +=
                        " -report \"" + reportType + "\" \"" + reportOutputType + "\" " + "\"" + reportOutputLocation
                            + "\"";
                    if(includeHowToFix)
                    {
                    	command += " -includeHowToFix";
                    }
                    if(includeTraceDefinitive)
                    {
                    	command += " -includeTraceDefinitive";
                    }
                    if(includeTraceSuspect)
                    {
                    	command += " -includeTraceSuspect";
                    }
                    if(includeTraceCoverage)
                    {
                    	command += " -includeTraceCoverage";
                    }
                }
                if ( publish )
                {
                    command += " -publish";
                }
            }
            else
            {
                // just generate a report for an existing saved assessment
                command += " generatereport -assessment \"" + existingAssessment + "\"";
                if ( !StringUtils.isEmpty( reportType ) )
                {
                    command +=
                        " -type \"" + reportType + "\" -output \"" + reportOutputType + "\" -file \""
                            + reportOutputLocation + "\"";
                    
                    if(includeHowToFix)
                    {
                    	command += " -includeHowToFix";
                    }
                    if(includeTraceDefinitive)
                    {
                    	command += " -includeTraceDefinitive";
                    }
                    if(includeTraceSuspect)
                    {
                    	command += " -includeTraceSuspect";
                    }
                    if(includeTraceCoverage)
                    {
                    	command += " -includeTraceCoverage";
                    }
                }
            }
            if ( !StringUtils.isEmpty( caller ) )
            {
                command += " -caller \"" + caller + "\"";
            }
            if ( includeSrcBefore != -1 )
            {
                command += " -includeSrcBefore " + includeSrcBefore;
            }
            if ( includeSrcAfter != -1 )
            {
                command += " -includeSrcAfter " + includeSrcAfter;
            }

            log.info( command );

            int requestId = executeCommand( command, log );

            log.info( "requestId: " + requestId );
            if ( wait )
            {
                if ( installDir == null )
                {
                    // just assume it's on the path
                    command = "ounceauto";
                }
                else
                {
                    command = installDir + File.separator + "bin" + File.separator + "ounceauto";
                }
                command += " wait -requestid " + requestId;
                log.info( command );
                executeCommand( command, log );
            }
        }
        catch ( Exception ex )
        {
            throw new OunceCoreException( ex );
        }
    }

    public void createPathVariables( Map pathVariableMap, String installDir, Log log )
        throws OunceCoreException
    {
        String command;
        if ( installDir == null )
            command = "ounceauto"; // just assume it's on the path
        else
            command = installDir + File.separator + "bin" + File.separator + "ounceauto";

        try
        {
            command += " setvars";
            if ( pathVariableMap != null )
            {
                Set keys = pathVariableMap.keySet();
                Iterator it = keys.iterator();
                while ( it.hasNext() )
                {
                    String key = (String) it.next();
                    String value = (String) pathVariableMap.get( key );

                    // TODO: need to put quotes around the key and value
                    command += " -" + key + " " + value;
                }
                log.info( command );
                executeCommand( command, log );
            }
        }
        catch ( Exception ex )
        {
            // drop this problem on the floor, it is not an issue to fail the build
        }
    }

    private int executeCommand( String command, Log log ) throws IOException, InterruptedException
    {
    	final String OS ="Windows";
    	String opSys = System.getProperty("os.name");
    	Process p = null;
    	log.info("Operating System: " + opSys);
    	if(!opSys.startsWith(OS))
    	{
    		if(opSys.equals("Mac OS X"))
    		{
    			File cli = new File("/Users/Shared/AppScanSource/logs/maven_ounceauto.sh");
    			cli.setExecutable(true);
    			FileWriter fw = new FileWriter(cli);
    			fw.append(command);
    			fw.close();
    			p = Runtime.getRuntime().exec( cli.getAbsolutePath() );
    		}
    		if(opSys.equals("Linux"))
    		{
    			File cli = new File("/var/opt/ibm/appscansource/logs/maven_ounceauto.sh");
    			cli.setExecutable(true);
    			FileWriter fw = new FileWriter(cli);
    			fw.append(command);
    			fw.close();
    			p = Runtime.getRuntime().exec( cli.getAbsolutePath() );
    		}
    	}
    	else
    	{
    		p = Runtime.getRuntime().exec( command );
    	}
  
        BufferedReader input = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
        String line;
        while ( ( line = input.readLine() ) != null )
        {
            if ( log != null )
            {
                log.info( "ounceauto: " + line );
            }
            else
            {
                log.info( "ounceauto: " + line );
            }
        }

        input.close();
        return p.waitFor();
    }

	public void publishASE(String aseApplication, String nameToPublish, String assessmentFile, String caller,
			String folderID, String installDir, boolean wait, Log log)
			throws OunceCoreException {
		
		String command;
		
		if(installDir == null )
		{
			command = "ounceauto";
		}
		else
		{
			command = installDir + File.separator + "bin" + File.separator + "ounceauto";
		}
		try
		{
			command += " PublishAssessmentASE";
			
			if(!StringUtils.isEmpty(aseApplication))
			{
				command += String.format(" -aseapplication \"%s\"",aseApplication);
			}

			if(!StringUtils.isEmpty(nameToPublish))
			{
				command += String.format(" -name \"%s\"",nameToPublish);
			}

			if(!StringUtils.isEmpty(assessmentFile))
			{
				command += String.format(" -file \"%s\"", assessmentFile);
			}
			
			if(!StringUtils.isEmpty(caller))
			{
				command += " -caller " + caller;
			}
			
			if(!StringUtils.isEmpty(folderID))
			{
				command += " -folderid " + folderID;
			}
			
			log.info( command );
			int requestId = executeCommand( command, log );
			log.info( "requestId: " + requestId );
			
			if ( wait )
            {
                if ( installDir == null )
                {
                    // just assume it's on the path
                    command = "ounceauto";
                }
                else
                {
                    command = installDir + File.separator + "bin" + File.separator + "ounceauto";
                }
                command += " wait -requestid " + requestId;
                log.info( command );
                executeCommand( command, log );
            }
		}
		catch(Exception ex)
		{
			throw new OunceCoreException(ex);
		}
		
	}
}
