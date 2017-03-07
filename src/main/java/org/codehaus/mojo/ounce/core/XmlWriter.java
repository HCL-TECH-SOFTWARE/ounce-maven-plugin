/*
 * Copyright (c) 2008, Ounce Labs, Inc.
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

import org.w3c.dom.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An XML Writer
 * 
 * @author <a href="mailto:sam.headrick@ouncelabs.com">Sam Headrick</a>
 */
public class XmlWriter
{

    public static final String START_ELEMENT = "<";

    public static final String START_CLOSE_ELEMENT = "</";

    public static final String END_CLOSE_ELEMENT = "/>";

    public static final String END_ELEMENT = ">";

    public static final String HEADER_TEXT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static String RETURN_CHAR = "\n";

    public static final String TAB_CHAR = "\t";

    public static final String QUOTE = "\"";

    public static final String EQUALS = "=";

    public static final String SPACE = " ";

    public static final String WINDOWS_FAMILY = "windows";

    public static final String LINUX_FAMILY = "linux";

    public static final String SOLARIS_FAMILY = "sunos";

    public static final String UNKNOWN_FAMILY = "other";

    public static final String[] OS_FAMILIES = new String[] { WINDOWS_FAMILY, LINUX_FAMILY, SOLARIS_FAMILY };

    private FileOutputStream m_outstream;

    private OutputStreamWriter m_writer;

    private boolean m_formatXml = true;

    private HashMap/* <String, Boolean> */m_attributesOnSameLine = new HashMap/* <String, Boolean> */();

    private HashMap/* <String, String[]> */m_attributeOrder = new HashMap/* <String, String[]> */();

    private String m_tabs = "";

    private boolean m_writeEmptyValues = false;

    private boolean m_defaultToAttributesOnSameLine = false;

    public XmlWriter()
    {
        this( true );
    }

    public XmlWriter( boolean formatXml )
    {
        m_formatXml = formatXml;
        if ( getOsFamily().equals( WINDOWS_FAMILY ) )
        {
            RETURN_CHAR = "\r\n";
        }
    }

    public void setDefaultToAttributesOnSameLine( boolean value )
    {
        m_defaultToAttributesOnSameLine = value;
    }

    public void setAttributesOnSameLine( String nodeName, boolean attributesOnSameLine )
    {
        m_attributesOnSameLine.put( nodeName, new Boolean( attributesOnSameLine ) );
    }

    public void setAttributeOrder( String nodeName, String[] attributeOrder )
    {
        m_attributeOrder.put( nodeName, attributeOrder );
    }

    public void setWriteEmptyValues( boolean writeEmptyValues )
    {
        m_writeEmptyValues = writeEmptyValues;
    }

    public void saveXmlFile( String filePath, Document xmlDoc )
        throws IOException
    {
        if ( xmlDoc != null )
        {
            m_outstream = new FileOutputStream( filePath );
            m_writer = new OutputStreamWriter( m_outstream, "UTF-8" );

            startXmlDoc();

            Element root = xmlDoc.getDocumentElement();

            writeElement( root );

            endXmlDoc();
        }
    }

    private void writeElement( Node element )
        throws IOException
    {

        if ( element.getNodeType() != Node.ELEMENT_NODE )
        {
            // skip
            return;
        }

        NamedNodeMap attributeMap = element.getAttributes();

        boolean hasChildren = false;

        NodeList childNodes = element.getChildNodes();
        for ( int i = 0; i < childNodes.getLength(); i++ )
        {
            Node node = childNodes.item( i );
            if ( node.getNodeType() == Node.ELEMENT_NODE )
            {
                hasChildren = true;
                break;
            }
        }

        if ( hasChildren )
        {
            openElement( element.getNodeName(), attributeMap );

            for ( int i = 0; i < childNodes.getLength(); i++ )
            {
                writeElement( childNodes.item( i ) );
            }

            closeElement( element.getNodeName() );
        }
        else
        {
            // no element children

            // check for text child
            String value = null;
            for ( int i = 0; i < childNodes.getLength(); i++ )
            {
                Node node = childNodes.item( i );
                if ( node.getNodeType() == Node.TEXT_NODE )
                {
                    value = node.getNodeValue();
                    break;
                }
            }

            if ( value == null )
            {
                addElementNoValue( element.getNodeName(), attributeMap );
            }
            else
            {
                addElementAndValue( element.getNodeName(), value, attributeMap );
            }
        }
    }

    private void addElementAndValue( String nodeName, String value, NamedNodeMap attributeMap )
        throws IOException
    {
        startWriteLine();
        writeStartElement( nodeName, attributeMap );
        m_writer.write( value );
        writeCloseElement( nodeName );
        endWriteLine();
    }

    private void addElementNoValue( String nodeName, NamedNodeMap attributeMap )
        throws IOException
    {
        startWriteLine();
        writeElementNoValue( nodeName, attributeMap );
        endWriteLine();
    }

    private void writeElementNoValue( String nodeName, NamedNodeMap attributeMap )
        throws IOException
    {
        m_writer.write( START_ELEMENT );
        m_writer.write( nodeName );
        writeAttributes( nodeName, attributeMap );
        m_writer.write( END_CLOSE_ELEMENT );
    }

    private void openElement( String nodeName, NamedNodeMap attributeMap )
        throws IOException
    {
        startWriteLine();
        writeStartElement( nodeName, attributeMap );
        endWriteLine();
        incrementTabs();
    }

    private void writeStartElement( String nodeName, NamedNodeMap attributeMap )
        throws IOException
    {
        m_writer.write( START_ELEMENT );
        m_writer.write( nodeName );
        writeAttributes( nodeName, attributeMap );
        m_writer.write( END_ELEMENT );
    }

    private void writeAttributes( String nodeName, NamedNodeMap attributeMap )
        throws IOException
    {
        if ( attributeMap != null )
        {

            boolean attributesOnSameLine = false;

            if ( m_defaultToAttributesOnSameLine )
            {
                attributesOnSameLine = true;
            }

            if ( m_attributesOnSameLine.get( nodeName ) != null )
            {
                attributesOnSameLine = ( (Boolean) m_attributesOnSameLine.get( nodeName ) ).booleanValue();
            }

            if ( !attributesOnSameLine )
            {
                m_writer.write( RETURN_CHAR );
                incrementTabs();
            }

            ArrayList/* <Node> */attributeList = new ArrayList/* <Node> */();

            if ( m_attributeOrder.get( nodeName ) != null )
            {
                // the attributes have a specific order
                String[] attributeOrder = (String[]) m_attributeOrder.get( nodeName );
                for ( int i = 0; i < attributeOrder.length; i++ )
                {
                    Node node = attributeMap.getNamedItem( attributeOrder[i] );
                    attributeList.add( node );
                }
            }
            else
            {
                for ( int i = 0; i < attributeMap.getLength(); i++ )
                {
                    Node node = attributeMap.item( i );
                    attributeList.add( node );
                }
            }

            for ( int i = 0; i < attributeList.size(); i++ )
            {

                Node node = (Node) attributeList.get( i );
                if ( node != null && node.getNodeValue() != null
                    && ( node.getNodeValue().length() > 0 || m_writeEmptyValues ) )
                {

                    if ( !attributesOnSameLine )
                    {
                        m_writer.write( m_tabs );
                    }

                    m_writer.write( SPACE );
                    m_writer.write( node.getNodeName() );
                    m_writer.write( EQUALS );
                    m_writer.write( QUOTE );
                    m_writer.write( safeEncode( node.getNodeValue() ) );
                    m_writer.write( QUOTE );

                    if ( !attributesOnSameLine )
                    {
                        m_writer.write( RETURN_CHAR );
                    }
                }
            }

            if ( !attributesOnSameLine )
            {
                decrementTabs();
                m_writer.write( m_tabs );
            }
        }
    }

    private void closeElement( String nodeName )
        throws IOException
    {
        decrementTabs();
        startWriteLine();
        writeCloseElement( nodeName );
        endWriteLine();
    }

    private void writeCloseElement( String nodeName )
        throws IOException
    {
        m_writer.write( START_CLOSE_ELEMENT );
        m_writer.write( nodeName );
        m_writer.write( END_ELEMENT );
    }

    public void startWriteLine()
        throws IOException
    {
        if ( m_formatXml )
        {
            m_writer.write( m_tabs );
        }
    }

    public void endWriteLine()
        throws IOException
    {
        if ( m_formatXml )
        {
            m_writer.write( RETURN_CHAR );
        }
    }

    private void startXmlDoc()
        throws IOException
    {
        m_writer.write( HEADER_TEXT );
        m_writer.write( RETURN_CHAR );
    }

    private void endXmlDoc()
        throws IOException
    {
        m_writer.flush();
        m_writer.close();
        m_outstream.close();
    }

    private void incrementTabs()
    {
        if ( m_formatXml )
        {
            m_tabs += TAB_CHAR;
        }
    }

    private void decrementTabs()
    {
        if ( m_formatXml )
        {
            if ( m_tabs.length() >= TAB_CHAR.length() )
            {
                m_tabs = m_tabs.substring( TAB_CHAR.length() );
            }
        }
    }

    /**
     * Replaces XML special characters with the XML special encoding. 
     * If the output writer already encodes the XML, do not use this method,
     * as it will be encoded twice.
     * 
     * @param str the string to encode
     * @return the encoded string
     */
    public static String safeEncode( String str )
    {
        StringBuffer result = null;

        for ( int i = 0, max = str.length(), delta = 0; i < max; i++ )
        {
            char c = str.charAt( i );
            String replacement = null;
            if ( c == '&' )
            {
                replacement = "&amp;";
            }
            else if ( c == '<' )
            {
                replacement = "&lt;";
            }
            else if ( c == '\r' )
            {
                replacement = "&#13;";
            }
            else if ( c == '>' )
            {
                replacement = "&gt;";
            }
            else if ( c == '"' )
            {
                replacement = "&quot;";
            }
            else if ( c == '\'' )
            {
                replacement = "&apos;";
            }

            if ( replacement != null )
            {
                if ( result == null )
                {
                    result = new StringBuffer( str );
                }

                result.replace( i + delta, i + delta + 1, replacement );
                delta += ( replacement.length() - 1 );
            }
        }

        if ( result == null )
        {
            return str;
        }

        return result.toString();
    }

    /**
     * @return the operating system name
     */
    private static String getOsName()
    {
        return System.getProperty( "os.name" ).toLowerCase();
    }

    /**
     * Get the OS family, this looks in the OS_FAMILIES array for a match
     * 
     * @return the os family
     */
    public static String getOsFamily()
    {
        String osName = getOsName();
        String familyName = UNKNOWN_FAMILY;
        for ( int i = 0; i < OS_FAMILIES.length; ++i )
        {
            if ( osName.startsWith( OS_FAMILIES[i] ) )
            {
                familyName = OS_FAMILIES[i];
                break;
            }
        }
        return familyName;
    }
}
