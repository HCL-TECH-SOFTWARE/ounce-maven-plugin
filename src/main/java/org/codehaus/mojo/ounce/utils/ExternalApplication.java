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

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class ExternalApplication
{

    private String path;

    private String includes;

    private String excludes;

    public ExternalApplication( String thePath, String theIncludes, String theExcludes )
    {
        super();
        this.path = thePath;
        this.includes = theIncludes;
        this.excludes = theExcludes;
    }

    /**
     * @return the path
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * @param thePath the path to set
     */
    public void setPath( String thePath )
    {
        this.path = thePath;
    }

    /**
     * @return the includes
     */
    public String getIncludes()
    {
        return this.includes;
    }

    /**
     * @param theIncludes the includes to set
     */
    public void setIncludes( String theIncludes )
    {
        this.includes = theIncludes;
    }

    /**
     * @return the excludes
     */
    public String getExcludes()
    {
        return this.excludes;
    }

    /**
     * @param theExcludes the excludes to set
     */
    public void setExcludes( String theExcludes )
    {
        this.excludes = theExcludes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "ExternalApplication: Path: " + this.path + " Includes: " + this.includes + " Excludes: " +
            this.excludes;
    }

}
