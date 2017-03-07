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

import java.util.List;

import org.apache.maven.project.MavenProject;

/**
 * This mojo generates an Ounce project file. It forks the build and executes the process-sources phase so that any
 * plugins that may generate sources and attach new source folders to the project will execute and those source folders
 * will be automatically included in the generated project. This mojo is intended to be executed from the command line.
 * If you would rather have the project built automatically during your build, use the project-only goal instead.
 * 
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * @requiresDependencyResolution test
 * @goal project
 * @execute phase="process-sources"
 */
public class ProjectMojo
    extends ProjectOnlyMojo
{

    /**
     * The project executed when forked.
     * 
     * @parameter expression="${executedProject}"
     * @readonly
     */
    protected MavenProject executedProject;

    /**
     * This method gets the source roots from the forked project (executedProject)
     * 
     * @return List of source roots.
     */
    protected List getSourceRoots()
    {
        List sourceRoots = executedProject.getCompileSourceRoots();

        if ( this.includeTestSources )
        {
            sourceRoots.addAll( executedProject.getTestCompileSourceRoots() );
        }
        return sourceRoots;
    }

    /**
     * @return the executedProject
     */
    public MavenProject getExecutedProject()
    {
        return this.executedProject;
    }

    /**
     * @param theExecutedProject the executedProject to set
     */
    public void setExecutedProject( MavenProject theExecutedProject )
    {
        this.executedProject = theExecutedProject;
    }
}
